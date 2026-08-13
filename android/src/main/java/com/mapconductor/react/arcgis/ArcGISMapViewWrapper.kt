package com.mapconductor.react.arcgis

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ProcessLifecycleOwner
import com.mapconductor.arcgis.ArcGISDesignTypeInterface
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapView2DController
import com.mapconductor.arcgis.ArcGISMapViewScope
import com.mapconductor.arcgis.WrapMapView
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.react.wrapper.MapConductorMapViewWrapperBase
import com.mapconductor.react.wrapper.MapConductorReactNativeHost
import com.mapconductor.react.wrapper.MapConductorReactNativeHostDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * RN の ArcGIS ビュー。
 *
 * コマンドの受け口・マーカー取り込み・スクリーン座標の通知・拡張の Compose レイヤは
 * [MapConductorMapViewWrapperBase]（js-sdk-react/android）が全部持っているので、
 * ここはプロバイダ固有のアダプタと、API キーの prop だけ。
 */
class ArcGISMapViewWrapper(context: Context) : MapConductorMapViewWrapperBase(context) {
    private val arcGISHost = ArcGISReactNativeHost()

    override val host: MapConductorReactNativeHost = arcGISHost

    fun setApiKey(apiKey: String?) {
        arcGISHost.apiKey = apiKey
    }
}

/** ArcGIS の地図一式を RN のラッパー基底が扱える形へ翻訳する。 */
private class ArcGISReactNativeHost : MapConductorReactNativeHost {
    override val providerName = "ArcGIS"
    override val extensionScope = ArcGISMapViewScope()
    override val serviceRegistry = MutableMapServiceRegistry()

    var apiKey: String? = null

    private val lifecycleOwner = ProcessLifecycleOwner.get()
    private val coroutine = CoroutineScope(Dispatchers.Main)
    private var wrapMapView: WrapMapView? = null
    private var controller: ArcGISMapView2DController? = null
    private var mapDesign: ArcGISDesignTypeInterface = ArcGISDesign.Streets

    override fun createMapView(
        context: Context,
        initialCamera: MapCameraPosition,
        markerTiling: MarkerTilingOptions,
        delegate: MapConductorReactNativeHostDelegate,
    ): View {
        ensureArcGISInitialized(context, apiKey)

        val nativeMapView = com.arcgismaps.mapping.view.MapView(context)
        val wrapView =
            WrapMapView(context).apply {
                addView(
                    nativeMapView,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            }
        wrapView.arcGISMapView = nativeMapView
        wrapMapView = wrapView
        nativeMapView.onCreate(lifecycleOwner)
        nativeMapView.onResume(lifecycleOwner)

        coroutine.launch {
            val viewController =
                createArcGISMapViewController(
                    wrapView = wrapView,
                    mapDesignType = mapDesign,
                    markerTiling = markerTiling,
                    serviceRegistry = serviceRegistry,
                )
            if (!delegate.isAttached) return@launch
            controller = viewController
            delegate.onControllerReady(viewController)
            viewController.setMapInitializedListener { delegate.onMapLoaded() }
            wrapView.post {
                viewController.moveCamera(initialCamera)
                viewController.sendInitialCameraUpdate()
            }
        }
        return wrapView
    }

    override fun setMapDesign(id: String?) {
        mapDesign = ArcGISReactNativeDesign.from(id)
        controller?.setMapDesignType(mapDesign)
    }

    override fun toScreenOffset(position: GeoPointInterface): Offset? =
        controller?.holder?.toScreenOffset(position)

    override fun destroy() {
        controller = null
        val wrapView = wrapMapView
        wrapMapView = null
        wrapView?.onPause(lifecycleOwner)
        wrapView?.onDestroy(lifecycleOwner)
        coroutine.cancel()
    }
}
