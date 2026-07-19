package com.mapconductor.react.arcgis

import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISDesignTypeInterface

/**
 * Maps the JS-side design ids from `@mapconductor/react-for-arcgis`'s `ArcGISDesign` catalog
 * (web/RN-facing, e.g. "streets", "dark-gray") onto the native `com.mapconductor.arcgis.ArcGISDesign`
 * catalog, which uses a much larger set of Esri/OSM basemap style ids. A handful of the web ids
 * ("national-geographic", "dark-matter", "positron") have no exact native counterpart - Esri
 * retired the National Geographic style, and "dark-matter"/"positron" are CARTO basemap names, not
 * Esri ones - so those fall back to the closest available style.
 */
object ArcGISReactNativeDesign {
    fun from(value: String?): ArcGISDesignTypeInterface =
        when (value) {
            "streets" -> ArcGISDesign.Streets
            "satellite" -> ArcGISDesign.Imagery
            "hybrid" -> ArcGISDesign.ImageryLabels
            "topo" -> ArcGISDesign.Topographic
            "gray" -> ArcGISDesign.LightGray
            "dark-gray" -> ArcGISDesign.DarkGray
            "oceans" -> ArcGISDesign.Oceans
            "national-geographic" -> ArcGISDesign.Outdoor
            "osm" -> ArcGISDesign.OsmStandard
            "dark-matter" -> ArcGISDesign.OsmDarkGray
            "positron" -> ArcGISDesign.OsmLightGray
            else -> ArcGISDesign.Streets
        }
}
