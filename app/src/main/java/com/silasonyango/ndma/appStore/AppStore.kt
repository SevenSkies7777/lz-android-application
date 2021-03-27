package com.silasonyango.ndma.appStore

import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire

class AppStore {
    var accessToken: String = ""
    var countyLevelQuestionnairesList: MutableList<CountyLevelQuestionnaire> = ArrayList()

    var wealthGroupQuestionnaireList: MutableList<WealthGroupQuestionnaire> = ArrayList()

    companion object {

        private val appStore: AppStore = AppStore()

        @JvmStatic
        fun getInstance(): AppStore {
            return appStore
        }
    }
}