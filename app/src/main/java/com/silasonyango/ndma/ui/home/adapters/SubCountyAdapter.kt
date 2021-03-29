package com.silasonyango.ndma.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.ui.county.model.SubCountyModel

class SubCountyAdapter(
    private val subCountyModelList: MutableList<SubCountyModel>,
    val subCountyAdapterCallBack: SubCountyAdapter.SubCountyAdapterCallBack
) : RecyclerView.Adapter<SubCountyAdapter.ViewHolder>() {

    interface SubCountyAdapterCallBack {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.simple_list_item_layout, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvOption.text = subCountyModelList.get(position).subCountyName
    }

    override fun getItemCount() = subCountyModelList.size
}