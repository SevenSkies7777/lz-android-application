package com.silasonyango.ndma.ui.county.responses

import com.silasonyango.ndma.ui.county.model.CropModel

data class LzCropProductionResponseItem(
    val crop: CropModel,
    val rainfedCultivatedAreaPercentage: Double,
    val rainfedAverageYieldPerHa: Double,
    val irrigatedCultivatedArea: Double,
    val irrigatedAverageYieldPerHa: Double
)