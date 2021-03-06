package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.LivelihoodZoneModel

class LivelihoodZonesAdapter(
    private val livelihoodZoneModelList: MutableList<LivelihoodZoneModel>,
    val livelihoodZonesAdapterCallBack: LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack
) : RecyclerView.Adapter<LivelihoodZonesAdapter.ViewHolder>() {

    interface LivelihoodZonesAdapterCallBack {
        fun onLivelihoodZoneItemClicked(selectedLivelihoodZone: LivelihoodZoneModel )
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
        viewHolder.tvOption.text = livelihoodZoneModelList.get(position).livelihoodZoneName
        if (position == livelihoodZoneModelList.size - 1) {
            viewHolder.stroke.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            livelihoodZonesAdapterCallBack.onLivelihoodZoneItemClicked(livelihoodZoneModelList.get(position))
        }
    }

    override fun getItemCount() = livelihoodZoneModelList.size
}