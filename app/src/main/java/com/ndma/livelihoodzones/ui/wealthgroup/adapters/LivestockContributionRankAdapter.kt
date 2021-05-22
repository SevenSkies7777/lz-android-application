package com.ndma.livelihoodzones.ui.wealthgroup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.model.LivestockContributionRankTypeEnum
import com.ndma.livelihoodzones.ui.model.RankResponseItem
import com.ndma.livelihoodzones.ui.model.WgLivestockTypesEnum

class LivestockContributionRankAdapter(
    val rankResponseItems: MutableList<RankResponseItem>,
    val livestockContributionRankAdapterCallBack: LivestockContributionRankAdapter.LivestockContributionRankAdapterCallBack,
    val livestockContributionRankTypeEnum: LivestockContributionRankTypeEnum,
    val animalType: WgLivestockTypesEnum
) : RecyclerView.Adapter<LivestockContributionRankAdapter.ViewHolder>() {

    interface LivestockContributionRankAdapterCallBack {
        fun onALivestockContributionRankItemSelected(
            selectedRankItem: RankResponseItem,
            position: Int,
            livestockContributionRankTypeEnum: LivestockContributionRankTypeEnum,
            animalType: WgLivestockTypesEnum
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
            livestockContributionRankAdapterCallBack.onALivestockContributionRankItemSelected(
                currentRank,
                position,
                livestockContributionRankTypeEnum,
                animalType
            )
        }
    }

    override fun getItemCount() = rankResponseItems.size
}