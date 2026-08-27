package com.mapconductor.react.arcgis

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ProcessLifecycleOwner
import com.mapconductor.arcgis.ArcGISDesignTypeInterface
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapView2DController
import com.mapconductor.arcgis.ArcGISMapViewController
import com.mapconductor.arcgis.ArcGISMapViewControllerInterface
import com.mapconductor.arcgis.ArcGISMapViewScope
import com.mapconductor.arcgis.WrapMapView
import com.mapconductor.arcgis.WrapSceneView
import com.mapconductor.core.controller.BaseMapViewController
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
 *
 * 2D（`MapView`）と 3D（`SceneView`）で別のラッパーを出す。ネイティブビューの生成と
 * コントローラの組み立てだけが違い、それ以外は [ArcGISReactNativeHostBase] が共通で持つ。
 */
abstract class ArcGISMapViewWrapperBase(context: Context) : MapConductorMapViewWrapperBase(context) {
    abstract fun setApiKey(apiKey: String?)
}

/** 3D（`SceneView`）。他プラットフォームと同じく無印が 3D。 */
class ArcGISMapViewWrapper(context: Context) : ArcGISMapViewWrapperBase(context) {
    private val arcGISHost = ArcGISSceneReactNativeHost()

    override val host: MapConductorReactNativeHost = arcGISHost

    override fun setApiKey(apiKey: String?) {
        arcGISHost.apiKey = apiKey
    }
}

/** 2D（`MapView`）。 */
class ArcGISMapView2DWrapper(context: Context) : ArcGISMapViewWrapperBase(context) {
    private val arcGISHost = ArcGIS2DReactNativeHost()

    override val host: MapConductorReactNativeHost = arcGISHost

    override fun setApiKey(apiKey: String?) {
        arcGISHost.apiKey = apiKey
    }
}

/**
 * ArcGIS の地図一式を RN のラッパー基底が扱える形へ翻訳する。
 *
 * 2D と 3D で違うのは「どのネイティブビューを作るか」「コントローラをどう組み立てるか」
 * 「どう畳むか」の 3 点だけなので、そこだけ抽象メソッドにしてある。
 */
private abstract class ArcGISReactNativeHostBase<C> : MapConductorReactNativeHost
    where C : BaseMapViewController, C : ArcGISMapViewControllerInterface {
    override val providerName = "ArcGIS"
    override val extensionScope = ArcGISMapViewScope()
    override val serviceRegistry = MutableMapServiceRegistry()

    var apiKey: String? = null

    protected val lifecycleOwner = ProcessLifecycleOwner.get()
    private val coroutine = CoroutineScope(Dispatchers.Main)
    private var wrapView: FrameLayout? = null
    private var controller: C? = null
    private var mapDesign: ArcGISDesignTypeInterface = ArcGISDesign.Streets

    /** `WrapMapView` / `WrapSceneView` を作り、ライフサイクルを開始して返す。 */
    protected abstract fun createWrapView(context: Context): FrameLayout

    /** 地図の読み込み完了を待ってコントローラを組み立てる。 */
    protected abstract suspend fun createController(
        wrapView: FrameLayout,
        markerTiling: MarkerTilingOptions,
    ): C

    /**
     * 初回カメラ送出。`sendInitialCameraUpdate()` は 2D / 3D のコントローラが
     * それぞれ持っていて共通の型に無いので、ここで振り分ける。
     */
    protected abstract fun sendInitialCameraUpdate(controller: C)

    /** `WrapMapView` / `WrapSceneView` を畳む。 */
    protected abstract fun teardown(wrapView: FrameLayout)

    override fun createMapView(
        context: Context,
        initialCamera: MapCameraPosition,
        markerTiling: MarkerTilingOptions,
        delegate: MapConductorReactNativeHostDelegate,
    ): View {
        ensureArcGISInitialized(context, apiKey)

        val view = createWrapView(context)
        wrapView = view

        coroutine.launch {
            val viewController = createController(view, markerTiling)
            if (!delegate.isAttached) return@launch
            controller = viewController
            delegate.onControllerReady(viewController)
            viewController.setMapInitializedListener { delegate.onMapLoaded() }
            view.post {
                viewController.moveCamera(initialCamera)
                sendInitialCameraUpdate(viewController)
            }
        }
        return view
    }

    override fun setMapDesign(id: String?) {
        mapDesign = ArcGISReactNativeDesign.from(id)
        controller?.setMapDesignType(mapDesign)
    }

    /** 地図生成時に渡すデザイン。コントローラができるまでは prop の値を持っておく。 */
    protected val currentMapDesign: ArcGISDesignTypeInterface
        get() = mapDesign

    override fun toScreenOffset(position: GeoPointInterface): Offset? =
        controller?.holder?.toScreenOffset(position)

    override fun destroy() {
        controller = null
        val view = wrapView
        wrapView = null
        view?.let { teardown(it) }
        coroutine.cancel()
    }
}

/** 3D。`SceneView` を `WrapSceneView` に載せ、`ArcGISMapViewController` を組む。 */
private class ArcGISSceneReactNativeHost : ArcGISReactNativeHostBase<ArcGISMapViewController>() {
    override fun createWrapView(context: Context): FrameLayout {
        val sceneView = com.arcgismaps.mapping.view.SceneView(context)
        val wrapView =
            WrapSceneView(context).apply {
                addView(
                    sceneView,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            }
        wrapView.sceneView = sceneView
        // ライフサイクルは attach/draw より先に始めないと GeoView.lifeCycleOwner が
        // 未初期化で落ちる（android-for-arcgis の viewProvider と同じ順序）。
        sceneView.onCreate(lifecycleOwner)
        sceneView.onResume(lifecycleOwner)
        return wrapView
    }

    override suspend fun createController(
        wrapView: FrameLayout,
        markerTiling: MarkerTilingOptions,
    ): ArcGISMapViewController =
        createArcGISSceneViewController(
            wrapView = wrapView as WrapSceneView,
            mapDesignType = currentMapDesign,
            markerTiling = markerTiling,
            serviceRegistry = serviceRegistry,
        )

    override fun sendInitialCameraUpdate(controller: ArcGISMapViewController) {
        controller.sendInitialCameraUpdate()
    }

    override fun teardown(wrapView: FrameLayout) {
        (wrapView as WrapSceneView).onPause(lifecycleOwner)
        wrapView.onDestroy(lifecycleOwner)
    }
}

/** 2D。`MapView` を `WrapMapView` に載せ、`ArcGISMapView2DController` を組む。 */
private class ArcGIS2DReactNativeHost : ArcGISReactNativeHostBase<ArcGISMapView2DController>() {
    override fun createWrapView(context: Context): FrameLayout {
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
        nativeMapView.onCreate(lifecycleOwner)
        nativeMapView.onResume(lifecycleOwner)
        return wrapView
    }

    override suspend fun createController(
        wrapView: FrameLayout,
        markerTiling: MarkerTilingOptions,
    ): ArcGISMapView2DController =
        createArcGISMapViewController(
            wrapView = wrapView as WrapMapView,
            mapDesignType = currentMapDesign,
            markerTiling = markerTiling,
            serviceRegistry = serviceRegistry,
        )

    override fun sendInitialCameraUpdate(controller: ArcGISMapView2DController) {
        controller.sendInitialCameraUpdate()
    }

    override fun teardown(wrapView: FrameLayout) {
        (wrapView as WrapMapView).onPause(lifecycleOwner)
        wrapView.onDestroy(lifecycleOwner)
    }
}
