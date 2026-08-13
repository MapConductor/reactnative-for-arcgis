import { ArcGISMapViewStateInterface } from '@mapconductor/react-for-arcgis/state';
export { ArcGISDesign, ArcGISDesignType, ArcGISMapViewState, ArcGISViewState, ArcGISViewStateOptions, useArcGISViewState } from '@mapconductor/react-for-arcgis/state';
import React from 'react';
import { ViewProps, HostComponent, NativeMethods } from 'react-native';
import { GeoPoint, MapCameraPosition, MarkerTilingOptions, MapViewHolder, Offset, BaseMapViewController, MapViewControllerInterface, CircleCapable, GroundImageCapable, MarkerCapable, PolygonCapable, PolylineCapable, RasterLayerCapable, NativeMapExtensionCapable, GeoRectBounds, MapUISettings, MarkerState, PolylineState, CircleState, OnCircleEventHandler, GroundImageState, OnGroundImageEventHandler, PolygonState, OnPolygonEventHandler, OnPolylineEventHandler, RasterLayerState, NativeMapExtensionDescriptor, NativeMapExtensionEventHandler, NativeMapExtensionEvent as NativeMapExtensionEvent$1, OnMarkerEventHandler, MarkerAnimationOverlayHost } from '@mapconductor/js-sdk-core';
import { NativeMapExtensionEvent, MapViewBaseProps } from '@mapconductor/js-sdk-react/native';

interface NativeArcGISMapViewEvent<T> {
    nativeEvent: T;
}
interface NativeMarkerTilingOptions {
    enabled: boolean;
    debugTileOverlay: boolean;
    minMarkerCount: number;
    cacheSize: number;
    /**
     * A JS function can't cross the RN bridge, so this only signals that
     * `iconScaleCallback` is set; the native wrapper resolves the actual
     * per-marker scale by calling back into JS via MarkerScaleBridge (JSI).
     */
    hasIconScaleCallback: boolean;
}
interface NativeArcGISMapViewProps extends ViewProps {
    apiKey?: string;
    cameraPosition?: {
        position: {
            latitude: number;
            longitude: number;
            altitude?: number | null;
        };
        zoom: number;
        bearing: number;
        tilt: number;
    };
    mapDesignType?: string;
    markerTilingOptions?: NativeMarkerTilingOptions;
    infoBubblePositions?: Array<{
        id: string;
        latitude: number;
        longitude: number;
        altitude?: number | null;
    }>;
    onMapLoaded?: () => void;
    onMarkerCompositionBatchProcessed?: (event: NativeArcGISMapViewEvent<{
        generation: number;
        sequence: number;
    }>) => void;
    onMapClick?: (event: NativeArcGISMapViewEvent<{
        point: GeoPoint;
    }>) => void;
    onMapLongClick?: (event: NativeArcGISMapViewEvent<{
        point: GeoPoint;
    }>) => void;
    onCameraMoveStart?: (event: NativeArcGISMapViewEvent<{
        cameraPosition: MapCameraPosition;
    }>) => void;
    onCameraMove?: (event: NativeArcGISMapViewEvent<{
        cameraPosition: MapCameraPosition;
    }>) => void;
    onCameraMoveEnd?: (event: NativeArcGISMapViewEvent<{
        cameraPosition: MapCameraPosition;
    }>) => void;
    onMarkerClick?: (event: NativeArcGISMapViewEvent<{
        markerId: string;
    }>) => void;
    onCircleClick?: (event: NativeArcGISMapViewEvent<{
        circleId: string;
        point: GeoPoint;
    }>) => void;
    onGroundImageClick?: (event: NativeArcGISMapViewEvent<{
        groundImageId: string;
        point: GeoPoint;
    }>) => void;
    onPolylineClick?: (event: NativeArcGISMapViewEvent<{
        polylineId: string;
        point: GeoPoint;
    }>) => void;
    onPolygonClick?: (event: NativeArcGISMapViewEvent<{
        polygonId: string;
        point: GeoPoint;
    }>) => void;
    onMarkerDragStart?: (event: NativeArcGISMapViewEvent<{
        markerId: string;
        point: GeoPoint;
    }>) => void;
    onMarkerDrag?: (event: NativeArcGISMapViewEvent<{
        markerId: string;
        point: GeoPoint;
    }>) => void;
    onMarkerDragEnd?: (event: NativeArcGISMapViewEvent<{
        markerId: string;
        point: GeoPoint;
    }>) => void;
    onMarkerAnimateStart?: (event: NativeArcGISMapViewEvent<{
        markerId: string;
    }>) => void;
    onMarkerAnimateEnd?: (event: NativeArcGISMapViewEvent<{
        markerId: string;
    }>) => void;
    onMarkerScreenPositions?: (event: NativeArcGISMapViewEvent<{
        positions: Array<{
            markerId: string;
            x: number;
            y: number;
        }>;
    }>) => void;
    onInfoBubbleScreenPositions?: (event: NativeArcGISMapViewEvent<{
        positions: Array<{
            id: string;
            x: number;
            y: number;
        }>;
    }>) => void;
    onNativeMapExtensionEvent?: (event: NativeArcGISMapViewEvent<NativeMapExtensionEvent>) => void;
}
declare function toNativeMarkerTilingOptions(markerTilingOptions: MarkerTilingOptions | undefined): NativeMarkerTilingOptions | undefined;
declare function toNativeCameraPosition(cameraPosition: MapCameraPosition | undefined): {
    position: {
        latitude: number;
        longitude: number;
        altitude: number;
    };
    zoom: number;
    bearing: number;
    tilt: number;
} | undefined;

type ArcGISMapViewRef = React.ComponentRef<HostComponent<NativeArcGISMapViewProps>> & NativeMethods;
type ArcGISMapMapView = ArcGISMapViewRef | null;
type ArcGISMapMap = null;

declare class ArcGISMapViewHolder implements MapViewHolder<ArcGISMapViewRef | null, null> {
    private readonly nativeRef;
    readonly map: null;
    constructor(nativeRef: React.RefObject<ArcGISMapViewRef | null>);
    get mapView(): ArcGISMapViewRef | null;
    toScreenOffset(_position: GeoPoint): null;
    fromScreenOffset(_offset: Offset): Promise<GeoPoint | null>;
    fromScreenOffsetSync(_offset: Offset): GeoPoint | null;
}

declare class ArcGISMapViewController extends BaseMapViewController implements MapViewControllerInterface, CircleCapable, GroundImageCapable, MarkerCapable, PolygonCapable, PolylineCapable, RasterLayerCapable, NativeMapExtensionCapable {
    private readonly nativeRef;
    readonly holder: ArcGISMapViewHolder;
    private cameraPosition;
    private mapLoaded;
    private markerCompositionGeneration;
    private activeMarkerComposition;
    private pendingMarkerComposition;
    private markerBatchAck;
    private readonly pendingMarkerUpdates;
    private readonly markerStates;
    private readonly circleStates;
    private readonly groundImageStates;
    private readonly polygonStates;
    private readonly polylineStates;
    private readonly rasterLayerStates;
    private pendingPolygons;
    private pendingCircles;
    private pendingGroundImages;
    private pendingPolylines;
    private pendingRasterLayers;
    private markerClickListener;
    private circleClickListener;
    private groundImageClickListener;
    private markerDragStartListener;
    private markerDragListener;
    private markerDragEndListener;
    private markerAnimateStartListener;
    private markerAnimateEndListener;
    private polygonClickListener;
    private polylineClickListener;
    private readonly nativeMapExtensionEventHandlers;
    constructor(nativeRef: React.RefObject<ArcGISMapViewRef | null>, cameraPosition: MapCameraPosition);
    clearOverlays(): Promise<void>;
    moveCamera(position: MapCameraPosition): Promise<boolean>;
    animateCamera(position: MapCameraPosition, durationMillis: number): Promise<boolean>;
    fitBounds(bounds: GeoRectBounds, padding: number): Promise<boolean>;
    getCameraPosition(): MapCameraPosition | null;
    /**
     * ジェスチャ設定をネイティブへ転送する。web 版が地図エンジンへ直接適用するのに対し、
     * RN はネイティブのコントローラが `applyUISettings` を持つのでブリッジ 1 本で済む。
     */
    applyUISettings(settings: MapUISettings): void;
    compositionMarkers(data: MarkerState[]): Promise<void>;
    updateMarker(state: MarkerState): Promise<void>;
    compositionPolylines(data: PolylineState[]): Promise<void>;
    compositionCircles(data: CircleState[]): Promise<void>;
    updateCircle(state: CircleState): Promise<void>;
    hasCircle(state: CircleState): boolean;
    setOnCircleClickListener(listener: OnCircleEventHandler | null): void;
    compositionGroundImages(data: GroundImageState[]): Promise<void>;
    updateGroundImage(state: GroundImageState): Promise<void>;
    hasGroundImage(state: GroundImageState): boolean;
    setOnGroundImageClickListener(listener: OnGroundImageEventHandler | null): void;
    compositionPolygons(data: PolygonState[]): Promise<void>;
    updatePolygon(state: PolygonState): Promise<void>;
    hasPolygon(state: PolygonState): boolean;
    setOnPolygonClickListener(listener: OnPolygonEventHandler | null): void;
    updatePolyline(state: PolylineState): Promise<void>;
    hasPolyline(state: PolylineState): boolean;
    setOnPolylineClickListener(listener: OnPolylineEventHandler | null): void;
    compositionRasterLayers(data: RasterLayerState[]): Promise<void>;
    updateRasterLayer(state: RasterLayerState): Promise<void>;
    hasRasterLayer(state: RasterLayerState): boolean;
    upsertNativeMapExtension(extension: NativeMapExtensionDescriptor, eventHandler?: NativeMapExtensionEventHandler | null): void;
    removeNativeMapExtension(extensionId: string): void;
    onNativeMapExtensionEvent(event: NativeMapExtensionEvent$1): void;
    hasMarker(state: MarkerState): boolean;
    setOnMarkerClickListener(listener: OnMarkerEventHandler | null): void;
    setOnMarkerDragStart(listener: OnMarkerEventHandler | null): void;
    setOnMarkerDrag(listener: OnMarkerEventHandler | null): void;
    setOnMarkerDragEnd(listener: OnMarkerEventHandler | null): void;
    setOnMarkerAnimateStart(listener: OnMarkerEventHandler | null): void;
    setOnMarkerAnimateEnd(listener: OnMarkerEventHandler | null): void;
    setMarkerAnimationOverlayHost(_host: MarkerAnimationOverlayHost | null): void;
    setMapInitializedListener(listener: (() => void) | null): void;
    destroy(): void;
    onNativeCameraMoveStart(camera: MapCameraPosition): void;
    onNativeCameraMove(camera: MapCameraPosition): void;
    onNativeCameraMoveEnd(camera: MapCameraPosition): void;
    onNativeMapLoaded(): void;
    onNativeMarkerCompositionBatchProcessed(generation: number, sequence: number): void;
    onNativeMapClick(point: GeoPoint): void;
    onNativeMapLongClick(point: GeoPoint): void;
    onNativeMarkerClick(markerId: string): void;
    onNativeCircleClick(circleId: string, clicked: GeoPoint): void;
    onNativeGroundImageClick(groundImageId: string, clicked: GeoPoint): void;
    onNativePolylineClick(polylineId: string, clicked: GeoPoint): void;
    onNativePolygonClick(polygonId: string, clicked: GeoPoint): void;
    onNativeMarkerDragStart(markerId: string, point: GeoPoint): void;
    onNativeMarkerDrag(markerId: string, point: GeoPoint): void;
    onNativeMarkerDragEnd(markerId: string, point: GeoPoint): void;
    onNativeMarkerAnimateStart(markerId: string): void;
    onNativeMarkerAnimateEnd(markerId: string): void;
    private dispatchCommand;
    private flushPendingMarkerUpdates;
    private startPendingMarkerComposition;
    private waitForMarkerBatchAck;
    private cancelMarkerBatchAck;
    private cancelMarkerComposition;
}

interface ArcGISMapViewProps extends Omit<MapViewBaseProps<ArcGISMapViewStateInterface>, 'state'> {
    state?: ArcGISMapViewStateInterface;
    mapId?: string;
    markerTilingOptions?: MarkerTilingOptions;
    className?: string;
    onError?: (error: Error) => void;
}
declare function ArcGISMapView({ style, state, onMapLoaded, onMapClick, onMapLongClick, onCameraMoveStart, onCameraMove, onCameraMoveEnd, cameraRestriction, markerTilingOptions, children, }: ArcGISMapViewProps): React.JSX.Element;

export { type ArcGISMapMap, type ArcGISMapMapView, ArcGISMapView, ArcGISMapViewController, ArcGISMapViewHolder, type ArcGISMapViewProps, type ArcGISMapViewRef, type NativeArcGISMapViewEvent, type NativeArcGISMapViewProps, type NativeMarkerTilingOptions, toNativeCameraPosition, toNativeMarkerTilingOptions };
