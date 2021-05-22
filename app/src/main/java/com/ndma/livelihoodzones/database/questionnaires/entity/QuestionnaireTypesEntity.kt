package com.ndma.livelihoodzones.database.questionnaires.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questionnaire_types")
data class QuestionnaireTypesEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "QuestionnaireTypeId") val questionnaireTypeId: Int,
                                    @ColumnInfo(name = "QuestionnaireTypeName") val questionnaireTypeName: String?,
                                    @ColumnInfo(name = "QuestionnaireTypeCode") val questionnaireTypeCode: String?) {
}