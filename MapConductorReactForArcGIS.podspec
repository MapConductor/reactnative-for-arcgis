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
  s.platforms = { :ios => "17.0" }
  s.source_files = "ios/*.{h,m,mm,swift}"
  # MapConductorForArcGIS is a source pod (see ios-sdk/ios-for-arcgis's podspec) that itself
  # depends on the real, officially-published `ArcGIS` pod - CocoaPods installs Esri's own
  # binary directly into the consuming app, so neither this package nor MapConductorForArcGIS
  # ever vendors or redistributes it.
  s.dependency "React-Core"
  s.dependency "MapConductorReactNativeCore"
  s.dependency "MapConductorReactMarkerClustering"
  s.dependency "MapConductorForArcGIS"
end
