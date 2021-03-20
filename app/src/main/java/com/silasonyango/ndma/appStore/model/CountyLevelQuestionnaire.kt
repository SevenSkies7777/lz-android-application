package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.model.SubCountyMarkets

class CountyLevelQuestionnaire(val uniqueId: String,val questionnaireName: String){
    private lateinit var subCountyMarketsList: MutableList<SubCountyMarkets>
}