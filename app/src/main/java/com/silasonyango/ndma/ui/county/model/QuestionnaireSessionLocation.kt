package com.silasonyango.ndma.ui.county.model

import android.os.Parcel
import android.os.Parcelable

class QuestionnaireSessionLocation(): Parcelable {
    lateinit var selectedSubCounty: SubCountyModel
    lateinit var selectedWard: WardModel
    lateinit var selectedSubLocation: SubLocationModel
    lateinit var selectedWealthGroup: WealthGroupModel

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