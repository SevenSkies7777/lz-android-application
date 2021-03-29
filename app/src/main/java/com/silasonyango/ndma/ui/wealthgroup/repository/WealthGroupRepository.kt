package com.silasonyango.ndma.ui.wealthgroup.repository

import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.login.model.LoginRequestModel
import com.silasonyango.ndma.services.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class WealthGroupRepository(private val wealthGroupService: WealthGroupService) {
    fun submitWealthGroupQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire) = flow {
        emit(Resource.loading(null))
        emit(wealthGroupService.submitWealthGroupQuestionnaire(wealthGroupQuestionnaire))
    }.flowOn(Dispatchers.IO)
}