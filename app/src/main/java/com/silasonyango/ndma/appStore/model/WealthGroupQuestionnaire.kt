package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.QuestionnaireSessionLocation
import com.silasonyango.ndma.ui.wealthgroup.responses.IncomeAndFoodSourceResponses


class WealthGroupQuestionnaire(val uniqueId: String,val questionnaireName: String){
    lateinit var questionnaireStartDate: String
    lateinit var questionnaireEndDate: String
    lateinit var questionnaireGeography: QuestionnaireSessionLocation
    lateinit var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses
}