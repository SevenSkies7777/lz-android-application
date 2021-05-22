package com.ndma.livelihoodzones.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.ndma.livelihoodzones.appStore.model.CountyLevelQuestionnaire
import com.ndma.livelihoodzones.appStore.model.WealthGroupQuestionnaire
import com.ndma.livelihoodzones.database.AppDatabase
import com.ndma.livelihoodzones.database.questionnaires.entity.QuestionnaireTypesEntity
import com.ndma.livelihoodzones.database.questionnaires.repository.QuestionnaireTypeRepository
import com.ndma.livelihoodzones.services.model.Resource
import com.ndma.livelihoodzones.ui.county.repository.CountyRepository
import com.ndma.livelihoodzones.ui.county.repository.CountyService
import com.ndma.livelihoodzones.ui.model.QuestionnaireApiResponse
import com.ndma.livelihoodzones.ui.wealthgroup.repository.WealthGroupRepository
import com.ndma.livelihoodzones.ui.wealthgroup.repository.WealthGroupService
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