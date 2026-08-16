import { ArcGISMapViewStateInterface } from '@mapconductor/react-for-arcgis/state';
export { ArcGISDesign, ArcGISDesignType, ArcGISDesignTypeInterface, ArcGISMapViewState, ArcGISMapViewStateInterface, ArcGISMapViewStateParams, ArcGISViewState, ArcGISViewStateOptions, useArcGISViewState } from '@mapconductor/react-for-arcgis/state';
import * as React from 'react';
import React__default from 'react';
import { HostComponent, NativeMethods } from 'react-native';
import { NativeMapViewProps, NativeMapViewEvent, ReactNativeMapViewHolder, ReactNativeBridgeMapViewController } from '@mapconductor/js-sdk-react/internal';
export { NativeMarkerStatePayload as NativeArcGISMarkerState, NativeMarkerTilingOptions, markerStateToNative, toNativeCameraPosition, toNativeMarkerTilingOptions } from '@mapconductor/js-sdk-react/internal';
import { MapViewControllerInterface, MarkerTilingOptions } from '@mapconductor/js-sdk-core';
import { MapViewBaseProps } from '@mapconductor/js-sdk-react/native';

type NativeArcGISMapViewEvent<T> = NativeMapViewEvent<T>;
/** プロバイダ固有のネイティブ props。共通部は NativeMapViewProps 側にある。 */
interface NativeArcGISMapViewProps extends NativeMapViewProps {
    apiKey?: string;
}

type ArcGISMapViewRef = React__default.ComponentRef<HostComponent<NativeArcGISMapViewProps>> & NativeMethods;
type ArcGISMapMapView = ArcGISMapViewRef | null;
type ArcGISMapMap = null;

type ArcGISMapViewControllerInterface = MapViewControllerInterface;

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

interface ArcGISMapViewProps extends MapViewBaseProps<ArcGISMapViewStateInterface> {
    className?: string;
    onError?: (error: Error) => void;
    children?: React__default.ReactNode;
    markerTilingOptions?: MarkerTilingOptions;
}

/**
 * ネイティブイベントの配線・オーバーレイ収集・InfoBubble レイヤは全 RN プロバイダで
 * 同一なので {@link NativeMapViewHost} に集約してある。ここで渡すのは
 * 「どのネイティブビューか」「デザインをどう文字列化するか」だけ。
 */
declare function ArcGISMapView(props: ArcGISMapViewProps): React.JSX.Element;

export { type ArcGISMapMap, type ArcGISMapMapView, ArcGISMapView, ArcGISMapViewController, type ArcGISMapViewControllerInterface, ArcGISMapViewHolder, type ArcGISMapViewProps, type ArcGISMapViewRef, type NativeArcGISMapViewEvent, type NativeArcGISMapViewProps };
