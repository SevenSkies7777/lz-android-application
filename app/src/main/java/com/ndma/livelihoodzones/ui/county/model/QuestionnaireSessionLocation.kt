package com.ndma.livelihoodzones.ui.county.model

import android.os.Parcel
import android.os.Parcelable

class QuestionnaireSessionLocation(): Parcelable {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var selectedSubCounty: SubCountyModel? = null
    var selectedWard: WardModel? = null
    var selectedSubLocation: SubLocationModel? = null
    var selectedWealthGroup: WealthGroupModel? = null
    var selectedLivelihoodZone: LivelihoodZoneModel? = null
    var selectedWgQuestionnaireType: WgQuestionnaireTypeModel? = null

    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QuestionnaireSessionLocation> {
        override fun createFromParcel(parcel: Parcel): QuestionnaireSessionLocation {
            return QuestionnaireSessionLocation(parcel)
        }

        override fun newArray(size: Int): Array<QuestionnaireSessionLocation?> {
            return arrayOfNulls(size)
        }
    }
}