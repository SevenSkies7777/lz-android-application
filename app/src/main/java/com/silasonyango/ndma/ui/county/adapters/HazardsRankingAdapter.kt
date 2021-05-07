package com.silasonyango.ndma.ui.county.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.HazardTypeEnum
import com.silasonyango.ndma.ui.model.LivestockContributionRankTypeEnum
import com.silasonyango.ndma.ui.model.RankResponseItem
import com.silasonyango.ndma.ui.model.WgLivestockTypesEnum
import com.silasonyango.ndma.ui.wealthgroup.adapters.LivestockContributionRankAdapter

class HazardsRankingAdapter(
    val rankResponseItems: MutableList<RankResponseItem>,
    val hazardsRankingAdapterCallBack: HazardsRankingAdapter.HazardsRankingAdapterCallBack,
    val hazardTypeEnum: HazardTypeEnum
) : RecyclerView.Adapter<HazardsRankingAdapter.ViewHolder>() {

    interface HazardsRankingAdapterCallBack {
        fun onAHazardRankItemSelected(
            selectedRankItem: RankResponseItem,
            position: Int,
            hazardTypeEnum: HazardTypeEnum
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.simple_list_item_layout, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentRank = rankResponseItems.get(position)
        viewHolder.tvOption.text = currentRank.rankPosition.toString()
        viewHolder.itemView.setOnClickListener {
            hazardsRankingAdapterCallBack.onAHazardRankItemSelected(
                currentRank,
                position,
                hazardTypeEnum
            )
        }
    }

    override fun getItemCount() = rankResponseItems.size
}