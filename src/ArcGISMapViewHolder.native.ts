import type React from 'react';
import type { GeoPoint, MapViewHolder, Offset } from '@mapconductor/js-sdk-core';
import type { ArcGISMapViewRef } from './ArcGISTypeAlias.native';

export class ArcGISMapViewHolder implements MapViewHolder<ArcGISMapViewRef | null, null> {
  readonly map = null;

  constructor(private readonly nativeRef: React.RefObject<ArcGISMapViewRef | null>) {}

  get mapView(): ArcGISMapViewRef | null {
    return this.nativeRef.current;
  }

  toScreenOffset(_position: GeoPoint): null {
    return null;
  }

  async fromScreenOffset(_offset: Offset): Promise<GeoPoint | null> {
    return null;
  }

  fromScreenOffsetSync(_offset: Offset): GeoPoint | null {
    return null;
  }
}
