package com.silasonyango.ndma.ui.wealthgroup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.CropModel

class CropSelectionListAdapter(
    context: Context,
    val resource: Int,
    val crops: MutableList<CropModel>,
    val cropSelectionListAdapterCallBack: CropSelectionListAdapterCallBack
) :
    ArrayAdapter<CropModel>(context, resource, crops) {

    interface CropSelectionListAdapterCallBack {
        fun onCropItemSelectedFromSelectionList(selectedCrop: CropModel, position: Int)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.lz_selection_item, null, true)

        val tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
        val highlightIcon: View = view.findViewById<View>(R.id.highlightIcon)
        val uncheckedIcon: View = view.findViewById<View>(R.id.uncheckedIcon)
        val currentCropModel = crops.get(position)
        tvOption.text = crops.get(position).cropName

        if (currentCropModel.hasBeenSelected) {
            highlightIcon.visibility = View.VISIBLE
            uncheckedIcon.visibility = View.GONE
        } else {
            highlightIcon.visibility = View.GONE
            uncheckedIcon.visibility = View.VISIBLE
        }

        view.setOnClickListener {
            if (highlightIcon.visibility == View.VISIBLE) {
                currentCropModel.hasBeenSelected = false
                highlightIcon.visibility = View.GONE
                uncheckedIcon.visibility = View.VISIBLE
            } else {
                currentCropModel.hasBeenSelected = true
                highlightIcon.visibility = View.VISIBLE
                uncheckedIcon.visibility = View.GONE
            }
            cropSelectionListAdapterCallBack.onCropItemSelectedFromSelectionList(
                currentCropModel,
                position
            )
        }

        return view
    }

    override fun getCount(): Int {
        return crops.size
    }
}