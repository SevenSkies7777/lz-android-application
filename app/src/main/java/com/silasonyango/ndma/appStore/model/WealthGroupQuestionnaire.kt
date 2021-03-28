package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.wealthgroup.responses.IncomeAndFoodSourceResponses


class WealthGroupQuestionnaire(val uniqueId: String,val questionnaireName: String){
    lateinit var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses
}