package com.ndma.livelihoodzones.ui.county.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.MonthsModel
import com.ndma.livelihoodzones.ui.county.model.SeasonsResponsesEnum
import com.ndma.livelihoodzones.ui.home.adapters.MonthsAdapter
import com.ndma.livelihoodzones.ui.wealthgroup.responses.WgCropProductionResponseItem

class HarvestingSeasonsAdapter(
    private val context: Context,
    private val cropResponseModelList: MutableList<WgCropProductionResponseItem>,
    private val months: MutableList<MonthsModel>
) : RecyclerView.Adapter<HarvestingSeasonsAdapter.ViewHolder>(), MonthsAdapter.MonthsAdapterCallBack{
    private var seasonCalendarDialog: android.app.AlertDialog? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cropName: TextView = view.findViewById<TextView>(R.id.cropName)
        var selectRankText: TextView = view.findViewById<TextView>(R.id.selectRankText)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.crop_seasons_calendar_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentCropResponseItem = cropResponseModelList.get(position)
        viewHolder.cropName.text = currentCropResponseItem.crop.cropName
        viewHolder.selectRankText.setOnClickListener {
            inflateSeasonCalendarModal(
                months,
                SeasonsResponsesEnum.DYNAMIC_LAND_PREPARATION
            )
        }
    }

    override fun getItemCount() = cropResponseModelList.size

    private fun inflateSeasonCalendarModal(
        months: MutableList<MonthsModel>,
        seasonsResponsesEnum: SeasonsResponsesEnum
    ) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)
        val icClose = v.findViewById<View>(R.id.icClose)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val monthsAdapter = MonthsAdapter(
            months,
            this,
            seasonsResponsesEnum
        )

        icClose.setOnClickListener {
            (seasonCalendarDialog as android.app.AlertDialog).dismiss()
        }

        val gridLayoutManager = GridLayoutManager(context, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = monthsAdapter

        openSeasonCalendarModal(v)
    }

    private fun openSeasonCalendarModal(v: View) {
        val width =
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        seasonCalendarDialog = builder.create()
        (seasonCalendarDialog as android.app.AlertDialog).apply {
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

    override fun onMonthSelected(
        selectedMonth: MonthsModel,
        seasonsResponsesEnum: SeasonsResponsesEnum
    ) {

    }
}