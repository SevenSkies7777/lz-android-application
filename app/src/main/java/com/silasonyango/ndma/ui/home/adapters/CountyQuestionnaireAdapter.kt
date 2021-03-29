package com.silasonyango.ndma.ui.home.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.ui.county.adapters.LzCropProductionRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.model.CropModel

class CountyQuestionnaireAdapter(
    private val countLevelQuestionnaireList: MutableList<CountyLevelQuestionnaire>,
    val countyQuestionnaireAdapterCallBack: CountyQuestionnaireAdapterCallBack
) : RecyclerView.Adapter<CountyQuestionnaireAdapter.ViewHolder>() {

    interface CountyQuestionnaireAdapterCallBack {

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
        viewHolder.tvQuestionnaireName.text = countLevelQuestionnaireList.get(position).questionnaireName
    }

    override fun getItemCount() = countLevelQuestionnaireList.size
}