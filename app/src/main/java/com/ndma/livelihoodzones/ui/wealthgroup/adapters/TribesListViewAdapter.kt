package com.ndma.livelihoodzones.ui.wealthgroup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.EthnicGroupModel

class TribesListViewAdapter(
    context: Context,
    val resource: Int,
    val ethnicGroups: MutableList<EthnicGroupModel>,
    val tribesListViewAdapterCallBack: TribesListViewAdapter.TribesListViewAdapterCallBack
) :
    ArrayAdapter<EthnicGroupModel>(context, resource, ethnicGroups) {

    interface TribesListViewAdapterCallBack {
        fun onATribeSelected(currentTribe: EthnicGroupModel, position: Int)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.lz_selection_item, null, true)
        val tvOption: TextView = view.findViewById<TextView>(R.id.tvOption)
        val highlightIcon: View = view.findViewById<View>(R.id.highlightIcon)
        val uncheckedIcon: View = view.findViewById<View>(R.id.uncheckedIcon)

        val currentTribe = ethnicGroups.get(position)
        tvOption.text = currentTribe.ethnicGroupName

        if (currentTribe.hasBeenSelected) {
            highlightIcon.visibility = View.VISIBLE
            uncheckedIcon.visibility = View.GONE
        } else {
            highlightIcon.visibility = View.GONE
            uncheckedIcon.visibility = View.VISIBLE
        }

        view.setOnClickListener {
            if (highlightIcon.visibility == View.VISIBLE) {
                currentTribe.hasBeenSelected = false
                highlightIcon.visibility = View.GONE
                uncheckedIcon.visibility = View.VISIBLE
            } else {
                currentTribe.hasBeenSelected = true
                highlightIcon.visibility = View.VISIBLE
                uncheckedIcon.visibility = View.GONE
            }
            tribesListViewAdapterCallBack.onATribeSelected(currentTribe, position)
        }

        return view
    }

    override fun getCount(): Int {
        return ethnicGroups.size
    }
}