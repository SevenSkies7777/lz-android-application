package com.ndma.livelihoodzones.database.questionnaires.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ndma.livelihoodzones.database.questionnaires.entity.QuestionnaireTypesEntity

@Dao
interface QuestionnaireTypesDao {
    @Query("SELECT * FROM questionnaire_types")
    fun getAllQuestionnaireTypes(): LiveData<List<QuestionnaireTypesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg questionnaireTypesEntity: QuestionnaireTypesEntity)

    @Delete
    fun delete(questionnaireTypesEntity: QuestionnaireTypesEntity)
}