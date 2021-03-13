package com.silasonyango.ndma.ui.county.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.SubLocationModel

class SubLocationLzAssignmentRecyclerViewAdapter(
    private val context: Context,
    private val subLocationList: MutableList<SubLocationModel>,
    val subLocationLzAssignmentRecyclerViewAdapterCallback: SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback
) : RecyclerView.Adapter<SubLocationLzAssignmentRecyclerViewAdapter.ViewHolder>() {

    interface SubLocationLzAssignmentRecyclerViewAdapterCallback {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        init {

        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sublocation_lz_assignment_card, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

    }

    override fun getItemCount() = subLocationList.size
}