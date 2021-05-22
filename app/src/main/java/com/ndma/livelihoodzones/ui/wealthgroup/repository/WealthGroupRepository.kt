package com.ndma.livelihoodzones.ui.wealthgroup.repository

import com.ndma.livelihoodzones.appStore.model.WealthGroupQuestionnaire
import com.ndma.livelihoodzones.services.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class WealthGroupRepository(private val wealthGroupService: WealthGroupService) {
    fun submitWealthGroupQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire) = flow {
        emit(Resource.loading(null))
        emit(wealthGroupService.submitWealthGroupQuestionnaire(wealthGroupQuestionnaire))
    }.flowOn(Dispatchers.IO)
}