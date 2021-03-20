package com.silasonyango.ndma.ui.county.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.SubCountyModel
import java.util.zip.Inflater


class SubCountiesSpinnerAdapter(context: Context, resource: Int,textViewId: Int, private val subCountyList: MutableList<SubCountyModel>) : ArrayAdapter<SubCountyModel>(context, resource,textViewId, subCountyList) {
    var inflater: LayoutInflater? = null
    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val currentSubCounty = subCountyList.get(position)
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.spinner_item_layout, parent, false)
        val tvSpinnerItem = view.findViewById<TextView>(R.id.tvSpinnerItemText)
        tvSpinnerItem.text = currentSubCounty.subCountyName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val currentSubCounty = subCountyList.get(position)
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.spinner_item_layout, parent, false)
        val tvSpinnerItem = view.findViewById<TextView>(R.id.tvSpinnerItemText)
        tvSpinnerItem.text = currentSubCounty.subCountyName
        return view
    }
}