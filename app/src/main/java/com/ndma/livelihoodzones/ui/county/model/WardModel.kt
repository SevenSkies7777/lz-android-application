package com.ndma.livelihoodzones.ui.county.model

data class WardModel(
    val wardId: Int,
    val subCountyId: Int,
    val wardName: String,
    val wardCode: Int,
    val subLocations: MutableList<SubLocationModel>
)