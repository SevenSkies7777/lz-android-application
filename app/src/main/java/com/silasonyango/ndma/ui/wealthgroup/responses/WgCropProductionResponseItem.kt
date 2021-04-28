package com.silasonyango.ndma.ui.wealthgroup.responses

import com.silasonyango.ndma.ui.county.model.CropModel

data class WgCropProductionResponseItem(
    val crop: CropModel,
    var rainfedCultivatedAreaPercentage: CropProductionResponseValueModel,
    var rainfedAverageYieldPerHa: CropProductionResponseValueModel,
    var irrigatedCultivatedArea: CropProductionResponseValueModel,
    var irrigatedAverageYieldPerHa: CropProductionResponseValueModel
)