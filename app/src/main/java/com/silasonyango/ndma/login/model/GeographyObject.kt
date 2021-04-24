package com.silasonyango.ndma.login.model

import com.silasonyango.ndma.ui.county.model.*

data class GeographyObject(
    val county: CountyModel,
    var livelihoodZones: MutableList<LivelihoodZoneModel>,
    val subLocations: MutableList<SubLocationModel>,
    val crops: MutableList<CropModel>,
    val ethnicGroups: MutableList<EthnicGroupModel>,
    val months: MutableList<MonthsModel>,
    val subCounties: MutableList<SubCountyModel>,
    val sublocationsLivelihoodZoneAssignments: MutableList<SubLocationsLivelihoodZoneAssignmentsModel>
)