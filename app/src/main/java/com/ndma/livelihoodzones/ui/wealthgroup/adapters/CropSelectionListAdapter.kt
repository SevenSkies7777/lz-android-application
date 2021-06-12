package com.ndma.livelihoodzones.ui.wealthgroup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.appStore.AppStore
import com.ndma.livelihoodzones.ui.county.model.CropModel
import java.util.*
import kotlin.collections.ArrayList

class CropSelectionListAdapter(
    context: Context,
    val resource: Int,
    val crops: MutableList<CropModel>,
    val cropSelectionListAdapterCallBack: CropSelectionListAdapterCallBack,
    val isThisCurrentPageResume: Boolean
) :
    ArrayAdapter<CropModel>(context, resource, crops), Filterable {

    private var allCrops: MutableList<CropModel> = ArrayList(crops)
    private var cropsList = crops
    private val customFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredCrops: MutableList<CropModel> =
                ArrayList<CropModel>()
            if (constraint.toString().isEmpty()) {
                filteredCrops.addAll(allCrops)
            } else {
                for (crop in allCrops) {
                    if (crop.cropName.toLowerCase()
                            .contains(constraint.toString().toLowerCase())
                    ) {
                        filteredCrops.add(crop)
                    }
                }
            }
            val filterResults = FilterResults()
            filterResults.values = filteredCrops
            return filterResults
        }

        override fun publishResults(
            constraint: CharSequence,
            results: FilterResults
        ) {
            cropsList.clear()
            cropsList.addAll(results.values as Collection<CropModel>)
            notifyDataSetChanged()
        }
    }

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
                if (isThisCurrentPageResume) {
                    AppStore.getInstance().newlySelectedCrops.add(currentCropModel)
                }
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

    override fun getFilter(): Filter {
        return customFilter
    }
}