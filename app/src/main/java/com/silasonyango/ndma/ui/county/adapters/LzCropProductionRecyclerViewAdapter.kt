package com.silasonyango.ndma.ui.county.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.county.model.SubLocationModel

class LzCropProductionRecyclerViewAdapter(
        private val context: Context,
        private val cropModelList: MutableList<CropModel>,
        val lzCropProductionRecyclerViewAdapter: LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack
) : RecyclerView.Adapter<LzCropProductionRecyclerViewAdapter.ViewHolder>() {

    interface LzCropProductionRecyclerViewAdapterCallBack {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReadOrUnreadText: TextView
        val tvAccordionIcon: TextView
        val accordionStroke: View
        val accordionContent: View
        init {
            tvReadOrUnreadText = view.findViewById<TextView>(R.id.cropName)
            tvAccordionIcon = view.findViewById<TextView>(R.id.tvAccordionIcon)
            accordionStroke = view.findViewById<TextView>(R.id.accordionStroke)
            accordionContent = view.findViewById<TextView>(R.id.accordionContent)
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.lz_crop_production_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvReadOrUnreadText.text = cropModelList.get(position).cropName
        viewHolder.itemView.setOnClickListener {
            viewHolder.tvAccordionIcon.text = if (viewHolder.tvAccordionIcon.text == "+") "-" else "+"
            viewHolder.accordionStroke.visibility = if (viewHolder.accordionStroke.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            viewHolder.accordionContent.visibility = if (viewHolder.accordionContent.visibility ==  View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount() = cropModelList.size
}