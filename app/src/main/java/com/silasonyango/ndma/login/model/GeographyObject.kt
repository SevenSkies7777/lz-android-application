package com.silasonyango.ndma.login.model

import com.silasonyango.ndma.ui.county.model.SubCountyModel
import com.silasonyango.ndma.ui.county.model.SubLocationModel
import com.silasonyango.ndma.ui.county.model.WardModel

data class GeographyObject(
    val subCounties: List<SubCountyModel>,
    val wards: List<WardModel>,
    val subLocations: List<SubLocationModel>
)