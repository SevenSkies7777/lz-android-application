package com.ndma.livelihoodzones.ui.county.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.*
import com.ndma.livelihoodzones.ui.home.adapters.MarketSubCountySelectionAdapter
import com.ndma.livelihoodzones.ui.home.adapters.MonthsAdapter
import com.ndma.livelihoodzones.ui.model.CropContributionEditTypeEnum
import com.ndma.livelihoodzones.ui.wealthgroup.responses.WgCropProductionResponseItem

class MarketConfigurationAdapter(
    private val context: Context,
    private val marketResponseItems: MutableList<MarketTransactionsItem>,
    private val marketConfigurationAdapterCallBack: MarketConfigurationAdapterCallBack,
    private val subCounties: MutableList<SubCountyModel>
) : RecyclerView.Adapter<MarketConfigurationAdapter.ViewHolder>(),
    MarketSubCountySelectionAdapter.MarketSubCountySelectionAdapterCallBack {

    private var marketSubCountyDialog: android.app.AlertDialog? = null

    interface MarketConfigurationAdapterCallBack {
        fun onAMarketTransactionItemEdited(
            marketTransactionsItem: MarketTransactionsItem,
            position: Int
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var marketName: EditText = view.findViewById<EditText>(R.id.marketName)
        var nearestVillageOrTown: EditText = view.findViewById<EditText>(R.id.nearestVillageOrTown)
        var subCounty: TextView = view.findViewById<TextView>(R.id.subCounty)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.market_configuration_item, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentMarketResponseItem = marketResponseItems.get(position)

        if (currentMarketResponseItem.marketName.isNotEmpty()) {
            viewHolder.marketName.setText(currentMarketResponseItem.marketName)
        }

        if (!currentMarketResponseItem.nearestVillageOrTownName.isNullOrEmpty()) {
            viewHolder.nearestVillageOrTown.setText(currentMarketResponseItem.nearestVillageOrTownName)
        }

        currentMarketResponseItem.subCounty?.let {
            viewHolder.subCounty.text = it.subCountyName
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({

                    if (editable == viewHolder.marketName.editableText) {

                        currentMarketResponseItem.marketName = viewHolder.marketName.text.toString()

                        marketConfigurationAdapterCallBack.onAMarketTransactionItemEdited(
                            currentMarketResponseItem,
                            position
                        )

                    }

                    if (editable == viewHolder.nearestVillageOrTown.editableText) {

                        currentMarketResponseItem.nearestVillageOrTownName =
                            viewHolder.nearestVillageOrTown.text.toString()

                        marketConfigurationAdapterCallBack.onAMarketTransactionItemEdited(
                            currentMarketResponseItem,
                            position
                        )

                    }

                }, 1000)
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        }

        viewHolder.marketName.addTextChangedListener(textWatcher)
        viewHolder.nearestVillageOrTown.addTextChangedListener(textWatcher)

        viewHolder.subCounty.setOnClickListener {
            inflateMarketSubCountySelectionModal(
                subCounties,
                MarketCountySelectionEnum.DYNAMIC,
                currentMarketResponseItem,
                position,
                viewHolder.subCounty
            )
        }

    }

    override fun getItemCount() = marketResponseItems.size

    private fun inflateMarketSubCountySelectionModal(
        subCounties: MutableList<SubCountyModel>,
        marketSubCountySelectionEnum: MarketCountySelectionEnum,
        marketTransactionsItem: MarketTransactionsItem,
        position: Int,
        subcountyNameTextView: TextView
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val marketSubCountySelectionAdapter = MarketSubCountySelectionAdapter(
            subCounties,
            this,
            marketSubCountySelectionEnum,
            marketTransactionsItem,
            position,
            subcountyNameTextView
        )
        val gridLayoutManager = GridLayoutManager(context, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = marketSubCountySelectionAdapter

        openMarketSubCountySelectionModal(v)
    }

    private fun openMarketSubCountySelectionModal(v: View) {
        val width =
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        marketSubCountyDialog = builder.create()
        (marketSubCountyDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            window?.setLayout(
                width,
                height
            )
        }

    }

    override fun onMarketSubCountyItemClicked(
        selectedSubCounty: SubCountyModel,
        marketCountySelectionEnum: MarketCountySelectionEnum,
        marketTransactionsItem: MarketTransactionsItem,
        marketTransactionArrayPosition: Int,
        subcountyNameTextView: TextView?
    ) {
        marketTransactionsItem.subCounty = selectedSubCounty
        subcountyNameTextView?.text = selectedSubCounty.subCountyName
        marketConfigurationAdapterCallBack.onAMarketTransactionItemEdited(
            marketTransactionsItem,
            marketTransactionArrayPosition
        )
        (marketSubCountyDialog as android.app.AlertDialog).dismiss()
    }


}