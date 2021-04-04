package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.QuestionnaireSessionLocation
import com.silasonyango.ndma.ui.wealthgroup.responses.FoodConsumptionResponses
import com.silasonyango.ndma.ui.wealthgroup.responses.IncomeAndFoodSourceResponses
import com.silasonyango.ndma.ui.wealthgroup.responses.LivestockPoultryOwnershipResponses


class WealthGroupQuestionnaire(val uniqueId: String,val questionnaireName: String){
    lateinit var questionnaireStartDate: String
    lateinit var questionnaireEndDate: String
    lateinit var questionnaireGeography: QuestionnaireSessionLocation
    lateinit var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses
    lateinit var foodConsumptionResponses: FoodConsumptionResponses
    lateinit var livestockPoultryOwnershipResponses: LivestockPoultryOwnershipResponses
}