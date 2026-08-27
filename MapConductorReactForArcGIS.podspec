require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name = "MapConductorReactForArcGIS"
  s.version = package["version"]
  s.summary = package["description"]
  s.license = package["license"]
  s.author = package["author"]
  s.homepage = "https://github.com/mapconductor/react-sdk"
  s.source = { :path => __dir__ }
  # ArcGIS Maps SDK for Swift 300.x requires iOS 18 (see MapConductorForArcGIS.podspec).
  s.platforms = { :ios => "18.0" }
  s.source_files = "ios/*.{h,m,mm,swift}"
  # MapConductorForArcGIS is a source pod (see ios-sdk/ios-for-arcgis's podspec). Esri publishes
  # no podspec of its own, so that pod depends on an `ArcGIS` pod defined by a metadata-only
  # spec living next to it, whose :http source is Esri's own CDN URL taken verbatim from Esri's
  # Package.swift. CocoaPods downloads Esri's binary straight into the consuming app; neither this
  # package nor MapConductorForArcGIS ever vendors or redistributes it.
  #
  # Because Esri has no spec repo, apps must tell CocoaPods where that spec is - see this repo's
  # examples/reactnative-basic/ios/Podfile for the one line involved.
  s.dependency "React-Core"
  s.dependency "MapConductorReactNativeCore"
  s.dependency "MapConductorReactMarkerClustering"
  s.dependency "MapConductorForArcGIS", "~> 1.3.0"
end
