package com.silasonyango.ndma.ui.county.responses

import com.silasonyango.ndma.ui.county.model.MonthsModel

class LzSeasonsResponses() {

    /* Seasons responses */
    lateinit var dry: MonthsModel
    lateinit var longRains: MonthsModel
    lateinit var shortRains: MonthsModel

    /* Crop production responses */
    lateinit var maizeLandPreparation: MonthsModel
    lateinit var cassavaLandPreparation: MonthsModel
    lateinit var riceLandPreparation: MonthsModel
    lateinit var sorghumLandPreparation: MonthsModel
    lateinit var legumesLandPreparation: MonthsModel

    lateinit var maizePlanting: MonthsModel
    lateinit var cassavaPlanting: MonthsModel
    lateinit var ricePlanting: MonthsModel
    lateinit var sorghumPlanting: MonthsModel
    lateinit var legumesPlanting: MonthsModel

    lateinit var maizeHarvesting: MonthsModel
    lateinit var cassavaHarvesting: MonthsModel
    lateinit var riceHarvesting: MonthsModel
    lateinit var sorghumHarvesting: MonthsModel
    lateinit var legumesHarvesting: MonthsModel
}