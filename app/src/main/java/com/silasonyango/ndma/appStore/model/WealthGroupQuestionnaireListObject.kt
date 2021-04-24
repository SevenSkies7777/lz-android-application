package com.silasonyango.ndma.appStore.model

class WealthGroupQuestionnaireListObject {
    var questionnaireList: MutableList<WealthGroupQuestionnaire> = ArrayList()

    fun addQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        questionnaireList.add(wealthGroupQuestionnaire)
    }
}