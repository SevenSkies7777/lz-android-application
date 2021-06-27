package com.ndma.livelihoodzones.appStore.model

import com.ndma.livelihoodzones.ui.wealthgroup.responses.FoodConsumptionResponses
import com.ndma.livelihoodzones.ui.wealthgroup.responses.IncomeAndFoodSourceResponses
import com.ndma.livelihoodzones.ui.wealthgroup.responses.LivestockContributionResponses
import com.ndma.livelihoodzones.ui.wealthgroup.responses.LivestockPoultryOwnershipResponses

class WealthGroupDraft {
    var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses? = null

    var foodConsumptionResponses: FoodConsumptionResponses? = null

    var livestockPoultryOwnershipResponses: LivestockPoultryOwnershipResponses? = null

    var livestockContributionResponses: LivestockContributionResponses? = null
}