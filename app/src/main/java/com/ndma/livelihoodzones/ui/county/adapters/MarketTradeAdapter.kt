package com.ndma.livelihoodzones.ui.county.adapters

import android.content.Context
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
import com.ndma.livelihoodzones.ui.county.model.SubLocationModel
import com.ndma.livelihoodzones.ui.home.adapters.MarketTransactionsAdapter

class MarketTradeAdapter(
    val marketTransactionItems: MutableList<MarketTransactionsItem>,
    val marketTradeAdapterCallBack: MarketTradeAdapter.MarketTradeAdapterCallBack
) : RecyclerView.Adapter<MarketTradeAdapter.ViewHolder>() {

    interface MarketTradeAdapterCallBack {
        fun onAMarketTradeUpdated(marketTransactionsItem: MarketTransactionsItem, position: Int)
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
        val currentItem = marketTransactionItems.get(position)
        viewHolder.marketName.text = currentItem.marketName


        viewHolder.livestockTick.isVisible = currentItem.livestockTrade
        viewHolder.poultryTick.isVisible = currentItem.poultryTrade
        viewHolder.farmProduceTick.isVisible = currentItem.farmProduceTrade
        viewHolder.foodProduceTick.isVisible = currentItem.foodProduceRetail
        viewHolder.farmInputsTick.isVisible = currentItem.retailFarmInput
        viewHolder.labourExchangeTick.isVisible = currentItem.labourExchange


        viewHolder.livestock.setOnClickListener {
            if (!currentItem.livestockTrade) {
                currentItem.livestockTrade = true
                viewHolder.livestockTick.isVisible = true
            } else {
                currentItem.livestockTrade = false
                viewHolder.livestockTick.isVisible = false
            }
            marketTradeAdapterCallBack.onAMarketTradeUpdated(currentItem,position)
        }

        viewHolder.poultry.setOnClickListener {
            if (!currentItem.poultryTrade) {
                currentItem.poultryTrade = true
                viewHolder.poultryTick.isVisible = true
            } else {
                currentItem.poultryTrade = false
                viewHolder.poultryTick.isVisible = false
            }
            marketTradeAdapterCallBack.onAMarketTradeUpdated(currentItem,position)
        }

        viewHolder.farmProduce.setOnClickListener {
            if (!currentItem.farmProduceTrade) {
                currentItem.farmProduceTrade = true
                viewHolder.farmProduceTick.isVisible = true
            } else {
                currentItem.farmProduceTrade = false
                viewHolder.farmProduceTick.isVisible = false
            }
            marketTradeAdapterCallBack.onAMarketTradeUpdated(currentItem,position)
        }

        viewHolder.foodProduce.setOnClickListener {
            if (!currentItem.foodProduceRetail) {
                currentItem.foodProduceRetail = true
                viewHolder.foodProduceTick.isVisible = true
            } else {
                currentItem.foodProduceRetail = false
                viewHolder.foodProduceTick.isVisible = false
            }
            marketTradeAdapterCallBack.onAMarketTradeUpdated(currentItem,position)
        }

        viewHolder.farmInputs.setOnClickListener {
            if (!currentItem.retailFarmInput) {
                currentItem.retailFarmInput = true
                viewHolder.farmInputsTick.isVisible = true
            } else {
                currentItem.retailFarmInput = false
                viewHolder.farmInputsTick.isVisible = false
            }
            marketTradeAdapterCallBack.onAMarketTradeUpdated(currentItem,position)
        }

        viewHolder.labourExchange.setOnClickListener {
            if (!currentItem.labourExchange) {
                currentItem.labourExchange = true
                viewHolder.labourExchangeTick.isVisible = true
            } else {
                currentItem.labourExchange = false
                viewHolder.labourExchangeTick.isVisible = false
            }
            marketTradeAdapterCallBack.onAMarketTradeUpdated(currentItem,position)
        }
    }

    override fun getItemCount() = marketTransactionItems.size
}