package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.LivelihoodZoneModel
import com.silasonyango.ndma.ui.county.responses.HazardResponses
import com.silasonyango.ndma.ui.county.responses.HungerPatternsResponses
import com.silasonyango.ndma.ui.county.responses.WaterSourcesResponses
import com.silasonyango.ndma.ui.county.responses.WealthGroupResponse

class CountyLevelQuestionnaire(val uniqueId: String,val questionnaireName: String){
    lateinit var selectedLivelihoodZone: LivelihoodZoneModel

    val countyLivelihoodZones: MutableList<LivelihoodZoneModel> = ArrayList()

    var latitude: Double = 0.0

    var longitude: Double = 0.0

    lateinit var questionnaireStartDate: String

    lateinit var questionnaireEndDate: String

    lateinit var wealthGroupResponse: WealthGroupResponse

    lateinit var waterSourceResponses: WaterSourcesResponses

    lateinit var hungerPatternsResponses: HungerPatternsResponses

    lateinit var hazardResponses: HazardResponses
}