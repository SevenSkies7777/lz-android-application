package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.EthnicGroupModel

class TribeSelectionAdapter(
    private val ethnicGroupModelList: MutableList<EthnicGroupModel>,
    val tribeSelectionAdapterCallBack: TribeSelectionAdapter.TribeSelectionAdapterCallBack
) : RecyclerView.Adapter<TribeSelectionAdapter.ViewHolder>() {

    interface TribeSelectionAdapterCallBack {
        fun onTribeItemSelectedFromSelectionList(selectedTribe: EthnicGroupModel)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
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
        viewHolder.tvOption.text = ethnicGroupModelList.get(position).ethnicGroupName
        viewHolder.itemView.setOnClickListener {
            tribeSelectionAdapterCallBack.onTribeItemSelectedFromSelectionList(
                ethnicGroupModelList.get(position)
            )
            viewHolder.highlightIcon.visibility = if (viewHolder.highlightIcon.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            viewHolder.uncheckedIcon.visibility = if (viewHolder.highlightIcon.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount() = ethnicGroupModelList.size
}