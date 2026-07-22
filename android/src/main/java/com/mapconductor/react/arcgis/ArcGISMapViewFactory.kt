package com.mapconductor.react.arcgis

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LoadStatus
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.GraphicsRenderingMode
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISDesignTypeInterface
import com.mapconductor.arcgis.ArcGISMapView2DController
import com.mapconductor.arcgis.ArcGISMapView2DHolder
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.WrapMapView
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageController
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageOverlayRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.marker.ArcGISMarkerRenderer
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer
import com.mapconductor.arcgis.raster.ArcGISRasterLayerController
import com.mapconductor.arcgis.raster.ArcGISRasterLayerOverlayRenderer
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.MarkerRenderingSupport
import com.mapconductor.core.marker.MarkerRenderingSupportKey
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.tileserver.TileServerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * `com.mapconductor.arcgis.ArcGISMapView2D`'s `getXController()` helpers and its
 * `defaultArcGISInitialize()`/`getArcGisApiKey()` are `internal` to the android-for-arcgis module,
 * so this file mirrors their bodies (all the types they use - GraphicsOverlay, the per-feature
 * overlay controllers/renderers, MarkerManager, TileServerRegistry - are public) rather than
 * calling into them directly. Keep this in sync with android-for-arcgis's ArcGISMapView.kt /
 * ArcGISMapView2D.kt if that module's construction sequence changes.
 */

/**
 * Reads `ARCGIS_API_KEY` from `AndroidManifest.xml` metadata unless [apiKeyOverride] is given, and
 * configures `ArcGISEnvironment.apiKey`. No-ops if credentials are already configured (e.g. by
 * OAuth elsewhere in the host app).
 */
fun ensureArcGISInitialized(context: Context, apiKeyOverride: String?): Boolean {
    if (ArcGISEnvironment.authenticationManager.arcGISCredentialStore.getCredentials().isNotEmpty()) {
        return true
    }
    val apiKey = apiKeyOverride?.takeIf { it.isNotBlank() } ?: context.applicationContext.getArcGisApiKey()
    if (apiKey.isNullOrBlank()) {
        Log.e("ArcGISMapView", "apiKey prop or <meta-data android:name=\"ARCGIS_API_KEY\" /> is required")
        return false
    }
    ArcGISEnvironment.apiKey = ApiKey.create(apiKey)
    return true
}

private fun Context.getArcGisApiKey(): String? =
    packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("ARCGIS_API_KEY")

/**
 * Builds the 2D `ArcGISMap`, waits for it to finish loading, and assembles the
 * `ArcGISMapView2DController` together with its marker/circle/polygon/polyline/ground-image/raster
 * sub-controllers - mirroring `ArcGISMapView2D`'s `holderProvider`/`controllerProvider`.
 */
suspend fun createArcGISMapViewController(
    wrapView: WrapMapView,
    mapDesignType: ArcGISDesignTypeInterface,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
    serviceRegistry: MutableMapServiceRegistry? = null,
): ArcGISMapView2DController {
    val basemapStyle = ArcGISDesign.toBasemapStyle(mapDesignType)
    val map = ArcGISMap(basemapStyle)
    wrapView.arcGISMapView.map = map

    val loadStatusScope = CoroutineScope(Dispatchers.Default)
    val holder =
        suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { loadStatusScope.cancel() }
            loadStatusScope.launch {
                map.loadStatus.collect { status ->
                    when (status) {
                        is LoadStatus.Loaded, is LoadStatus.FailedToLoad ->
                            if (cont.isActive) {
                                cont.resumeWith(
                                    Result.success(
                                        ArcGISMapView2DHolder(mapView = wrapView, map = wrapView.arcGISMapView),
                                    ),
                                )
                            }
                        else -> Unit
                    }
                }
            }
        }

    val markerLayer: GraphicsOverlay = GraphicsOverlay().apply { renderingMode = GraphicsRenderingMode.Dynamic }
    holder.geoView.graphicsOverlays.add(markerLayer)
    val markerController = getArcGISMarkerController(holder, markerLayer, markerTiling)
    val polylineController = getArcGISPolylineController(holder)
    val rasterLayerController = getArcGISRasterLayerController(holder)
    val polygonController = getArcGISPolygonController(holder)
    val circleController = getArcGISCircleController(holder)
    val groundImageController = getArcGISGroundImageController(holder)

    val mapController =
        ArcGISMapView2DController(
            holder = holder,
            markerController = markerController,
            polylineController = polylineController,
            polygonController = polygonController,
            circleController = circleController,
            groundImageController = groundImageController,
            rasterLayerController = rasterLayerController,
        )

    serviceRegistry?.let { registry ->
        registry.clear()
        registry.put(
            MarkerRenderingSupportKey,
            object : MarkerRenderingSupport<ArcGISActualMarker> {
                override fun createMarkerRenderer(
                    strategy: MarkerRenderingStrategyInterface<ArcGISActualMarker>,
                ): MarkerOverlayRendererInterface<ArcGISActualMarker> = mapController.createMarkerRenderer()

                override fun createMarkerEventController(
                    controller: StrategyMarkerController<ArcGISActualMarker>,
                    renderer: MarkerOverlayRendererInterface<ArcGISActualMarker>,
                ): MarkerEventControllerInterface<ArcGISActualMarker> =
                    mapController.createMarkerEventController(controller)

                override fun registerMarkerEventController(
                    controller: MarkerEventControllerInterface<ArcGISActualMarker>,
                ) {
                    mapController.registerMarkerEventController(controller)
                }

                override fun onMarkerRenderingReady() {
                    mapController.sendInitialCameraUpdate()
                }
            },
        )
    }

    return mapController
}

private fun getArcGISCircleController(
    holder: ArcGISMapView2DHolder,
): ArcGISCircleOverlayController {
    val circleLayer = GraphicsOverlay()
    holder.geoView.graphicsOverlays.add(circleLayer)
    return ArcGISCircleOverlayController(renderer = ArcGISCircleOverlayRenderer(circleLayer = circleLayer, holder = holder))
}

private fun getArcGISPolylineController(
    holder: ArcGISMapView2DHolder,
): ArcGISPolylineOverlayController {
    val polylineLayer = GraphicsOverlay()
    holder.geoView.graphicsOverlays.add(polylineLayer)
    return ArcGISPolylineOverlayController(
        renderer = ArcGISPolylineOverlayRenderer(polylineLayer = polylineLayer, holder = holder),
    )
}

private fun getArcGISPolygonController(
    holder: ArcGISMapView2DHolder,
): ArcGISPolygonOverlayController {
    val polygonLayer = GraphicsOverlay()
    holder.geoView.graphicsOverlays.add(polygonLayer)
    return ArcGISPolygonOverlayController(
        renderer = ArcGISPolygonOverlayRenderer(polygonLayer = polygonLayer, holder = holder),
    )
}

private fun getArcGISMarkerController(
    holder: ArcGISMapView2DHolder,
    markerLayer: GraphicsOverlay,
    markerTiling: MarkerTilingOptions,
): ArcGISMarkerController {
    val renderer = ArcGISMarkerRenderer(markerLayer = markerLayer, holder = holder)
    val markerManager = MarkerManager.defaultManager<ArcGISActualMarker>(minMarkerCount = markerTiling.minMarkerCount)
    return ArcGISMarkerController(markerManager = markerManager, renderer = renderer, markerTiling = markerTiling)
}

private fun getArcGISRasterLayerController(
    holder: ArcGISMapView2DHolder,
): ArcGISRasterLayerController =
    ArcGISRasterLayerController(renderer = ArcGISRasterLayerOverlayRenderer(holder = holder))

private fun getArcGISGroundImageController(
    holder: ArcGISMapView2DHolder,
): ArcGISGroundImageController {
    val tileServer = TileServerRegistry.get()
    return ArcGISGroundImageController(
        renderer = ArcGISGroundImageOverlayRenderer(holder = holder, tileServer = tileServer),
    )
}
