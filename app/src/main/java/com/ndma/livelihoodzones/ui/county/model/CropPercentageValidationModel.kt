package com.ndma.livelihoodzones.ui.county.model

data class CropPercentageValidationModel(
    val hasAPercentageError: Boolean,
    val variationStatus: PercentageValidationEnum,
    val differenceValue: Double
)