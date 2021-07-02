package com.ndma.livelihoodzones.appStore.model

import com.ndma.livelihoodzones.ui.wealthgroup.model.FgdParticipantModel
import com.ndma.livelihoodzones.ui.wealthgroup.responses.*

class WealthGroupDraft {
    var incomeAndFoodSourceResponses: IncomeAndFoodSourceResponses? = null

    var foodConsumptionResponses: FoodConsumptionResponses? = null

    var livestockPoultryOwnershipResponses: LivestockPoultryOwnershipResponses? = null

    var livestockContributionResponses: LivestockContributionResponses? = null

    var labourPatternResponse: LabourPatternResponse? = null

    var expenditurePatternsResponses: ExpenditurePatternsResponses? = null

    var migrationPatternResponses: MigrationPatternResponses? = null

    var constraintResponses: ConstraintsResponses? = null

    var copingStrategiesResponses: CopingStrategiesResponses? = null

    var fdgParticipantsModelList: MutableList<FgdParticipantModel> = ArrayList()
}