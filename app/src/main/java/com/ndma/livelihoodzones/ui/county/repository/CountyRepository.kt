package com.ndma.livelihoodzones.ui.county.repository

import com.ndma.livelihoodzones.appStore.model.CountyLevelQuestionnaire
import com.ndma.livelihoodzones.services.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CountyRepository(private val countyservice: CountyService) {
    fun submitCountyQuestionnaire(countyLevelQuestionnaire: CountyLevelQuestionnaire) = flow {
        emit(Resource.loading(null))
        emit(countyservice.submitCountyQuestionnaire(countyLevelQuestionnaire))
    }.flowOn(Dispatchers.IO)
}