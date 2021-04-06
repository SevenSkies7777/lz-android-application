package com.silasonyango.ndma.login.model

import com.silasonyango.ndma.ui.county.model.*

data class GeographyObject(
    val county: CountyModel,
    var livelihoodZones: MutableList<LivelihoodZoneModel>,
    val subLocations: MutableList<SubLocationModel>
)