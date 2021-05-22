package com.ndma.livelihoodzones.appStore.model

class CountyLevelQuestionnaireListObject() {
    var questionnaireList: MutableList<CountyLevelQuestionnaire> = ArrayList()

    fun addQuestionnaire(countyLevelQuestionnaire: CountyLevelQuestionnaire) {
        questionnaireList.add(countyLevelQuestionnaire)
    }
}