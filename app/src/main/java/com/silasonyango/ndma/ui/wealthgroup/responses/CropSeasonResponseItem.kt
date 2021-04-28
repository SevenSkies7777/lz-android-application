package com.silasonyango.ndma.ui.wealthgroup.responses

data class CropSeasonResponseItem(
    var rainfedCultivatedAreaPercentage: CropProductionResponseValueModel,
    var rainfedAverageYieldPerHa: CropProductionResponseValueModel,
    var irrigatedCultivatedArea: CropProductionResponseValueModel,
    var irrigatedAverageYieldPerHa: CropProductionResponseValueModel
)