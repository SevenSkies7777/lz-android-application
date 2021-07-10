package com.ndma.livelihoodzones.appStore.model

import com.ndma.livelihoodzones.ui.county.responses.LzCropProductionResponses
import com.ndma.livelihoodzones.ui.county.responses.LzSeasonsResponses
import com.ndma.livelihoodzones.ui.county.responses.WealthGroupResponse

class CountyLevelDraft {
    var draftLivelihoodZoneSeasonsResponses: LzSeasonsResponses? = null

    var wealthGroupResponse: WealthGroupResponse? = null

    var lzCropProductionResponses: LzCropProductionResponses? =  null
}