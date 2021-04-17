package com.silasonyango.ndma.ui.home.adapters

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire


class CountyQuestionnaireAdapter(
    private val countLevelQuestionnaireList: MutableList<CountyLevelQuestionnaire>,
    val countyQuestionnaireAdapterCallBack: CountyQuestionnaireAdapterCallBack,
    val context: Context
) : RecyclerView.Adapter<CountyQuestionnaireAdapter.ViewHolder>() {
    val fontAwesome: Typeface = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf")

    interface CountyQuestionnaireAdapterCallBack {
        fun onCountyLevelQuestionnaireItemClicked(countyLevelQuestionnaire: CountyLevelQuestionnaire)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvQuestionnaireName: TextView = view.findViewById<TextView>(R.id.questionnaireName)
        var collectionDate: TextView = view.findViewById<TextView>(R.id.collectionDate)
        var icon: View = view.findViewById<View>(R.id.icon)
        var icSend: TextView = view.findViewById<TextView>(R.id.icSend)
        var progressBar: ProgressBar = view.findViewById<ProgressBar>(R.id.progressBar)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.questionnaire_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentQuestionnaire = countLevelQuestionnaireList.get(position)

        if (currentQuestionnaire.hasBeenSubmitted) {
            viewHolder.icon.background = context.resources.getDrawable(R.drawable.ic_synced_tool)
            viewHolder.icSend.setTextColor(context.resources.getColor(R.color.inactive))
        }
        viewHolder.tvQuestionnaireName.text = countLevelQuestionnaireList.get(position).questionnaireName
        viewHolder.collectionDate.text = countLevelQuestionnaireList.get(position).questionnaireStartDate
        viewHolder.icSend.setOnClickListener {
            viewHolder.icon.visibility = View.GONE
            viewHolder.progressBar.visibility = View.VISIBLE
            viewHolder.icSend.setTextColor(context.resources.getColor(R.color.inactive))
            countyQuestionnaireAdapterCallBack.onCountyLevelQuestionnaireItemClicked(countLevelQuestionnaireList.get(position))
        }
        viewHolder.icSend.setTypeface(fontAwesome)
    }

    override fun getItemCount() = countLevelQuestionnaireList.size
}