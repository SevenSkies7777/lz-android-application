package com.ndma.livelihoodzones.ui.wealthgroup.responses

data class LivestockContributionResponseItem(
    val incomeRank: Int,
    val incomePercentage: Double,
    val consumptionRank: Int,
    val consumptionPercentage: Double
)