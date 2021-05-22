package com.ndma.livelihoodzones.ui.county.model

data class CountyModel(
    val countyId: Int,
    val countyName: String,
    val countyCode: String,
    val subCounties: MutableList<SubCountyModel>
)