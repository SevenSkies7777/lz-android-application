package com.ndma.livelihoodzones.appStore

import com.ndma.livelihoodzones.appStore.model.CountyLevelQuestionnaire
import com.ndma.livelihoodzones.appStore.model.WealthGroupQuestionnaire
import com.ndma.livelihoodzones.login.model.LoginResponseModel

class AppStore {
    var sessionDetails: LoginResponseModel? = null
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