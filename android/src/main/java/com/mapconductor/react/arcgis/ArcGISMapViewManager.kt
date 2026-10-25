package com.mapconductor.react.arcgis

import com.mapconductor.react.wrapper.MapConductorMapViewCommands
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

/**
 * 2D / 3D で違うのはビュー名と作るラッパーだけなので、prop とコマンドの配線はここに置く。
 * `@ReactProp` は親クラスの分も拾われる（ViewManagersPropertyCache が superclass を辿る）。
 */
abstract class ArcGISMapViewManagerBase : ViewGroupManager<ArcGISMapViewWrapperBase>() {
    override fun onAfterUpdateTransaction(view: ArcGISMapViewWrapperBase) {
        super.onAfterUpdateTransaction(view)
        view.initializeMapIfNeeded()
    }

    @ReactProp(name = "apiKey")
    fun setApiKey(
        view: ArcGISMapViewWrapperBase,
        apiKey: String?,
    ) {
        view.setApiKey(apiKey)
    }

    @ReactProp(name = "cameraPosition")
    fun setCameraPosition(
        view: ArcGISMapViewWrapperBase,
        cameraPosition: ReadableMap?,
    ) {
        view.setCameraPosition(cameraPosition)
    }

    @ReactProp(name = "mapDesignType")
    fun setMapDesignType(
        view: ArcGISMapViewWrapperBase,
        mapDesignType: String?,
    ) {
        view.setMapDesignType(mapDesignType)
    }

    /**
     * 地図の見た目。JS 側は「記述」だけ送ってくる（コンパイラが wasm で
     * Hermes に wasm が無い）ので、組み立てはネイティブ側で行う。
     * 詳細は `ReactNativeVectorStyle`。
     */
    @ReactProp(name = "vectorStyle")
    fun setVectorStyle(
        view: ArcGISMapViewWrapperBase,
        vectorStyle: ReadableMap?,
    ) {
        view.setVectorStyle(vectorStyle)
    }

    @ReactProp(name = "infoBubblePositions")
    fun setInfoBubblePositions(
        view: ArcGISMapViewWrapperBase,
        positions: ReadableArray?,
    ) {
        view.setInfoBubblePositions(positions)
    }

    @ReactProp(name = "markerTilingOptions")
    fun setMarkerTilingOptions(
        view: ArcGISMapViewWrapperBase,
        options: ReadableMap?,
    ) {
        view.setMarkerTilingOptions(options)
    }

    override fun receiveCommand(
        root: ArcGISMapViewWrapperBase,
        commandId: String,
        args: ReadableArray?,
    ) {
        // コマンド名の対応は全プロバイダ共通。写経すると綴り違いが黙って無効化されるため
        // js-sdk-react に集約してある。
        MapConductorMapViewCommands.receive(root, commandId, args)
    }

    override fun onDropViewInstance(view: ArcGISMapViewWrapperBase) {
        view.onDropViewInstance()
        super.onDropViewInstance(view)
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> =
        MapConductorMapViewCommands.directEventTypeConstants()
}

/** 3D（`SceneView`）。他プラットフォームと同じく無印が 3D。 */
class ArcGISMapViewManager : ArcGISMapViewManagerBase() {
    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): ArcGISMapViewWrapperBase =
        ArcGISMapViewWrapper(reactContext)

    companion object {
        const val REACT_CLASS = "ArcGISMapView"
    }
}

/** 2D（`MapView`）。 */
class ArcGISMapView2DViewManager : ArcGISMapViewManagerBase() {
    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): ArcGISMapViewWrapperBase =
        ArcGISMapView2DWrapper(reactContext)

    companion object {
        const val REACT_CLASS = "ArcGISMapView2D"
    }
}
