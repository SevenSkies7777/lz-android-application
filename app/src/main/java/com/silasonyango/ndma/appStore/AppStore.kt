package com.silasonyango.ndma.appStore

import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire

class AppStore {
    var questionnairesList: MutableList<CountyLevelQuestionnaire> = ArrayList()

    companion object {

        private val appStore: AppStore = AppStore()

        @JvmStatic
        fun getInstance(): AppStore {
            return appStore
        }
    }
}