package com.ndma.livelihoodzones.ui.county.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ndma.livelihoodzones.database.AppDatabase
import com.ndma.livelihoodzones.database.questionnaires.entity.QuestionnaireTypesEntity
import com.ndma.livelihoodzones.database.questionnaires.repository.QuestionnaireTypeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CountyLevelViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    val allQuestionnaireTypesLiveData: LiveData<List<QuestionnaireTypesEntity>>
    private val questionnaireTypeRepository: QuestionnaireTypeRepository

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
}