package com.ndma.livelihoodzones.ui.county.responses

import com.ndma.livelihoodzones.ui.county.model.CropModel

data class LzCropProductionResponseItem(
    val crop: CropModel,
    val rainfedCultivatedAreaPercentage: Double,
    val rainfedAverageYieldPerHa: Double,
    val irrigatedCultivatedArea: Double,
    val irrigatedAverageYieldPerHa: Double
)