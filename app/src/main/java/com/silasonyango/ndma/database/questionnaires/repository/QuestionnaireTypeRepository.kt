package com.silasonyango.ndma.database.questionnaires.repository

import androidx.lifecycle.LiveData
import com.silasonyango.ndma.database.questionnaires.dao.QuestionnaireTypesDao
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity

class QuestionnaireTypeRepository(private val questionnaireTypesDao: QuestionnaireTypesDao) {

    val allQuestionnaireTypesLiveData: LiveData<List<QuestionnaireTypesEntity>> = questionnaireTypesDao.getAllQuestionnaireTypes()

    suspend fun addQuestionnaireType(questionnaireTypesEntity: QuestionnaireTypesEntity) {
        questionnaireTypesDao.insertAll(questionnaireTypesEntity)
    }
}