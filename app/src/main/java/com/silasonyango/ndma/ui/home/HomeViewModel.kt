package com.silasonyango.ndma.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.database.AppDatabase
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity
import com.silasonyango.ndma.database.questionnaires.repository.QuestionnaireTypeRepository
import com.silasonyango.ndma.services.model.Resource
import com.silasonyango.ndma.ui.county.repository.CountyRepository
import com.silasonyango.ndma.ui.county.repository.CountyService
import com.silasonyango.ndma.ui.model.QuestionnaireApiResponse
import com.silasonyango.ndma.ui.wealthgroup.repository.WealthGroupRepository
import com.silasonyango.ndma.ui.wealthgroup.repository.WealthGroupService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val questionnaireApiResponse: LiveData<Resource<QuestionnaireApiResponse?>> = MutableLiveData(null)

    val allQuestionnaireTypesLiveData: LiveData<List<QuestionnaireTypesEntity>>
    private val questionnaireTypeRepository: QuestionnaireTypeRepository
    val questionnaires: LiveData<List<QuestionnaireTypesEntity>> = MutableLiveData(null)

    init {
        val questionnaireTypesDao = AppDatabase.getDatabase(application).questionnaireTypesDao()
        questionnaireTypeRepository = QuestionnaireTypeRepository(questionnaireTypesDao)
        allQuestionnaireTypesLiveData = questionnaireTypeRepository.allQuestionnaireTypesLiveData
    }

    fun addQuestionnaireType(questionnaireTypesEntity: QuestionnaireTypesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            questionnaireTypeRepository.addQuestionnaireType(questionnaireTypesEntity)
        }
    }

//    fun fetchAllQuestionnaires() {
//        viewModelScope.launch(Dispatchers.IO) {
//            questionnaireTypeRepository.fetchAllQuestionnaires()
//                .collect { commitAllQuestionnaires(it) }
//        }
//    }
//
//    fun commitAllQuestionnaires(questionnaireTypesEntityList: LiveData<List<QuestionnaireTypesEntity>>) {
//        (this.questionnaires as MutableLiveData).value = questionnaireTypesEntityList.value
//    }



    fun submitWealthGroupQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        viewModelScope.launch {
            WealthGroupRepository(WealthGroupService()).submitWealthGroupQuestionnaire(wealthGroupQuestionnaire)
                .collect { commitQuestionnaireApiResponse(it) }
        }
    }

    fun submitCountyLevelQuestionnaire(countyLevelQuestionnaire: CountyLevelQuestionnaire) {
        viewModelScope.launch {
            CountyRepository(CountyService()).submitCountyQuestionnaire(countyLevelQuestionnaire)
                .collect { commitQuestionnaireApiResponse(it) }
        }
    }

    fun commitQuestionnaireApiResponse(questionnaireApiResponse: Resource<QuestionnaireApiResponse?>) {
        (this.questionnaireApiResponse as MutableLiveData).value = questionnaireApiResponse
    }
}