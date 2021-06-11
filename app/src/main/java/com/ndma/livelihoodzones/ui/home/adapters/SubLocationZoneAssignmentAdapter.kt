package com.ndma.livelihoodzones.ui.home.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.SubLocationZoneAssignmentModel

class SubLocationZoneAssignmentAdapter(
    private val subLocationZoneAssignmentList: MutableList<SubLocationZoneAssignmentModel>,
    val context: Context,
    val subLocationZoneAssignmentAdapterCallBack: SubLocationZoneAssignmentAdapterCallBack
) : RecyclerView.Adapter<SubLocationZoneAssignmentAdapter.ViewHolder>() {

    interface SubLocationZoneAssignmentAdapterCallBack {
        fun onLivelihoodZoneSublocationClicked(
            selectedSubLocationZoneAssignment: SubLocationZoneAssignmentModel,
            position: Int
        )
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var subLocationName: TextView = view.findViewById<TextView>(R.id.subLocationName)
        var lzDropDown: TextView = view.findViewById<TextView>(R.id.lzDropDown)

        var highlightIcon: View = view.findViewById<View>(R.id.highlightIcon)
        var uncheckedIcon: View = view.findViewById<View>(R.id.uncheckedIcon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sublocation_zone_assignment_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        var currentSubLocationZoneAssignmentModel = subLocationZoneAssignmentList.get(position)
        viewHolder.setIsRecyclable(false)
        viewHolder.subLocationName.text =
            currentSubLocationZoneAssignmentModel.subLocationName
        viewHolder.lzDropDown.text = currentSubLocationZoneAssignmentModel.zoneName

        viewHolder.highlightIcon.isVisible = currentSubLocationZoneAssignmentModel.hasBeenSelected
        viewHolder.uncheckedIcon.isVisible = !currentSubLocationZoneAssignmentModel.hasBeenSelected

        viewHolder.itemView.setOnClickListener {
            if (currentSubLocationZoneAssignmentModel.hasBeenSelected) {
                currentSubLocationZoneAssignmentModel.hasBeenSelected = false
                viewHolder.highlightIcon.isVisible = false
                viewHolder.uncheckedIcon.isVisible = true
            } else {
                currentSubLocationZoneAssignmentModel.hasBeenSelected = true
                viewHolder.highlightIcon.isVisible = true
                viewHolder.uncheckedIcon.isVisible = false
            }
            subLocationZoneAssignmentAdapterCallBack.onLivelihoodZoneSublocationClicked(currentSubLocationZoneAssignmentModel,position)
        }
    }

    override fun getItemCount() = subLocationZoneAssignmentList.size
}