package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.WardModel

class WardAdapter(
    private val wardModelList: MutableList<WardModel>,
    val wardAdapterCallBack: WardAdapter.WardAdapterCallBack
) : RecyclerView.Adapter<WardAdapter.ViewHolder>() {

    interface WardAdapterCallBack {
        fun onWardItemClicked(selectedWard: WardModel)
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
        viewHolder.tvOption.text = wardModelList.get(position).wardName
        if (position == wardModelList.size - 1) {
            viewHolder.stroke.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            wardAdapterCallBack.onWardItemClicked(wardModelList.get(position))
        }
    }

    override fun getItemCount() = wardModelList.size
}