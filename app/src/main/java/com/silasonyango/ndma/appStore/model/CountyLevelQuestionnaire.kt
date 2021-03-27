package com.silasonyango.ndma.appStore.model

import com.silasonyango.ndma.ui.county.responses.HungerPatternsResponses
import com.silasonyango.ndma.ui.county.responses.WaterSourcesResponses
import com.silasonyango.ndma.ui.county.responses.WealthGroupResponse

class CountyLevelQuestionnaire(val uniqueId: String,val questionnaireName: String){
    lateinit var wealthGroupResponse: WealthGroupResponse

    lateinit var waterSourceResponses: WaterSourcesResponses

    lateinit var hungerPatternsResponses: HungerPatternsResponses
}