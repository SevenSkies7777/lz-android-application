package com.ndma.livelihoodzones.ui.home.adapters

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
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.appStore.model.CountyLevelQuestionnaire
import com.ndma.livelihoodzones.ui.model.QuestionnaireStatus


class CountyQuestionnaireAdapter(
    private val countLevelQuestionnaireList: MutableList<CountyLevelQuestionnaire>,
    val countyQuestionnaireAdapterCallBack: CountyQuestionnaireAdapterCallBack,
    val context: Context
) : RecyclerView.Adapter<CountyQuestionnaireAdapter.ViewHolder>() {
    val fontAwesome: Typeface =
        Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf")

    interface CountyQuestionnaireAdapterCallBack {
        fun onCountyLevelQuestionnaireItemClicked(countyLevelQuestionnaire: CountyLevelQuestionnaire)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvQuestionnaireName: TextView = view.findViewById<TextView>(R.id.questionnaireName)
        var collectionDate: TextView = view.findViewById<TextView>(R.id.collectionDate)
        var icon: View = view.findViewById<View>(R.id.icon)
        var icSend: TextView = view.findViewById<TextView>(R.id.icSend)
        var progressBar: ProgressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        var questionnaireStatus: TextView = view.findViewById<TextView>(R.id.questionnaireStatus)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.questionnaire_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentQuestionnaire = countLevelQuestionnaireList.get(position)

        viewHolder.icon.background = context.resources.getDrawable(
            determineQuestionnaireIcon(currentQuestionnaire.questionnaireStatus),
            null
        )
        viewHolder.icSend.setTextColor(
            context.resources.getColor(
                determineSubmitButtonColor(
                    currentQuestionnaire.questionnaireStatus
                ), null
            )
        )

        viewHolder.tvQuestionnaireName.text =
            countLevelQuestionnaireList.get(position).questionnaireName
        viewHolder.collectionDate.text =
            countLevelQuestionnaireList.get(position).questionnaireStartDate
        viewHolder.icSend.setOnClickListener {
            viewHolder.icon.visibility = View.GONE
            viewHolder.progressBar.visibility = View.VISIBLE
            viewHolder.icSend.setTextColor(context.resources.getColor(R.color.inactive))
            countyQuestionnaireAdapterCallBack.onCountyLevelQuestionnaireItemClicked(
                countLevelQuestionnaireList.get(position)
            )
        }
        viewHolder.icSend.setTypeface(fontAwesome)
        viewHolder.questionnaireStatus.text =
            determineQuestionnaireStatusText(currentQuestionnaire.questionnaireStatus)
        viewHolder.questionnaireStatus.background = context.resources.getDrawable(
            determineStatusBackground(currentQuestionnaire.questionnaireStatus),
            null
        )
    }

    private fun determineQuestionnaireStatusText(questionnaireStatus: QuestionnaireStatus): String {
        when (questionnaireStatus) {
            QuestionnaireStatus.SUBMITTED_TO_BACKEND -> return "Sync succesfully"
            QuestionnaireStatus.BACKEND_SYNC_FAILED -> return "Sync failed"
            QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION -> return "Completed"
            else -> return "Draft"
        }
    }

    private fun determineStatusBackground(questionnaireStatus: QuestionnaireStatus): Int {
        when (questionnaireStatus) {
            QuestionnaireStatus.SUBMITTED_TO_BACKEND -> return R.drawable.bg_sync_succesful
            QuestionnaireStatus.BACKEND_SYNC_FAILED -> return R.drawable.bg_sync_failed
            QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION -> return R.drawable.bg_complete_questionnaire
            else -> return R.drawable.bg_draft_status
        }
    }

    private fun determineQuestionnaireIcon(questionnaireStatus: QuestionnaireStatus): Int {
        when (questionnaireStatus) {
            QuestionnaireStatus.SUBMITTED_TO_BACKEND -> return R.drawable.ic_succesful_sync
            QuestionnaireStatus.BACKEND_SYNC_FAILED -> return R.drawable.ic_sync_failed
            QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION -> return R.drawable.ic_completed_questionnaire
            else -> return R.drawable.ic_draft
        }
    }

    private fun determineSubmitButtonColor(questionnaireStatus: QuestionnaireStatus): Int {
        when (questionnaireStatus) {
            QuestionnaireStatus.SUBMITTED_TO_BACKEND -> return R.color.inactive
            QuestionnaireStatus.BACKEND_SYNC_FAILED -> return R.color.ready_status
            QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION -> return R.color.ready_status
            else -> return R.color.inactive
        }
    }

    override fun getItemCount() = countLevelQuestionnaireList.size
}