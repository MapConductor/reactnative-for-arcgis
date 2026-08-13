import { ReactNativeBridgeMapViewController } from '@mapconductor/js-sdk-react/internal';
import type { ArcGISMapViewRef } from './ArcGISTypeAlias.native';

/**
 * ネイティブブリッジの実装は全 RN プロバイダで同一なので
 * {@link ReactNativeBridgeMapViewController} に集約してある。ここはネイティブビューの
 * ref 型を与えるだけ。プロバイダ固有の振る舞いが要るときだけメソッドを override する。
 */
export class ArcGISMapViewController extends ReactNativeBridgeMapViewController<ArcGISMapViewRef> {}
