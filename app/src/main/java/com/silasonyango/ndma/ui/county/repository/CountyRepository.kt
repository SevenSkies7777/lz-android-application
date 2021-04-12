package com.silasonyango.ndma.ui.county.repository

import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.services.model.Resource
import com.silasonyango.ndma.ui.wealthgroup.repository.WealthGroupService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CountyRepository(private val countyservice: CountyService) {
    fun submitCountyQuestionnaire(countyLevelQuestionnaire: CountyLevelQuestionnaire) = flow {
        emit(Resource.loading(null))
        emit(countyservice.submitCountyQuestionnaire(countyLevelQuestionnaire))
    }.flowOn(Dispatchers.IO)
}