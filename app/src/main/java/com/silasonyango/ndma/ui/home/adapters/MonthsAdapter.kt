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
import com.silasonyango.ndma.ui.county.model.MonthsModel
import com.silasonyango.ndma.ui.county.model.SeasonsResponsesEnum

class MonthsAdapter(
    private val monthsModelList: MutableList<MonthsModel>,
    val monthsAdapterCallBack: MonthsAdapter.MonthsAdapterCallBack,
    val seasonsResponsesEnum: SeasonsResponsesEnum
) : RecyclerView.Adapter<MonthsAdapter.ViewHolder>() {

    interface MonthsAdapterCallBack {
        fun onMonthSelected(selectedMonth: MonthsModel, seasonsResponsesEnum: SeasonsResponsesEnum)
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
        viewHolder.tvOption.text = monthsModelList.get(position).monthName
        if (position == monthsModelList.size - 1) {
            viewHolder.stroke.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            monthsAdapterCallBack.onMonthSelected(monthsModelList.get(position),seasonsResponsesEnum)
        }
    }

    override fun getItemCount() = monthsModelList.size
}