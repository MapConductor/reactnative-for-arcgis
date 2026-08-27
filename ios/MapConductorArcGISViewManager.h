#import <MapConductorReactNativeCore/MCReactNativeMapViewManager.h>

/// 3D（Esri の `SceneView`）。他プラットフォームと同じく無印が 3D。
@interface MapConductorArcGISViewManager : MCReactNativeMapViewManagerBase
@end

/// 2D（Esri の `MapView`）。
@interface MapConductorArcGIS2DViewManager : MCReactNativeMapViewManagerBase
@end
