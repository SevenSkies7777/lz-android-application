package com.silasonyango.ndma.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.EthnicityResponseItem
import com.silasonyango.ndma.ui.county.model.MarketTransactionsItem
import com.silasonyango.ndma.ui.county.model.SubCountyModel

class MarketTransactionsAdapter(
    private val marketTransactionItems: MutableList<MarketTransactionsItem>,
    val marketTransactionsAdapterCallBack: MarketTransactionsAdapter.MarketTransactionsAdapterCallBack
) : RecyclerView.Adapter<MarketTransactionsAdapter.ViewHolder>() {
    lateinit var currentMarketTransactionItem: MarketTransactionsItem

    interface MarketTransactionsAdapterCallBack {
        fun onLivestockMarketTradeClicked(marketUniqueId: String, isTradeHappening: Boolean)

        fun onPoultryMarketTradeClicked(marketUniqueId: String, isTradeHappening: Boolean)

        fun onFarmProduceTradeClicked(marketUniqueId: String, isTradeHappening: Boolean)

        fun onFoodProduceTradeClicked(marketUniqueId: String, isTradeHappening: Boolean)

        fun onFarmInputsTradeClicked(marketUniqueId: String, isTradeHappening: Boolean)

        fun onLabourExchangeTradeClicked(marketUniqueId: String, isTradeHappening: Boolean)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var marketName: TextView = view.findViewById<TextView>(R.id.marketName)
        var livestock: View = view.findViewById<TextView>(R.id.livestock)
        var livestockTick: View = view.findViewById<TextView>(R.id.livestockTick)

        var poultry: View = view.findViewById<TextView>(R.id.poultry)
        var poultryTick: View = view.findViewById<TextView>(R.id.poultryTick)

        var farmProduce: View = view.findViewById<TextView>(R.id.farmProduce)
        var farmProduceTick: View = view.findViewById<TextView>(R.id.farmProduceTick)

        var foodProduce: View = view.findViewById<TextView>(R.id.foodProduce)
        var foodProduceTick: View = view.findViewById<TextView>(R.id.foodProduceTick)

        var farmInputs: View = view.findViewById<TextView>(R.id.farmInputs)
        var farmInputsTick: View = view.findViewById<TextView>(R.id.farmInputsTick)

        var labourExchange: View = view.findViewById<TextView>(R.id.labourExchange)
        var labourExchangeTick: View = view.findViewById<TextView>(R.id.labourExchangeTick)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.market_transactions_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        currentMarketTransactionItem = marketTransactionItems.get(position)
        viewHolder.marketName.text = currentMarketTransactionItem.marketName

        viewHolder.livestock.setOnClickListener {
            if (viewHolder.livestockTick.visibility == View.VISIBLE) {
                viewHolder.livestockTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onLivestockMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false
                )
            } else {
                viewHolder.livestockTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onLivestockMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true
                )
            }
        }

        viewHolder.poultry.setOnClickListener {
            if (viewHolder.poultryTick.visibility == View.VISIBLE) {
                viewHolder.poultryTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onPoultryMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false
                )
            } else {
                viewHolder.poultryTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onPoultryMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true
                )
            }
        }

        viewHolder.farmProduce.setOnClickListener {
            if (viewHolder.farmProduceTick.visibility == View.VISIBLE) {
                viewHolder.farmProduceTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onFarmProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false
                )
            } else {
                viewHolder.farmProduceTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onFarmProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true
                )
            }
        }


        viewHolder.foodProduce.setOnClickListener {
            if (viewHolder.foodProduceTick.visibility == View.VISIBLE) {
                viewHolder.foodProduceTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onFoodProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false
                )
            } else {
                viewHolder.foodProduceTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onFoodProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true
                )
            }
        }


        viewHolder.farmInputs.setOnClickListener {
            if (viewHolder.farmInputsTick.visibility == View.VISIBLE) {
                viewHolder.farmInputsTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onFarmInputsTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false
                )
            } else {
                viewHolder.farmInputsTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onFarmInputsTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true
                )
            }
        }


        viewHolder.labourExchange.setOnClickListener {
            if (viewHolder.labourExchangeTick.visibility == View.VISIBLE) {
                viewHolder.labourExchangeTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onLabourExchangeTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false
                )
            } else {
                viewHolder.labourExchangeTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onLabourExchangeTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true
                )
            }
        }

    }

    override fun getItemCount() = marketTransactionItems.size
}