package com.ndma.livelihoodzones.ui.wealthgroup.responses

import com.ndma.livelihoodzones.ui.county.model.CropModel
import com.ndma.livelihoodzones.ui.county.model.MonthsModel

data class WgCropProductionResponseItem(
    val crop: CropModel,
    val longRainsSeason: CropSeasonResponseItem,
    val shortRainsSeason: CropSeasonResponseItem,
    var allFieldsFilled: Boolean = false
) {
    var landPreparationPeriod: MutableList<MonthsModel> = ArrayList()
    var plantingPeriod: MutableList<MonthsModel> = ArrayList()
    var harvestingPeriod: MutableList<MonthsModel> = ArrayList()
}