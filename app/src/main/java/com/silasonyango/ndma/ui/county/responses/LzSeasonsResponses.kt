package com.silasonyango.ndma.ui.county.responses

import com.silasonyango.ndma.ui.county.model.MonthsModel

class LzSeasonsResponses() {

    /* Seasons responses */
    var dry: MutableList<MonthsModel> = ArrayList()
    var longRains: MutableList<MonthsModel> = ArrayList()
    var shortRains: MutableList<MonthsModel> = ArrayList()

    /* Crop production responses */
    var maizeLandPreparation: MutableList<MonthsModel> = ArrayList()
    var cassavaLandPreparation: MutableList<MonthsModel> = ArrayList()
    var riceLandPreparation: MutableList<MonthsModel> = ArrayList()
    var sorghumLandPreparation: MutableList<MonthsModel> = ArrayList()
    var legumesLandPreparation: MutableList<MonthsModel> = ArrayList()

    var maizePlanting: MutableList<MonthsModel> = ArrayList()
    var cassavaPlanting: MutableList<MonthsModel> = ArrayList()
    var ricePlanting: MutableList<MonthsModel> = ArrayList()
    var sorghumPlanting: MutableList<MonthsModel> = ArrayList()
    var legumesPlanting: MutableList<MonthsModel> = ArrayList()

    var maizeHarvesting: MutableList<MonthsModel> = ArrayList()
    var cassavaHarvesting: MutableList<MonthsModel> = ArrayList()
    var riceHarvesting: MutableList<MonthsModel> = ArrayList()
    var sorghumHarvesting: MutableList<MonthsModel> = ArrayList()
    var legumesHarvesting: MutableList<MonthsModel> = ArrayList()

    /* Livestock production responses */
    var livestockInMigration: MutableList<MonthsModel> = ArrayList()
    var livestockOutMigration: MutableList<MonthsModel> = ArrayList()
    var highMilkProduction: MutableList<MonthsModel> = ArrayList()
    var lowMilkProduction: MutableList<MonthsModel> = ArrayList()
    var highCalving: MutableList<MonthsModel> = ArrayList()
    var lowCalving: MutableList<MonthsModel> = ArrayList()
    var highKidding: MutableList<MonthsModel> = ArrayList()
    var lowKidding: MutableList<MonthsModel> = ArrayList()
    var highFoodPrices: MutableList<MonthsModel> = ArrayList()
    var lowFoodPrices: MutableList<MonthsModel> = ArrayList()
    var highLivestockPrices: MutableList<MonthsModel> = ArrayList()
    var lowLivestockPrices: MutableList<MonthsModel> = ArrayList()
    var highCasualLabourAvailability: MutableList<MonthsModel> = ArrayList()
    var lowCasualLabourAvailability: MutableList<MonthsModel> = ArrayList()
    var highCasualLabourWages: MutableList<MonthsModel> = ArrayList()
    var lowCasualLabourWages: MutableList<MonthsModel> = ArrayList()
    var highRemittances: MutableList<MonthsModel> = ArrayList()
    var lowRemittances: MutableList<MonthsModel> = ArrayList()
    var highFish: MutableList<MonthsModel> = ArrayList()
    var lowFish: MutableList<MonthsModel> = ArrayList()
    var highMarketAccess: MutableList<MonthsModel> = ArrayList()
    var lowMarketAccess: MutableList<MonthsModel> = ArrayList()
    var highDiseaseOutbreak: MutableList<MonthsModel> = ArrayList()
    var lowDiseaseOutbreak: MutableList<MonthsModel> = ArrayList()
    var waterStress: MutableList<MonthsModel> = ArrayList()
    var conflictRisks: MutableList<MonthsModel> = ArrayList()
    var ceremonies: MutableList<MonthsModel> = ArrayList()
    var leanSeasons: MutableList<MonthsModel> = ArrayList()
    var foodSecurityAssessments: MutableList<MonthsModel> = ArrayList()
}