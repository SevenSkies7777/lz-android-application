package com.silasonyango.ndma.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.SubLocationModel
import com.silasonyango.ndma.ui.county.model.WardModel

class SubLocationAdapter(
    private val sublocationModelList: MutableList<SubLocationModel>,
    val subLocationAdapterCallBack: SubLocationAdapter.SubLocationAdapterCallBack
) : RecyclerView.Adapter<SubLocationAdapter.ViewHolder>() {

    interface SubLocationAdapterCallBack {
        fun onSubLocationItemClicked(selectedSubLocation: SubLocationModel)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
        var stroke: View = view.findViewById<View>(R.id.stroke)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.simple_list_item_layout, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvOption.text = sublocationModelList.get(position).subLocationName
        if (position == sublocationModelList.size - 1) {
            viewHolder.stroke.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            subLocationAdapterCallBack.onSubLocationItemClicked(sublocationModelList.get(position))
        }
    }

    override fun getItemCount() = sublocationModelList.size
}