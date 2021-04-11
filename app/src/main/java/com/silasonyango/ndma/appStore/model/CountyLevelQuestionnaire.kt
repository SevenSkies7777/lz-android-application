package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.*
import com.silasonyango.ndma.ui.county.responses.*

class CountyLevelQuestionnaire(val uniqueId: String,val questionnaireName: String){
    lateinit var selectedLivelihoodZone: LivelihoodZoneModel

    var countyLivelihoodZones: MutableList<LivelihoodZoneModel> = ArrayList()

    var livelihoodZoneCrops: MutableList<CropModel> = ArrayList()

    var livelihoodZoneEthnicGroups: MutableList<EthnicGroupModel> = ArrayList()

    var definedMarkets: MutableList<DefinedMarketModel> = ArrayList()

    var latitude: Double = 0.0

    var longitude: Double = 0.0

    var subLocationZoneAllocationList: MutableList<SubLocationZoneAssignmentModel> = ArrayList()

    lateinit var questionnaireStartDate: String

    lateinit var questionnaireEndDate: String

    lateinit var wealthGroupResponse: WealthGroupResponse

    lateinit var waterSourceResponses: WaterSourcesResponses

    lateinit var hungerPatternsResponses: HungerPatternsResponses

    lateinit var hazardResponses: HazardResponses

    lateinit var livelihoodZoneSeasonsResponses: LzSeasonsResponses
}