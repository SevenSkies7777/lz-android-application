package com.silasonyango.ndma.ui.wealthgroup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.home.adapters.WgCropContributionRankAdapter
import com.silasonyango.ndma.ui.model.CropContributionRankTypeEnum
import com.silasonyango.ndma.ui.model.LivestockContributionRankTypeEnum
import com.silasonyango.ndma.ui.model.RankResponseItem
import com.silasonyango.ndma.ui.model.WgLivestockTypesEnum
import com.silasonyango.ndma.ui.wealthgroup.responses.WgCropContributionResponseItem
import com.silasonyango.ndma.ui.wealthgroup.responses.WgLivestockContributionResponseItem

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