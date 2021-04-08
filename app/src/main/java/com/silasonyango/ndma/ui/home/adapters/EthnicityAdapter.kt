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

class EthnicityAdapter(
    private val ethnicityResponseItemList: MutableList<EthnicityResponseItem>,
    val ethnicityAdapterCallBack: EthnicityAdapter.EthnicityAdapterCallBack
) : RecyclerView.Adapter<EthnicityAdapter.ViewHolder>() {

    interface EthnicityAdapterCallBack {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ethnicityName: TextView = view.findViewById<TextView>(R.id.ethnicityName)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.ethnicity_table_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentEthnicityResponseItem = ethnicityResponseItemList.get(position)
        viewHolder.ethnicityName.text = currentEthnicityResponseItem.ethnicGroupModel.ethnicGroupName
    }

    override fun getItemCount() = ethnicityResponseItemList.size
}