package com.ndma.livelihoodzones.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ndma.livelihoodzones.database.questionnaires.dao.QuestionnaireTypesDao
import com.ndma.livelihoodzones.database.questionnaires.entity.QuestionnaireTypesEntity

@Database(entities = [QuestionnaireTypesEntity::class], exportSchema = false,version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionnaireTypesDao(): QuestionnaireTypesDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "livelihoodzones"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}