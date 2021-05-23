package com.ndma.livelihoodzones.appStore.model

import com.ndma.livelihoodzones.ui.county.model.*
import com.ndma.livelihoodzones.ui.county.responses.*
import com.ndma.livelihoodzones.ui.model.QuestionnaireStatus

class CountyLevelQuestionnaire(val uniqueId: String, var questionnaireName: String){
    var hasBeenSubmitted: Boolean = false

    var lastQuestionnaireStep: Int = 0

    var questionnaireStatus: QuestionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE

    var selectedLivelihoodZone: LivelihoodZoneModel? = null

    var countyLivelihoodZones: MutableList<LivelihoodZoneModel> = ArrayList()

    lateinit var wealthGroupCharectariticsResponses: WealthGroupCharectaristicsResponses

    var livelihoodZoneCrops: MutableList<CropModel> = ArrayList()

    var selectedCrops: MutableList<CropModel> = ArrayList()

    var livelihoodZonesCharectaristics: MutableList<ZoneCharectaristicsResponseItem> = ArrayList()

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