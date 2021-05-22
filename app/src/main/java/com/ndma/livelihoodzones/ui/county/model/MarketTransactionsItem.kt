package com.ndma.livelihoodzones.ui.county.model

class MarketTransactionsItem(
    val marketUniqueId: String,
    val marketName: String,
    var livestockTrade: Boolean,
    var poultryTrade: Boolean,
    var farmProduceTrade: Boolean,
    var foodProduceRetail: Boolean,
    var retailFarmInput: Boolean,
    var labourExchange: Boolean
)