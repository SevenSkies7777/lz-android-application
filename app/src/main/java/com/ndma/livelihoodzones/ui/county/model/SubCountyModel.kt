package com.ndma.livelihoodzones.ui.county.model

class SubCountyModel(
    val subCountyId: Int,
    val countyId: Int,
    val subCountyName: String,
    val subCountyCode: Int,
    val wards: MutableList<WardModel>
)