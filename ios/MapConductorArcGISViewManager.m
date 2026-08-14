#import "MapConductorArcGISViewManager.h"

/// API キーが揃うまで地図を作れないので、その prop だけプロバイダ固有で持つ。
@protocol MCArcGISApiKeyConfigurable <NSObject>
- (void)setApiKey:(NSString *)value;
@end

// 入れ物の生成・reactTag からの解決は MCReactNativeMapViewManagerBase（js-sdk-react/ios）。
@implementation MapConductorArcGISViewManager

RCT_EXPORT_MODULE(ArcGISMapView)

- (NSString *)mapViewClassName
{
  return @"MCArcGISReactNativeView";
}

RCT_CUSTOM_VIEW_PROPERTY(apiKey, NSString, MCReactNativeMapContainerView)
{
  [(id<MCArcGISApiKeyConfigurable>)view.mapView setApiKey:json];
}

MC_REACT_NATIVE_MAP_VIEW_MANAGER_BODY

@end

__attribute__((constructor)) static void MCArcGISRegisterLegacyInterop(void)
{
  MCReactNativeRegisterLegacyViewManagerInterop(@"ArcGISMapView");
}
