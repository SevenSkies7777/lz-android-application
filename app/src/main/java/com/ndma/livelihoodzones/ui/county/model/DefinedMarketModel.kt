package com.ndma.livelihoodzones.ui.county.model

class DefinedMarketModel(
    val marketName: String,
    val subCountyModel: SubCountyModel,
    val nearestVillageOrTown: String,
    val marketUniqueId: String
)