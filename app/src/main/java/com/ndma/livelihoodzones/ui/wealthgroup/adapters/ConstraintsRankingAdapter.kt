package com.ndma.livelihoodzones.ui.wealthgroup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.model.RankResponseItem
import com.ndma.livelihoodzones.ui.wealthgroup.model.ConstraintCategoryEnum
import com.ndma.livelihoodzones.ui.wealthgroup.model.ConstraintsTypeEnum

class ConstraintsRankingAdapter(
    val rankResponseItems: MutableList<RankResponseItem>,
    val constraintsRankingAdapterCallBack: ConstraintsRankingAdapter.ConstraintsRankingAdapterCallBack,
    val constraintsTypeEnum: ConstraintsTypeEnum,
    val constraintCategoryEnum: ConstraintCategoryEnum
) : RecyclerView.Adapter<ConstraintsRankingAdapter.ViewHolder>() {

    interface ConstraintsRankingAdapterCallBack {
        fun onAConstraintsRankItemSelected(
            selectedRankItem: RankResponseItem,
            position: Int,
            constraintsTypeEnum: ConstraintsTypeEnum,
            constraintCategoryEnum: ConstraintCategoryEnum
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
            constraintsRankingAdapterCallBack.onAConstraintsRankItemSelected(
                currentRank,
                position,
                constraintsTypeEnum,
                constraintCategoryEnum
            )
        }
    }

    override fun getItemCount() = rankResponseItems.size
}