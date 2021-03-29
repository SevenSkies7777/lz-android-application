package com.silasonyango.ndma.ui.wealthgroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.login.model.LoginRequestModel
import com.silasonyango.ndma.login.model.LoginResponseModel
import com.silasonyango.ndma.login.repository.LoginRepository
import com.silasonyango.ndma.services.model.Resource
import com.silasonyango.ndma.ui.model.QuestionnaireApiResponse
import com.silasonyango.ndma.ui.wealthgroup.repository.WealthGroupRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WealthGroupViewModel(private val wealthGroupRepository: WealthGroupRepository) : ViewModel() {

    val questionnaireApiResponse: LiveData<Resource<QuestionnaireApiResponse?>> = MutableLiveData(null)

    fun submitWealthGroupQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        viewModelScope.launch {
            wealthGroupRepository.submitWealthGroupQuestionnaire(wealthGroupQuestionnaire)
                .collect { commitQuestionnaireApiResponse(it) }
        }
    }

    fun commitQuestionnaireApiResponse(questionnaireApiResponse: Resource<QuestionnaireApiResponse?>) {
        (this.questionnaireApiResponse as MutableLiveData).value = questionnaireApiResponse
    }
}