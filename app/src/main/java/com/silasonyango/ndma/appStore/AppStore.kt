package com.silasonyango.ndma.appStore

import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire

class AppStore {
    private val appStore: AppStore = AppStore()
    private lateinit var questionnairesList: MutableList<CountyLevelQuestionnaire>

    fun getInstance(): AppStore {
        return appStore
    }
}