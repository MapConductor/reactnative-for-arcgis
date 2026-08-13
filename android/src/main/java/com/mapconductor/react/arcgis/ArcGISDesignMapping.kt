package com.mapconductor.react.arcgis

import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISDesignTypeInterface

/**
 * JS 側 `@mapconductor/react-for-arcgis` の `ArcGISDesign` カタログとネイティブの
 * `com.mapconductor.arcgis.ArcGISDesign` カタログは同じ id（"arc_gis_streets" など）を
 * 使うため、変換表は持たずに id をそのままネイティブの `Create` へ渡す。
 * 未知の id（旧 id や誤記）は Streets へフォールバックする。
 */
object ArcGISReactNativeDesign {
    fun from(value: String?): ArcGISDesignTypeInterface =
        value?.let { id -> runCatching { ArcGISDesign.Create(id) }.getOrNull() }
            ?: ArcGISDesign.Streets
}
