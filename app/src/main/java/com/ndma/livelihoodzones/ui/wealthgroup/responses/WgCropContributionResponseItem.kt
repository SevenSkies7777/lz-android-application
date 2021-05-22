package com.ndma.livelihoodzones.ui.wealthgroup.responses

import com.ndma.livelihoodzones.ui.county.model.CropModel
import com.ndma.livelihoodzones.ui.model.CropContributionResponseValue

data class WgCropContributionResponseItem(
    val cropModel: CropModel,
    var cashIncomeRank: CropContributionResponseValue,
    var cashIncomeApproxPercentage: CropContributionResponseValue,
    var foodConsumptionRank: CropContributionResponseValue,
    var foodConsumptionApproxPercentage: CropContributionResponseValue
)