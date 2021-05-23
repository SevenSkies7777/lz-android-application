package com.ndma.livelihoodzones.appStore.model

class WealthGroupQuestionnaireListObject {
    var questionnaireList: MutableList<WealthGroupQuestionnaire> = ArrayList()

    fun addQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        questionnaireList.add(wealthGroupQuestionnaire)
    }

    fun updateQuestionnaire(position: Int, wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        questionnaireList.set(position,wealthGroupQuestionnaire)
    }
}