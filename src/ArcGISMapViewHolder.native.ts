import type React from 'react';
import { MapViewHolderBase } from '@mapconductor/js-sdk-core';
import type { GeoPoint, Offset } from '@mapconductor/js-sdk-core';
import type { ArcGISMapViewRef } from './ArcGISTypeAlias.native';

export class ArcGISMapViewHolder extends MapViewHolderBase<ArcGISMapViewRef | null, null> {
  readonly map = null;

  constructor(private readonly nativeRef: React.RefObject<ArcGISMapViewRef | null>) {
    super();
  }

  get mapView(): ArcGISMapViewRef | null {
    return this.nativeRef.current;
  }

  toScreenOffset(_position: GeoPoint): null {
    return null;
  }

  fromScreenOffsetSync(_offset: Offset): GeoPoint | null {
    return null;
  }
}
