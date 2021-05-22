package com.ndma.livelihoodzones.ui.county.model

data class CropModel(val cropId: Int, val cropName: String, val cropCode: Int){
    var hasBeenSelected: Boolean = false
}