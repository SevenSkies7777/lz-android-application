package com.silasonyango.ndma.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.silasonyango.ndma.database.AppDatabase
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity
import com.silasonyango.ndma.database.questionnaires.repository.QuestionnaireTypeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

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
}