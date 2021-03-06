package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.MarketTransactionsItem

class MarketTransactionsAdapter(
    private val marketTransactionItems: MutableList<MarketTransactionsItem>,
    val marketTransactionsAdapterCallBack: MarketTransactionsAdapter.MarketTransactionsAdapterCallBack
) : RecyclerView.Adapter<MarketTransactionsAdapter.ViewHolder>() {
    lateinit var currentMarketTransactionItem: MarketTransactionsItem

    interface MarketTransactionsAdapterCallBack {
        fun onLivestockMarketTradeClicked(marketUniqueId: String, isTradeHappening: Boolean,position: Int)

        fun onPoultryMarketTradeClicked(marketUniqueId: String, isTradeHappening: Boolean,position: Int)

        fun onFarmProduceTradeClicked(marketUniqueId: String, isTradeHappening: Boolean,position: Int)

        fun onFoodProduceTradeClicked(marketUniqueId: String, isTradeHappening: Boolean,position: Int)

        fun onFarmInputsTradeClicked(marketUniqueId: String, isTradeHappening: Boolean,position: Int)

        fun onLabourExchangeTradeClicked(marketUniqueId: String, isTradeHappening: Boolean,position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var marketName: TextView = view.findViewById<TextView>(R.id.marketName)
        var livestock: View = view.findViewById<View>(R.id.livestock)
        var livestockTick: View = view.findViewById<View>(R.id.livestockTick)

        var poultry: View = view.findViewById<View>(R.id.poultry)
        var poultryTick: View = view.findViewById<View>(R.id.poultryTick)

        var farmProduce: View = view.findViewById<View>(R.id.farmProduce)
        var farmProduceTick: View = view.findViewById<View>(R.id.farmProduceTick)

        var foodProduce: View = view.findViewById<View>(R.id.foodProduce)
        var foodProduceTick: View = view.findViewById<View>(R.id.foodProduceTick)

        var farmInputs: View = view.findViewById<View>(R.id.farmInputs)
        var farmInputsTick: View = view.findViewById<View>(R.id.farmInputsTick)

        var labourExchange: View = view.findViewById<View>(R.id.labourExchange)
        var labourExchangeTick: View = view.findViewById<View>(R.id.labourExchangeTick)

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

        viewHolder.livestockTick.isVisible = currentMarketTransactionItem.livestockTrade
        viewHolder.poultryTick.isVisible = currentMarketTransactionItem.poultryTrade
        viewHolder.farmProduceTick.isVisible = currentMarketTransactionItem.farmProduceTrade
        viewHolder.foodProduceTick.isVisible = currentMarketTransactionItem.foodProduceRetail
        viewHolder.farmInputsTick.isVisible = currentMarketTransactionItem.retailFarmInput
        viewHolder.labourExchangeTick.isVisible = currentMarketTransactionItem.labourExchange

        viewHolder.livestock.setOnClickListener {
            if (viewHolder.livestockTick.visibility == View.VISIBLE) {
                viewHolder.livestockTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onLivestockMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false,
                    position
                )
            } else {
                viewHolder.livestockTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onLivestockMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true,
                    position
                )
            }
        }

        viewHolder.poultry.setOnClickListener {
            if (viewHolder.poultryTick.visibility == View.VISIBLE) {
                viewHolder.poultryTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onPoultryMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false,
                    position
                )
            } else {
                viewHolder.poultryTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onPoultryMarketTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true,
                    position
                )
            }
        }

        viewHolder.farmProduce.setOnClickListener {
            if (viewHolder.farmProduceTick.visibility == View.VISIBLE) {
                viewHolder.farmProduceTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onFarmProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false,
                    position
                )
            } else {
                viewHolder.farmProduceTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onFarmProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true,
                    position
                )
            }
        }


        viewHolder.foodProduce.setOnClickListener {
            if (viewHolder.foodProduceTick.visibility == View.VISIBLE) {
                viewHolder.foodProduceTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onFoodProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false,
                    position
                )
            } else {
                viewHolder.foodProduceTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onFoodProduceTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true,
                    position
                )
            }
        }


        viewHolder.farmInputs.setOnClickListener {
            if (viewHolder.farmInputsTick.visibility == View.VISIBLE) {
                viewHolder.farmInputsTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onFarmInputsTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false,
                    position
                )
            } else {
                viewHolder.farmInputsTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onFarmInputsTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true,
                    position
                )
            }
        }


        viewHolder.labourExchange.setOnClickListener {
            if (viewHolder.labourExchangeTick.visibility == View.VISIBLE) {
                viewHolder.labourExchangeTick.visibility = View.GONE
                marketTransactionsAdapterCallBack.onLabourExchangeTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    false,
                    position
                )
            } else {
                viewHolder.labourExchangeTick.visibility = View.VISIBLE
                marketTransactionsAdapterCallBack.onLabourExchangeTradeClicked(
                    currentMarketTransactionItem.marketUniqueId,
                    true,
                    position
                )
            }
        }

    }

    override fun getItemCount() = marketTransactionItems.size
}