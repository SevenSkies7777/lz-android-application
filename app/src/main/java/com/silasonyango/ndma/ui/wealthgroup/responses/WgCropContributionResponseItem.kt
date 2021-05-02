package com.silasonyango.ndma.ui.wealthgroup.responses

import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.model.CropContributionResponseValue

data class WgCropContributionResponseItem(
    val cropModel: CropModel,
    var cashIncomeRank: CropContributionResponseValue,
    var cashIncomeApproxPercentage: CropContributionResponseValue,
    var foodConsumptionRank: CropContributionResponseValue,
    var foodConsumptionApproxPercentage: CropContributionResponseValue
)