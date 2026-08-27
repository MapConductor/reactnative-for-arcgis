package com.mapconductor.react.arcgis

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LoadStatus
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.ArcGISTiledElevationSource
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.GraphicsRenderingMode
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISDesignTypeInterface
import com.mapconductor.arcgis.ArcGISGeoViewHolder
import com.mapconductor.arcgis.ArcGISMapView2DController
import com.mapconductor.arcgis.ArcGISMapView2DHolder
import com.mapconductor.arcgis.ArcGISMapViewController
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.ArcGISMapViewInitOptions
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.WrapMapView
import com.mapconductor.arcgis.WrapSceneView
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
 * `com.mapconductor.arcgis.ArcGISMapView`/`ArcGISMapView2D`'s `getXController()` helpers and their
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

    registerMarkerRenderingSupport(
        serviceRegistry = serviceRegistry,
        createRenderer = { mapController.createMarkerRenderer() },
        createEventController = { mapController.createMarkerEventController(it) },
        registerEventController = { mapController.registerMarkerEventController(it) },
        onRenderingReady = { mapController.sendInitialCameraUpdate() },
    )

    return mapController
}

/**
 * Builds the 3D `ArcGISScene` (basemap + the design's elevation sources), waits for it to finish
 * loading, and assembles the `ArcGISMapViewController` with the same sub-controllers as the 2D
 * path - mirroring `ArcGISMapView`'s `holderProvider`/`controllerProvider`. The only real
 * differences from 2D are the scene/elevation setup and the marker overlay's surface placement.
 */
suspend fun createArcGISSceneViewController(
    wrapView: WrapSceneView,
    mapDesignType: ArcGISDesignTypeInterface,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
    serviceRegistry: MutableMapServiceRegistry? = null,
): ArcGISMapViewController {
    val options =
        ArcGISMapViewInitOptions(
            basemapStyle = ArcGISDesign.toBasemapStyle(mapDesignType),
            elevationSources = mapDesignType.elevationSources,
        )
    val scene = ArcGISScene(options.basemapStyle)
    options.elevationSources.forEach { scene.baseSurface.elevationSources.add(ArcGISTiledElevationSource(it)) }
    wrapView.sceneView.scene = scene

    val loadStatusScope = CoroutineScope(Dispatchers.Default)
    val holder =
        suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { loadStatusScope.cancel() }
            loadStatusScope.launch {
                scene.loadStatus.collect { status ->
                    when (status) {
                        // FailedToLoad でも holder は返す。オフラインでも地図ビュー自体は
                        // 出したいので（android-for-arcgis の ArcGISMapView.kt と同じ判断）。
                        is LoadStatus.Loaded, is LoadStatus.FailedToLoad ->
                            if (cont.isActive) {
                                cont.resumeWith(
                                    Result.success(
                                        ArcGISMapViewHolder(mapView = wrapView, map = wrapView.sceneView),
                                    ),
                                )
                            }
                        else -> Unit
                    }
                }
            }
        }

    val markerLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            renderingMode = GraphicsRenderingMode.Dynamic
            // 標高のある面にマーカーを貼り付ける。3D だけの設定。
            sceneProperties.surfacePlacement = SurfacePlacement.Relative
        }
    holder.geoView.graphicsOverlays.add(markerLayer)

    val mapController =
        ArcGISMapViewController(
            holder = holder,
            markerController = getArcGISMarkerController(holder, markerLayer, markerTiling),
            polylineController = getArcGISPolylineController(holder),
            polygonController = getArcGISPolygonController(holder),
            circleController = getArcGISCircleController(holder),
            groundImageController = getArcGISGroundImageController(holder),
            rasterLayerController = getArcGISRasterLayerController(holder),
        )

    registerMarkerRenderingSupport(
        serviceRegistry = serviceRegistry,
        createRenderer = { mapController.createMarkerRenderer() },
        createEventController = { mapController.createMarkerEventController(it) },
        registerEventController = { mapController.registerMarkerEventController(it) },
        onRenderingReady = { mapController.sendInitialCameraUpdate() },
    )

    return mapController
}

/**
 * 2D / 3D どちらのコントローラでも同じ形なので、`MarkerRenderingSupport` の登録はここに寄せる。
 * 呼び忘れるとマーカーが黙って描かれなくなる（拡張側は registry を引けないと何もしない）。
 */
private fun registerMarkerRenderingSupport(
    serviceRegistry: MutableMapServiceRegistry?,
    createRenderer: () -> MarkerOverlayRendererInterface<ArcGISActualMarker>,
    createEventController: (StrategyMarkerController<ArcGISActualMarker>) -> MarkerEventControllerInterface<ArcGISActualMarker>,
    registerEventController: (MarkerEventControllerInterface<ArcGISActualMarker>) -> Unit,
    onRenderingReady: () -> Unit,
) {
    val registry = serviceRegistry ?: return
    registry.clear()
    registry.put(
        MarkerRenderingSupportKey,
        object : MarkerRenderingSupport<ArcGISActualMarker> {
            override fun createMarkerRenderer(
                strategy: MarkerRenderingStrategyInterface<ArcGISActualMarker>,
            ): MarkerOverlayRendererInterface<ArcGISActualMarker> = createRenderer()

            override fun createMarkerEventController(
                controller: StrategyMarkerController<ArcGISActualMarker>,
                renderer: MarkerOverlayRendererInterface<ArcGISActualMarker>,
            ): MarkerEventControllerInterface<ArcGISActualMarker> = createEventController(controller)

            override fun registerMarkerEventController(controller: MarkerEventControllerInterface<ArcGISActualMarker>) {
                registerEventController(controller)
            }

            override fun onMarkerRenderingReady() {
                onRenderingReady()
            }
        },
    )
}

private fun getArcGISCircleController(
    holder: ArcGISGeoViewHolder<*, *>,
): ArcGISCircleOverlayController {
    val circleLayer = GraphicsOverlay()
    holder.geoView.graphicsOverlays.add(circleLayer)
    return ArcGISCircleOverlayController(renderer = ArcGISCircleOverlayRenderer(circleLayer = circleLayer, holder = holder))
}

private fun getArcGISPolylineController(
    holder: ArcGISGeoViewHolder<*, *>,
): ArcGISPolylineOverlayController {
    val polylineLayer = GraphicsOverlay()
    holder.geoView.graphicsOverlays.add(polylineLayer)
    return ArcGISPolylineOverlayController(
        renderer = ArcGISPolylineOverlayRenderer(polylineLayer = polylineLayer, holder = holder),
    )
}

private fun getArcGISPolygonController(
    holder: ArcGISGeoViewHolder<*, *>,
): ArcGISPolygonOverlayController {
    val polygonLayer = GraphicsOverlay()
    holder.geoView.graphicsOverlays.add(polygonLayer)
    return ArcGISPolygonOverlayController(
        renderer = ArcGISPolygonOverlayRenderer(polygonLayer = polygonLayer, holder = holder),
    )
}

private fun getArcGISMarkerController(
    holder: ArcGISGeoViewHolder<*, *>,
    markerLayer: GraphicsOverlay,
    markerTiling: MarkerTilingOptions,
): ArcGISMarkerController {
    val renderer = ArcGISMarkerRenderer(markerLayer = markerLayer, holder = holder)
    val markerManager = MarkerManager.defaultManager<ArcGISActualMarker>(minMarkerCount = markerTiling.minMarkerCount)
    return ArcGISMarkerController(markerManager = markerManager, renderer = renderer, markerTiling = markerTiling)
}

private fun getArcGISRasterLayerController(
    holder: ArcGISGeoViewHolder<*, *>,
): ArcGISRasterLayerController =
    ArcGISRasterLayerController(renderer = ArcGISRasterLayerOverlayRenderer(holder = holder))

private fun getArcGISGroundImageController(
    holder: ArcGISGeoViewHolder<*, *>,
): ArcGISGroundImageController {
    val tileServer = TileServerRegistry.get()
    return ArcGISGroundImageController(
        renderer = ArcGISGroundImageOverlayRenderer(holder = holder, tileServer = tileServer),
    )
}
