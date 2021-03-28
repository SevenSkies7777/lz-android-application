package com.silasonyango.ndma.appStore.model

class CountyLevelQuestionnaireListObject() {
    val questionnaireList: MutableList<CountyLevelQuestionnaire> = ArrayList()

    fun addQuestionnaire(countyLevelQuestionnaire: CountyLevelQuestionnaire) {
        questionnaireList.add(countyLevelQuestionnaire)
    }
}