package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.*
import com.silasonyango.ndma.ui.county.responses.*
import com.silasonyango.ndma.ui.model.QuestionnaireStatus

class CountyLevelQuestionnaire(val uniqueId: String, var questionnaireName: String){
    var hasBeenSubmitted: Boolean = false

    var questionnaireStatus: QuestionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE

    var selectedLivelihoodZone: LivelihoodZoneModel? = null

    var countyLivelihoodZones: MutableList<LivelihoodZoneModel> = ArrayList()

    var livelihoodZoneCrops: MutableList<CropModel> = ArrayList()

    var selectedCrops: MutableList<CropModel> = ArrayList()

    var livelihoodZoneEthnicGroups: MutableList<EthnicGroupModel> = ArrayList()

    var definedMarkets: MutableList<DefinedMarketModel> = ArrayList()

    var marketTransactionItems: MutableList<MarketTransactionsItem> = ArrayList()

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

    var lzCropProductionResponses: LzCropProductionResponses =  LzCropProductionResponses()
}