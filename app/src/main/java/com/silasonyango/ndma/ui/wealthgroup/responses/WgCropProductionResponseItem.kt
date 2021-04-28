package com.silasonyango.ndma.ui.wealthgroup.responses

import com.silasonyango.ndma.ui.county.model.CropModel

data class WgCropProductionResponseItem(
    val crop: CropModel,
    val longRainsSeason: CropSeasonResponseItem,
    val shortRainsSeason: CropSeasonResponseItem,
    var allFieldsFilled: Boolean = false
)