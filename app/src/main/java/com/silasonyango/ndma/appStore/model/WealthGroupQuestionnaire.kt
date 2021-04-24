package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.QuestionnaireSessionLocation
import com.silasonyango.ndma.ui.model.QuestionnaireStatus
import com.silasonyango.ndma.ui.wealthgroup.responses.*


class WealthGroupQuestionnaire(val uniqueId: String, var questionnaireName: String){
    var hasBeenSubmitted: Boolean = false
    var questionnaireStatus: QuestionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE
    lateinit var questionnaireStartDate: String
    lateinit var questionnaireEndDate: String
    lateinit var questionnaireGeography: QuestionnaireSessionLocation
    lateinit var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses
    lateinit var foodConsumptionResponses: FoodConsumptionResponses
    lateinit var livestockPoultryOwnershipResponses: LivestockPoultryOwnershipResponses
    lateinit var livestockContributionResponses: LivestockContributionResponses
    lateinit var labourPatternResponses: LabourPatternResponse
    lateinit var expenditurePatternsResponses: ExpenditurePatternsResponses
    lateinit var migrationPatternResponses: MigrationPatternResponses
    lateinit var constraintsResponses: ConstraintsResponses
}