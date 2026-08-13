import type React from 'react';
import type { MarkerTilingOptions } from '@mapconductor/js-sdk-core';
import type { MapViewBaseProps } from '@mapconductor/js-sdk-react/native';
import type { ArcGISMapViewStateInterface } from '@mapconductor/react-for-arcgis/state';

export interface ArcGISMapViewProps extends MapViewBaseProps<ArcGISMapViewStateInterface> {
  className?: string;
  onError?: (error: Error) => void;
  children?: React.ReactNode;
  markerTilingOptions?: MarkerTilingOptions;
}
