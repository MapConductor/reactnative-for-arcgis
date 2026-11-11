import assert from 'node:assert/strict';
import { after, test } from 'node:test';
import { createGeoPoint, createMapCameraPosition } from '@mapconductor/js-sdk-core';
import { buildPackage } from './helpers/buildPackage.mjs';

// ArcGISMapView（3D, SceneView）と ArcGISMapView2D（2D, MapView）は、共有の
// NativeMapViewHost に「どのネイティブビューか」「デザインの id」「apiKey」を渡すだけの層。
// React を描画せず関数として呼び、返ってきた要素の props で受け渡しを確かめる。
const { mod, cleanup } = await buildPackage('mapview-tests');
after(cleanup);
const { ArcGISMapView, ArcGISMapView2D, ArcGISDesign, ArcGISMapViewState, ArcGISMapViewController, NativeMapViewHost, ReactNativeBridgeMapViewController } = mod;

// 両ビューとも内部の ArcGISNativeMapView を 1 段挟むので、もう 1 段展開して host 要素を得る。
function hostOf(element) {
  const inner = element.type(element.props);
  assert.equal(inner.type, NativeMapViewHost);
  return inner;
}

test('the unadorned ArcGISMapView is the 3D SceneView, ArcGISMapView2D the 2D MapView', () => {
  const state = new ArcGISMapViewState();
  // Android の ViewManager.REACT_CLASS / iOS の RCT_EXPORT_MODULE と揃っていないと
  // 「View config not found」で落ちる名前。
  assert.deepEqual(hostOf(ArcGISMapView({ state })).props.nativeComponent, { nativeViewName: 'ArcGISMapView' });
  assert.deepEqual(hostOf(ArcGISMapView2D({ state })).props.nativeComponent, { nativeViewName: 'ArcGISMapView2D' });
});

test('the design goes over as its id and follows a design change', () => {
  const state = new ArcGISMapViewState();
  assert.equal(hostOf(ArcGISMapView({ state })).props.mapDesignValue, 'arc_gis_streets');
  state.mapDesignType = ArcGISDesign.Imagery;
  assert.equal(hostOf(ArcGISMapView({ state })).props.mapDesignValue, 'arc_gis_imagery');
  assert.equal(hostOf(ArcGISMapView2D({ state })).props.mapDesignValue, 'arc_gis_imagery');
});

test('the API key from the state is handed to the native view in both 2D and 3D', () => {
  const state = new ArcGISMapViewState({ apiKey: 'AAPK-test' });
  assert.deepEqual(hostOf(ArcGISMapView({ state })).props.nativeProps, { apiKey: 'AAPK-test' });
  assert.deepEqual(hostOf(ArcGISMapView2D({ state })).props.nativeProps, { apiKey: 'AAPK-test' });
  assert.deepEqual(hostOf(ArcGISMapView({ state: new ArcGISMapViewState() })).props.nativeProps, { apiKey: '' });
});

test('app props pass through to the host, but the internal nativeComponent prop is not overridable', () => {
  const state = new ArcGISMapViewState();
  const onMapClick = () => {};
  const host = hostOf(ArcGISMapView({ state, onMapClick, children: 'child', nativeComponent: 'ignored' }));
  assert.equal(host.props.state, state);
  assert.equal(host.props.onMapClick, onMapClick);
  assert.equal(host.props.children, 'child');
  assert.deepEqual(host.props.nativeComponent, { nativeViewName: 'ArcGISMapView' });
});

test('createController builds an ArcGISMapViewController bound to the native ref and initial camera', () => {
  const state = new ArcGISMapViewState();
  const view = { nodeHandle: 7 };
  const camera = createMapCameraPosition({ position: createGeoPoint({ latitude: 34.05, longitude: -118.24 }), zoom: 12 });
  for (const View of [ArcGISMapView, ArcGISMapView2D]) {
    const controller = hostOf(View({ state })).props.createController({ current: view }, camera);
    assert.ok(controller instanceof ArcGISMapViewController);
    assert.ok(controller instanceof ReactNativeBridgeMapViewController);
    assert.equal(controller.getCameraPosition(), camera);
    assert.equal(controller.holder.mapView, view);
    controller.destroy();
  }
});
