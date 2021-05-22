package com.ndma.livelihoodzones.ui.wealthgroup.responses

import com.ndma.livelihoodzones.ui.county.model.CropModel

data class WgCropProductionResponseItem(
    val crop: CropModel,
    val longRainsSeason: CropSeasonResponseItem,
    val shortRainsSeason: CropSeasonResponseItem,
    var allFieldsFilled: Boolean = false
)