package com.ndma.livelihoodzones.appStore.model

import com.ndma.livelihoodzones.ui.county.model.*
import com.ndma.livelihoodzones.ui.county.responses.*
import com.ndma.livelihoodzones.ui.model.QuestionnaireStatus

class CountyLevelQuestionnaire(val uniqueId: String, var questionnaireName: String){
    var hasBeenSubmitted: Boolean = false

    var lastQuestionnaireStep: Int = 0

    val questionnaireCoveredSteps: MutableList<Int> = ArrayList()

    var questionnaireStatus: QuestionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE

    var selectedLivelihoodZone: LivelihoodZoneModel? = null


    var countyLivelihoodZones: MutableList<LivelihoodZoneModel> = ArrayList()

    var wealthGroupCharectariticsResponses = WealthGroupCharectaristicsResponses()

    var livelihoodZoneCrops: MutableList<CropModel> = ArrayList()

    var selectedCrops: MutableList<CropModel> = ArrayList()

    var livelihoodZonesCharectaristics: MutableList<ZoneCharectaristicsResponseItem> = ArrayList()

    var livelihoodZoneEthnicGroups: MutableList<EthnicGroupModel> = ArrayList()

    var definedMarkets: MutableList<DefinedMarketModel> = ArrayList()

    var marketTransactionItems: MutableList<MarketTransactionsItem> = ArrayList()

    var ethnicGroupResponseList: MutableList<EthnicityResponseItem> =
        ArrayList()

    var latitude: Double = 0.0

    var longitude: Double = 0.0

    var subLocationZoneAllocationList: MutableList<SubLocationZoneAssignmentModel> = ArrayList()

    lateinit var questionnaireStartDate: String

    lateinit var questionnaireEndDate: String

    lateinit var wealthGroupResponse: WealthGroupResponse

    var waterSourceResponses: WaterSourcesResponses? = null

    var hungerPatternsResponses = HungerPatternsResponses(0.0,0.0,0.0,0.0)

    lateinit var hazardResponses: HazardResponses

    lateinit var livelihoodZoneSeasonsResponses: LzSeasonsResponses

    var lzCropProductionResponses: LzCropProductionResponses =  LzCropProductionResponses()

    var draft = CountyLevelDraft()


}