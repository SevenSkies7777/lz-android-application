package com.ndma.livelihoodzones.ui.county.model

class SubCountyMarkets(val subCountyModel: SubCountyModel, val nearestVillageOrTown: NearestVillageOrTown) {
    val marketModelList: MutableList<MarketModel> = ArrayList()
}