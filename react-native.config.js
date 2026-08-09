module.exports = {
  dependency: {
    platforms: {
      android: {
        sourceDir: './android',
        packageImportPath:
          'import com.mapconductor.react.arcgis.MapConductorArcGISPackage;',
        packageInstance: 'new MapConductorArcGISPackage()',
      },
      ios: {
        sourceDir: './ios',
      },
    },
  },
};
