package com.silasonyango.ndma.database.questionnaires.repository

import androidx.lifecycle.LiveData
import com.silasonyango.ndma.database.questionnaires.dao.QuestionnaireTypesDao
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity
import com.silasonyango.ndma.login.model.LoginRequestModel
import com.silasonyango.ndma.services.model.Resource
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