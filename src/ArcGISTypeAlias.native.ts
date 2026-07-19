import type React from 'react';
import type { HostComponent, NativeMethods } from 'react-native';
import type { NativeArcGISMapViewProps } from './ArcGISMapViewNativeComponent';

export type ArcGISMapViewRef =
  React.ComponentRef<HostComponent<NativeArcGISMapViewProps>> & NativeMethods;
export type ArcGISMapMapView = ArcGISMapViewRef | null;
export type ArcGISMapMap = null;
