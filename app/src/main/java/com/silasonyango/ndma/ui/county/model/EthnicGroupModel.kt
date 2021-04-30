package com.silasonyango.ndma.ui.county.model

data class EthnicGroupModel(
    val ethnicGroupId: Int,
    val ethnicGroupName: String,
    val ethnicGroupCode: Int
) {
    var hasBeenSelected: Boolean = false
}