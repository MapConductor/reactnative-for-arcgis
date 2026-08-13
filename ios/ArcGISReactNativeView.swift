import MapConductorCore
import MapConductorForArcGIS
import MapConductorReactMarkerClustering
import MapConductorReactNativeCore
import SwiftUI
import UIKit

/// RN の ArcGIS ビュー。共通の処理は ``MCReactNativeMapViewBase`` にあるので、
/// ここは ArcGIS 固有のアダプタと、API キーの prop だけ。
///
/// ArcGIS だけは中で SwiftUI を使う。Esri の `MapView` は `UIViewRepresentable` ではなく
/// SwiftUI ビューそのもので、UIKit から直接組み立てる経路が SDK 側に無いため。
/// 基底から見れば「`UIView` を返して `MapViewContent` を受け取る」点は他と同じなので、
/// コマンド処理・マーカー取り込み・スクリーン座標の通知は共通のものがそのまま効く。
@objc(MCArcGISReactNativeView)
public final class ArcGISReactNativeView: MCReactNativeMapViewBase {
    private let arcGISHost = ArcGISReactNativeHost()

    public override func makeHost() -> MCReactNativeMapHost { arcGISHost }

    @objc public func setApiKey(_ value: String?) {
        let apiKey = value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !apiKey.isEmpty else { return }
        arcGISHost.apiKey = apiKey
        // キーが揃ったので次のレイアウトで地図を作らせる。
        setNeedsLayout()
    }
}

/// `ArcGISMapView2D`（SwiftUI）を RN の基底クラスが扱える非ジェネリックな形へ翻訳する。
@MainActor
final class ArcGISReactNativeHost: MCReactNativeMapHost {
    weak var mcDelegate: MCReactNativeMapHostDelegate?

    var apiKey = ""

    private let model = ArcGISContentModel()
    private var state: ArcGISMapViewState { model.state }

    var mcServiceRegistry: MutableMapServiceRegistry { state.serviceRegistry }
    var mcCameraZoom: Double { state.cameraPosition.zoom }

    /// キー未設定では ArcGIS の初期化が通らないので、揃うまで地図を作らせない。
    var mcIsReady: Bool { !apiKey.isEmpty }
    var mcNotReadyMessage: String? { "ArcGIS API key is not configured. Pass an `apiKey` prop to <ArcGISMapView />." }

    func mcMakeMapView(content: MapViewContent) -> UIView {
        let controller = UIHostingController(
            rootView: ArcGISReactNativeRoot(model: model, host: self)
        )
        controller.view.backgroundColor = .clear
        return controller.view
    }

    /// 実際の組み立ては `ArcGISMapView2D` の content クロージャの中で行う
    /// （プロバイダの contentPass の内側に入れないとクラスタリングが切断される）。
    /// ここでは SwiftUI に再評価を促すだけで、渡された値は使わない。
    func mcUpdateContent(_ content: MapViewContent) {
        model.revision &+= 1
    }

    /// デザインとジェスチャは state 経由で SwiftUI が拾うので、ここでやることは無い。
    func mcSyncNativeViewSettings() {}

    func mcUnbind() {}

    func mcSetMapDesign(id: String?) {
        // ArcGISDesign カタログは JS と同じ id を使うので変換表は持たない。
        // Create は未知 id もそのまま包み、toBasemapStyle の default が Streets に解決する。
        state.mapDesignType = id.map { ArcGISDesign.Create(id: $0) } ?? ArcGISDesign.Streets
    }

    func mcMoveCamera(_ camera: MapCameraPosition, durationMillis: Int64?) {
        if let durationMillis {
            state.moveCameraTo(cameraPosition: camera, durationMillis: durationMillis)
        } else {
            state.moveCameraTo(cameraPosition: camera)
        }
    }

    func mcFitBounds(_ bounds: GeoRectBounds, padding: Int) {
        state.fitBounds(bounds: bounds, padding: padding)
    }

    func mcApplyUISettings(_ settings: MapUISettings) {
        state.uiSettings = settings
    }

    func mcToScreenOffset(_ position: GeoPointProtocol) -> CGPoint? {
        state.getMapViewHolder()?.toScreenOffset(position: position)
    }

    func mcMakeLocalExtensionRenderer(
        type: String,
        extensionId: String,
        eventSink: @escaping NativeMapExtensionEventSink
    ) -> NativeMapExtensionRenderer? {
        guard type == "marker-clustering" else { return nil }
        return MarkerClusterExtensionRenderer<ArcGISActualMarker>(extensionId: extensionId, eventSink: eventSink)
    }

    fileprivate func initializeSdk() {
        _ = arcGISApiKeyInitialize(apiKey: apiKey)
    }
}

/// SwiftUI の再評価を促すだけの入れ物。content 自体は body の中で組み立てる。
private final class ArcGISContentModel: ObservableObject {
    let state = ArcGISMapViewState()
    /// 変わるたびに SwiftUI の body を再評価させるためだけのカウンタ。
    @Published var revision = 0
}

private struct ArcGISReactNativeRoot: View {
    @ObservedObject var model: ArcGISContentModel
    let host: ArcGISReactNativeHost

    var body: some View {
        // revision を読んで再評価の依存関係を作る。
        let _ = model.revision
        return ArcGISMapView2D(
            state: model.state,
            onMapLoaded: { _ in host.mcDelegate?.mcMapLoaded() },
            onMapClick: { host.mcDelegate?.mcMapClick($0) },
            onMapLongClick: { host.mcDelegate?.mcMapLongClick($0) },
            onCameraMoveStart: { host.mcDelegate?.mcCameraMoveStart($0) },
            onCameraMove: { host.mcDelegate?.mcCameraMove($0) },
            onCameraMoveEnd: { host.mcDelegate?.mcCameraMoveEnd($0) },
            sdkInitialize: { host.initializeSdk() },
            content: { host.mcDelegate?.mcAssembleContent() ?? MapViewContent() }
        )
    }
}
