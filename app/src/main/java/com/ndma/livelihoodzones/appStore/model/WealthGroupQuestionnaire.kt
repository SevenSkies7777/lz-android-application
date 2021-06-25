package com.ndma.livelihoodzones.appStore.model

import com.ndma.livelihoodzones.ui.county.model.CropModel
import com.ndma.livelihoodzones.ui.county.model.QuestionnaireSessionLocation
import com.ndma.livelihoodzones.ui.model.QuestionnaireStatus
import com.ndma.livelihoodzones.ui.wealthgroup.model.FgdParticipantModel
import com.ndma.livelihoodzones.ui.wealthgroup.responses.*


class WealthGroupQuestionnaire(val uniqueId: String, var questionnaireName: String){
    var hasBeenSubmitted: Boolean = false
    var questionnaireStatus: QuestionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE
    lateinit var questionnaireStartDate: String
    lateinit var questionnaireEndDate: String
    var lastQuestionnaireStep: Int = 0
    val questionnaireCoveredSteps: MutableList<Int> = ArrayList()
    lateinit var questionnaireGeography: QuestionnaireSessionLocation
    lateinit var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses
    var cropContributionResponseItems: MutableList<WgCropContributionResponseItem> =
        ArrayList()
    lateinit var foodConsumptionResponses: FoodConsumptionResponses
    lateinit var livestockPoultryOwnershipResponses: LivestockPoultryOwnershipResponses
    lateinit var livestockContributionResponses: LivestockContributionResponses
    lateinit var labourPatternResponses: LabourPatternResponse
    lateinit var expenditurePatternsResponses: ExpenditurePatternsResponses
    lateinit var migrationPatternResponses: MigrationPatternResponses
    lateinit var constraintsResponses: ConstraintsResponses
    var selectedCrops: MutableList<CropModel> = ArrayList()
    lateinit var copingStrategiesResponses: CopingStrategiesResponses
    var fdgParticipants: MutableList<FgdParticipantModel> = ArrayList()
    var draft = WealthGroupDraft()
}