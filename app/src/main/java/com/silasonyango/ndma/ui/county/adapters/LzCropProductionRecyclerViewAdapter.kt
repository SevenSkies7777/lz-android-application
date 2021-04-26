package com.silasonyango.ndma.ui.county.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.county.model.SubLocationModel
import com.silasonyango.ndma.ui.county.responses.LzCropProductionResponseItem

class LzCropProductionRecyclerViewAdapter(
    private val context: Context,
    private val cropModelList: MutableList<CropModel>,
    val lzCropProductionRecyclerViewAdapterCallBack: LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack
) : RecyclerView.Adapter<LzCropProductionRecyclerViewAdapter.ViewHolder>() {

    var memberViewHolder: ViewHolder? = null
    private var errorDialog: android.app.AlertDialog? = null

    init {
        val broadCastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                memberViewHolder?.apply {
                    var hasNoValidationError = true
                    if (rainfedCultivatedAreaPercentage.text.toString().isEmpty()) {
                        hasNoValidationError = false
                    }
                    if (rainfedAverageYieldPerHa.text.toString().isEmpty()) {
                        hasNoValidationError = false
                    }
                    if (irrigatedCultivatedArea.text.toString().isEmpty()) {
                        hasNoValidationError = false
                    }
                    if (irrigatedAverageYieldPerHa.text.toString().isEmpty()) {
                        hasNoValidationError = false
                    }

                    if (!hasNoValidationError) {

                    }

//                    if (hasNoValidationError) {
//                        currentCropModel?.let {
//                            lzCropProductionRecyclerViewAdapterCallBack.onCurrentCropHasNoError(
//                                LzCropProductionResponseItem(
//                                    it,
//                                    rainfedCultivatedAreaPercentage.text.toString().toDouble(),
//                                    rainfedAverageYieldPerHa.text.toString().toDouble(),
//                                    irrigatedCultivatedArea.text.toString().toDouble(),
//                                    irrigatedAverageYieldPerHa.text.toString().toDouble()
//                                )
//                            )
//                        }
//                    }
                }


            }
        }

        val filter = IntentFilter()
        filter.addAction(Constants.LZ_CROPS_NEXT_BUTTON_CLICKED)
        context.applicationContext.registerReceiver(broadCastReceiver, filter)
    }


    interface LzCropProductionRecyclerViewAdapterCallBack {
        fun onCurrentCropHasNoError(
            lzCropProductionResponseItem: LzCropProductionResponseItem
        )

        fun onACropHasAValidationError()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cropName: TextView
        val tvAccordionIcon: TextView
        val accordionStroke: View
        val accordionContent: View
        val rainfedCultivatedAreaPercentage: EditText
        val rainfedAverageYieldPerHa: EditText
        val irrigatedCultivatedArea: EditText
        val irrigatedAverageYieldPerHa: EditText
        val submitButton: TextView

        init {
            cropName = view.findViewById<TextView>(R.id.cropName)
            tvAccordionIcon = view.findViewById<TextView>(R.id.tvAccordionIcon)
            accordionStroke = view.findViewById<TextView>(R.id.accordionStroke)
            accordionContent = view.findViewById<TextView>(R.id.accordionContent)
            rainfedCultivatedAreaPercentage =
                view.findViewById<EditText>(R.id.rainfedCultivatedAreaPercentage)
            rainfedAverageYieldPerHa = view.findViewById<EditText>(R.id.rainfedAverageYieldPerHa)
            irrigatedCultivatedArea = view.findViewById<EditText>(R.id.irrigatedCultivatedArea)
            irrigatedAverageYieldPerHa =
                view.findViewById<EditText>(R.id.irrigatedAverageYieldPerHa)
            submitButton = view.findViewById<TextView>(R.id.cropSubmitButton)
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.lz_crop_production_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentCropModel = cropModelList.get(position)
        memberViewHolder = viewHolder
        viewHolder.cropName.text = cropModelList.get(position).cropName
        viewHolder.tvAccordionIcon.setOnClickListener {
            viewHolder.tvAccordionIcon.text =
                if (viewHolder.tvAccordionIcon.text == "+") "-" else "+"
            viewHolder.accordionStroke.visibility =
                if (viewHolder.accordionStroke.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            viewHolder.accordionContent.visibility =
                if (viewHolder.accordionContent.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        viewHolder.itemView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_UP -> inflateErrorModal("Sasa", "sasa")
                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        viewHolder.submitButton.setOnClickListener {
            viewHolder?.apply {
                var hasNoValidationError = true
                if (rainfedCultivatedAreaPercentage.text.toString().isEmpty()) {
                    hasNoValidationError = false
                }
                if (rainfedAverageYieldPerHa.text.toString().isEmpty()) {
                    hasNoValidationError = false
                }
                if (irrigatedCultivatedArea.text.toString().isEmpty()) {
                    hasNoValidationError = false
                }
                if (irrigatedAverageYieldPerHa.text.toString().isEmpty()) {
                    hasNoValidationError = false
                }

                if (hasNoValidationError) {
                    currentCropModel?.let {
                        lzCropProductionRecyclerViewAdapterCallBack.onCurrentCropHasNoError(
                            LzCropProductionResponseItem(
                                it,
                                rainfedCultivatedAreaPercentage.text.toString().toDouble(),
                                rainfedAverageYieldPerHa.text.toString().toDouble(),
                                irrigatedCultivatedArea.text.toString().toDouble(),
                                irrigatedAverageYieldPerHa.text.toString().toDouble()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun inflateErrorModal(errorTitle: String, errorMessage: String) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.error_message_layout, null)
        val title = v.findViewById<TextView>(R.id.title)
        val message = v.findViewById<TextView>(R.id.message)
        val close = v.findViewById<TextView>(R.id.close)
        title.text = errorTitle
        message.text = errorMessage
        close.setOnClickListener {
            (errorDialog as android.app.AlertDialog).cancel()
        }

        openErrorModal(v)
    }

    private fun openErrorModal(v: View) {
        val width =
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        errorDialog = builder.create()
        (errorDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            window?.setLayout(
                width,
                height
            )
        }

    }

    override fun getItemCount() = cropModelList.size
}