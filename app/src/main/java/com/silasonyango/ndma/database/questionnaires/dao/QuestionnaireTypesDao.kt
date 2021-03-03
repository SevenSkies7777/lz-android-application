package com.silasonyango.ndma.database.questionnaires.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity

@Dao
interface QuestionnaireTypesDao {
    @Query("SELECT * FROM questionnaire_types")
    fun getAllQuestionnaireTypes(): LiveData<List<QuestionnaireTypesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg questionnaireTypesEntity: QuestionnaireTypesEntity)

    @Delete
    fun delete(questionnaireTypesEntity: QuestionnaireTypesEntity)
}