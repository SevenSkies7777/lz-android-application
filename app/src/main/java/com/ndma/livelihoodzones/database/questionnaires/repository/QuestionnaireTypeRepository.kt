package com.ndma.livelihoodzones.database.questionnaires.repository

import androidx.lifecycle.LiveData
import com.ndma.livelihoodzones.database.questionnaires.dao.QuestionnaireTypesDao
import com.ndma.livelihoodzones.database.questionnaires.entity.QuestionnaireTypesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class QuestionnaireTypeRepository(private val questionnaireTypesDao: QuestionnaireTypesDao) {

    val allQuestionnaireTypesLiveData: LiveData<List<QuestionnaireTypesEntity>> = questionnaireTypesDao.getAllQuestionnaireTypes()

    suspend fun addQuestionnaireType(questionnaireTypesEntity: QuestionnaireTypesEntity) {
        questionnaireTypesDao.insertAll(questionnaireTypesEntity)
    }

//    suspend fun fetchAllQuestionnaires() {
//        questionnaireTypesDao.getAllQuestionnaireTypes()
//    }

    fun fetchAllQuestionnaires() = flow {
        emit(questionnaireTypesDao.getAllQuestionnaireTypes())
    }.flowOn(Dispatchers.IO)
}