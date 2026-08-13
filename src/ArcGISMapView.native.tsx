import { NativeMapViewHost } from '@mapconductor/js-sdk-react/internal';
import type { ArcGISMapViewStateInterface } from '@mapconductor/react-for-arcgis/state';
import { ArcGISMapViewController } from './ArcGISMapViewController.native';
import type { ArcGISMapViewProps } from './ArcGISMapViewProps.native';
import type { ArcGISMapViewRef } from './ArcGISTypeAlias.native';
import NativeArcGISMapView from './ArcGISMapViewNativeComponent';

/**
 * ネイティブイベントの配線・オーバーレイ収集・InfoBubble レイヤは全 RN プロバイダで
 * 同一なので {@link NativeMapViewHost} に集約してある。ここで渡すのは
 * 「どのネイティブビューか」「デザインをどう文字列化するか」だけ。
 */
export function ArcGISMapView(props: ArcGISMapViewProps) {
  return (
    <NativeMapViewHost<ArcGISMapViewRef, ArcGISMapViewStateInterface>
      {...props}
      nativeComponent={NativeArcGISMapView}
      mapDesignValue={props.state.mapDesignType.id}
      nativeProps={{ apiKey: props.state.apiKey }}
      createController={(ref, camera) => new ArcGISMapViewController(ref, camera)}
    />
  );
}
