import { requireNativeComponent } from 'react-native';
import type {
  NativeMapViewEvent,
  NativeMapViewProps,
} from '@mapconductor/js-sdk-react/internal';

// 共通のブリッジ props / イベント型は js-sdk-react に集約してある。
export type NativeArcGISMapViewEvent<T> = NativeMapViewEvent<T>;

/** プロバイダ固有のネイティブ props。共通部は NativeMapViewProps 側にある。 */
export interface NativeArcGISMapViewProps extends NativeMapViewProps {
  apiKey?: string;
}

export {
  toNativeCameraPosition,
  toNativeMarkerTilingOptions,
  type NativeMarkerTilingOptions,
} from '@mapconductor/js-sdk-react/internal';

export default requireNativeComponent<NativeArcGISMapViewProps>(
  // Align to android/src/main/java/com/mapconductor/react/arcgis/ArcGISMapViewManager.kt
  // (REACT_CLASS) and ios/MapConductorArcGISViewManager.m (RCT_EXPORT_MODULE)
  'ArcGISMapView'
);
