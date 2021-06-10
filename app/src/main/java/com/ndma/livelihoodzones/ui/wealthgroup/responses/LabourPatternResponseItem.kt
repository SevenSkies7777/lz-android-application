package com.ndma.livelihoodzones.ui.wealthgroup.responses

data class LabourPatternResponseItem(
    var women: Double,
    var men: Double,
    var extraDescription: String = ""
)