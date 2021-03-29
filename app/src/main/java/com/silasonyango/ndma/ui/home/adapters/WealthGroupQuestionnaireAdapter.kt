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
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire

class WealthGroupQuestionnaireAdapter(
    private val wealthGroupQuestionnairesList: MutableList<WealthGroupQuestionnaire>,
    val wealthGroupQuestionnaireAdapterCallBack: WealthGroupQuestionnaireAdapter.WealthGroupQuestionnaireAdapterCallBack
) : RecyclerView.Adapter<WealthGroupQuestionnaireAdapter.ViewHolder>() {

    interface WealthGroupQuestionnaireAdapterCallBack {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvQuestionnaireName: TextView = view.findViewById<TextView>(R.id.questionnaireName)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.questionnaire_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvQuestionnaireName.text = wealthGroupQuestionnairesList.get(position).questionnaireName
    }

    override fun getItemCount() = wealthGroupQuestionnairesList.size
}