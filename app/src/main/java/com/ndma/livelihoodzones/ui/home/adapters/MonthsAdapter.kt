package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.MonthsModel
import com.ndma.livelihoodzones.ui.county.model.SeasonsResponsesEnum
import com.ndma.livelihoodzones.ui.wealthgroup.responses.WgCropProductionResponseItem

class MonthsAdapter(
    private val monthsModelList: MutableList<MonthsModel>,
    val monthsAdapterCallBack: MonthsAdapter.MonthsAdapterCallBack,
    val seasonsResponsesEnum: SeasonsResponsesEnum,
    val cropResponseItem: WgCropProductionResponseItem? = null
) : RecyclerView.Adapter<MonthsAdapter.ViewHolder>() {

    interface MonthsAdapterCallBack {
        fun onMonthSelected(selectedMonth: MonthsModel, seasonsResponsesEnum: SeasonsResponsesEnum,cropResponseItem: WgCropProductionResponseItem? = null)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
        var stroke: View = view.findViewById<View>(R.id.stroke)
        var highlightIcon: View = view.findViewById<View>(R.id.highlightIcon)
        var uncheckedIcon: View = view.findViewById<View>(R.id.uncheckedIcon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.lz_selection_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvOption.text = monthsModelList.get(position).monthName
        if (position == monthsModelList.size - 1) {
            viewHolder.stroke.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            monthsAdapterCallBack.onMonthSelected(monthsModelList.get(position),seasonsResponsesEnum,cropResponseItem)
            viewHolder.highlightIcon.visibility = if (viewHolder.highlightIcon.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            viewHolder.uncheckedIcon.visibility = if (viewHolder.highlightIcon.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount() = monthsModelList.size
}