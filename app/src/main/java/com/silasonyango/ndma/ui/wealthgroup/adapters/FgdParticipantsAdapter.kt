package com.silasonyango.ndma.ui.wealthgroup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.wealthgroup.model.FgdParticipantModel

class FgdParticipantsAdapter(
    val fgdParticipants: MutableList<FgdParticipantModel>,
    val fgdParticipantsAdapterCallBack: FgdParticipantsAdapter.FgdParticipantsAdapterCallBack
) : RecyclerView.Adapter<FgdParticipantsAdapter.ViewHolder>() {

    interface FgdParticipantsAdapterCallBack {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fdg_participants_item, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentParticipant = fgdParticipants.get(position)

    }

    override fun getItemCount() = fgdParticipants.size
}