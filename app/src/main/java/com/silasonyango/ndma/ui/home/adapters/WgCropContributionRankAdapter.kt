package com.silasonyango.ndma.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.model.CropContributionRankTypeEnum
import com.silasonyango.ndma.ui.model.RankResponseItem
import com.silasonyango.ndma.ui.wealthgroup.responses.WgCropContributionResponseItem

class WgCropContributionRankAdapter(
    private val rankResponseItems: MutableList<RankResponseItem>,
    val wgCropContributionRankAdapterCallBack: WgCropContributionRankAdapter.RankAdapterCallBack,
    val cropContributionRankTypeEnum: CropContributionRankTypeEnum,
    val currentCropResponseItem: WgCropContributionResponseItem,
    val currentCropResponseItemPosition: Int
) : RecyclerView.Adapter<WgCropContributionRankAdapter.ViewHolder>() {

    interface RankAdapterCallBack {
        fun onARankItemSelected(
            selectedRankItem: RankResponseItem,
            position: Int,
            cropContributionRankTypeEnum: CropContributionRankTypeEnum,
            currentResponseItem: WgCropContributionResponseItem,
            currentCropResponseItemPosition: Int
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
            wgCropContributionRankAdapterCallBack.onARankItemSelected(
                currentRank,
                position,
                cropContributionRankTypeEnum,
                currentCropResponseItem,
                currentCropResponseItemPosition
            )
        }
    }

    override fun getItemCount() = rankResponseItems.size
}