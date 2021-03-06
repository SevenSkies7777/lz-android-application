package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.MarketCountySelectionEnum
import com.ndma.livelihoodzones.ui.county.model.MarketTransactionsItem
import com.ndma.livelihoodzones.ui.county.model.SubCountyModel

class MarketSubCountySelectionAdapter(
    private val subCountyModelList: MutableList<SubCountyModel>,
    val marketSubCountySelectionAdapterCallBack: MarketSubCountySelectionAdapter.MarketSubCountySelectionAdapterCallBack,
    val marketCountySelectionEnum: MarketCountySelectionEnum,
    val marketTransactionsItem: MarketTransactionsItem?,
    val marketTransactionArrayPosition: Int?,
    val subcountyNameTextView: TextView?
) : RecyclerView.Adapter<MarketSubCountySelectionAdapter.ViewHolder>() {

    interface MarketSubCountySelectionAdapterCallBack {
        fun onMarketSubCountyItemClicked(
            selectedSubCounty: SubCountyModel,
            marketCountySelectionEnum: MarketCountySelectionEnum,
            marketTransactionsItem: MarketTransactionsItem,
            marketTransactionArrayPosition: Int,
            subcountyNameTextView: TextView?
        )
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
        viewHolder.tvOption.text = subCountyModelList.get(position).subCountyName
        if (position == subCountyModelList.size - 1) {
            viewHolder.stroke.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {

            marketTransactionsItem?.let { marketItem ->
                marketTransactionArrayPosition?.let { arrayPosition ->
                    subcountyNameTextView?.let { subcountyTextView ->
                        marketSubCountySelectionAdapterCallBack.onMarketSubCountyItemClicked(
                            subCountyModelList.get(
                                position
                            ),
                            marketCountySelectionEnum,
                            marketItem,
                            arrayPosition,
                            subcountyTextView
                        )
                    }
                }
            }

        }
    }

    override fun getItemCount() = subCountyModelList.size
}