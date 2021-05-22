package com.ndma.livelihoodzones.ui.home.adapters

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.EthnicityResponseItem

class EthnicityAdapter(
    private val ethnicityResponseItemList: MutableList<EthnicityResponseItem>,
    val ethnicityAdapterCallBack: EthnicityAdapter.EthnicityAdapterCallBack
) : RecyclerView.Adapter<EthnicityAdapter.ViewHolder>() {

    interface EthnicityAdapterCallBack {
        fun onAnEthnicityResponseUpdated(ethnicityResponseItem: EthnicityResponseItem, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ethnicityName: TextView = view.findViewById<TextView>(R.id.ethnicityName)
        var populationPercentage: EditText = view.findViewById<EditText>(R.id.populationPercentage)
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

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({

                    if (editable.toString().isNotEmpty()) {
                        currentEthnicityResponseItem.populationPercentage = editable.toString().toDouble()
                        ethnicityAdapterCallBack.onAnEthnicityResponseUpdated(currentEthnicityResponseItem, position)
                    }

                }, 500)
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        }
        viewHolder.populationPercentage.addTextChangedListener(textWatcher)
    }

    override fun getItemCount() = ethnicityResponseItemList.size
}