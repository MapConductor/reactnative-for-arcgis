import { requireNativeComponent } from 'react-native';
import type { NativeArcGISMapViewProps } from './ArcGISMapViewNativeComponent';

// 2D（`MapView`）用のネイティブビュー。3D（`SceneView`）とは props が同じでも
// ネイティブ側の実体が別なので、ビュー名も別に取る。
export default requireNativeComponent<NativeArcGISMapViewProps>(
  // Align to android/src/main/java/com/mapconductor/react/arcgis/ArcGISMapViewManager.kt
  // (ArcGISMapView2DViewManager.REACT_CLASS) and ios/MapConductorArcGISViewManager.m
  // (RCT_EXPORT_MODULE of MapConductorArcGIS2DViewManager)
  'ArcGISMapView2D'
);
