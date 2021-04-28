package com.silasonyango.ndma.ui.wealthgroup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.responses.LzCropProductionResponseItem

class CropProductionListAdapter(context: Context, val resource: Int, val responseItems: MutableList<LzCropProductionResponseItem>):
    ArrayAdapter<LzCropProductionResponseItem>(context,resource,responseItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.lz_crop_production_item, null, true)

        return view
    }

    override fun getCount(): Int {
        return responseItems.size
    }
}