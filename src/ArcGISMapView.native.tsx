import type { HostComponent } from 'react-native';
import { NativeMapViewHost } from '@mapconductor/js-sdk-react/internal';
import type { ArcGISMapViewStateInterface } from '@mapconductor/react-for-arcgis/state';
import { ArcGISMapViewController } from './ArcGISMapViewController.native';
import type { ArcGISMapViewProps } from './ArcGISMapViewProps.native';
import type { ArcGISMapViewRef } from './ArcGISTypeAlias.native';
import NativeArcGISMapView, {
  type NativeArcGISMapViewProps,
} from './ArcGISMapViewNativeComponent';
import NativeArcGISMapView2D from './ArcGISMapView2DNativeComponent';

/**
 * ネイティブイベントの配線・オーバーレイ収集・InfoBubble レイヤは全 RN プロバイダで
 * 同一なので {@link NativeMapViewHost} に集約してある。ここで渡すのは
 * 「どのネイティブビューか」「デザインをどう文字列化するか」だけ。
 */
function ArcGISNativeMapView(
  props: ArcGISMapViewProps & { nativeComponent: HostComponent<NativeArcGISMapViewProps> }
) {
  const { nativeComponent, ...rest } = props;
  return (
    <NativeMapViewHost<ArcGISMapViewRef, ArcGISMapViewStateInterface>
      {...rest}
      nativeComponent={nativeComponent}
      mapDesignValue={props.state.mapDesignType.id}
      nativeProps={{ apiKey: props.state.apiKey }}
      createController={(ref, camera) => new ArcGISMapViewController(ref, camera)}
    />
  );
}

/**
 * 3D の地図（Esri の `SceneView`）。android-for-arcgis / ios-for-arcgis /
 * react-for-arcgis の `ArcGISMapView` と同じく、無印が 3D。
 */
export function ArcGISMapView(props: ArcGISMapViewProps) {
  return <ArcGISNativeMapView {...props} nativeComponent={NativeArcGISMapView} />;
}

/**
 * 2D の地図（Esri の `MapView`）。傾きは他プラットフォームと同じく擬似的な表現で、
 * カメラそのものは傾かない。
 */
export function ArcGISMapView2D(props: ArcGISMapViewProps) {
  return <ArcGISNativeMapView {...props} nativeComponent={NativeArcGISMapView2D} />;
}
