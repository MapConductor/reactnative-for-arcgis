import { ArcGISMapViewStateInterface } from '@mapconductor/react-for-arcgis/state';
export { ArcGISDesign, ArcGISDesignType, ArcGISMapViewState, ArcGISViewState, ArcGISViewStateOptions, useArcGISViewState } from '@mapconductor/react-for-arcgis/state';
import React from 'react';
import { ViewProps, HostComponent, NativeMethods } from 'react-native';
import { GeoPoint, MapCameraPosition, MarkerTilingOptions } from '@mapconductor/js-sdk-core';
import { NativeMapExtensionEvent, MapViewBaseProps } from '@mapconductor/js-sdk-react/native';
import { ReactNativeMapViewHolder, ReactNativeBridgeMapViewController } from '@mapconductor/js-sdk-react/internal';

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

/**
 * RN のホルダーは全プロバイダで同一（投影はネイティブ側が行う）なので
 * {@link ReactNativeMapViewHolder} に集約してある。ここは ref 型を与えるだけ。
 */
declare class ArcGISMapViewHolder extends ReactNativeMapViewHolder<ArcGISMapViewRef> {
}

/**
 * ネイティブブリッジの実装は全 RN プロバイダで同一なので
 * {@link ReactNativeBridgeMapViewController} に集約してある。ここはネイティブビューの
 * ref 型を与えるだけ。プロバイダ固有の振る舞いが要るときだけメソッドを override する。
 */
declare class ArcGISMapViewController extends ReactNativeBridgeMapViewController<ArcGISMapViewRef> {
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
