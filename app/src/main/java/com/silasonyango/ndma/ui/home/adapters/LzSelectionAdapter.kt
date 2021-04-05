package com.silasonyango.ndma.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.LivelihoodZoneModel

class LzSelectionAdapter(
    private val livelihoodZoneModelList: MutableList<LivelihoodZoneModel>,
    val lzSelectionAdapterCallBack: LzSelectionAdapter.LzSelectionAdapterCallBack
) : RecyclerView.Adapter<LzSelectionAdapter.ViewHolder>() {

    interface LzSelectionAdapterCallBack {
        fun onLivelihoodZoneItemSelectedFromSelectionList(selectedLivelihoodZone: LivelihoodZoneModel)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
        var highlightIcon: View = view.findViewById<View>(R.id.highlightIcon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.lz_selection_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvOption.text = livelihoodZoneModelList.get(position).livelihoodZoneName
        viewHolder.itemView.setOnClickListener {
            lzSelectionAdapterCallBack.onLivelihoodZoneItemSelectedFromSelectionList(
                livelihoodZoneModelList.get(position)
            )
            viewHolder.highlightIcon.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = livelihoodZoneModelList.size
}