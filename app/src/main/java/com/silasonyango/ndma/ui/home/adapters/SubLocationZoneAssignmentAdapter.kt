package com.silasonyango.ndma.ui.home.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.LivelihoodZoneModel
import com.silasonyango.ndma.ui.county.model.SubLocationZoneAssignmentModel

class SubLocationZoneAssignmentAdapter(
    private val subLocationZoneAssignmentList: MutableList<SubLocationZoneAssignmentModel>,
    val context: Context
) : RecyclerView.Adapter<SubLocationZoneAssignmentAdapter.ViewHolder>() {

    lateinit var currentSubLocationZoneAssignmentModel: SubLocationZoneAssignmentModel

    var currentPosition: Int = 0

    interface SubLocationZoneAssignmentAdapterCallBack {
        fun onLivelihoodZoneSelected(selectedSubLocationZoneAssignment: SubLocationZoneAssignmentModel, position: Int)
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var subLocationName: TextView = view.findViewById<TextView>(R.id.subLocationName)
        var lzDropDown: TextView = view.findViewById<TextView>(R.id.lzDropDown)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sublocation_zone_assignment_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        currentPosition = position
        currentSubLocationZoneAssignmentModel = subLocationZoneAssignmentList.get(position)
        viewHolder.subLocationName.text = subLocationZoneAssignmentList.get(position).subLocationName
        viewHolder.lzDropDown.text = currentSubLocationZoneAssignmentModel.zoneName
    }

    override fun getItemCount() = subLocationZoneAssignmentList.size
}