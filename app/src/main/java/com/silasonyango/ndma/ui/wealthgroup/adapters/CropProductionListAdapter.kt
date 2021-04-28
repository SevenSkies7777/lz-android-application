package com.silasonyango.ndma.ui.wealthgroup.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.county.responses.LzCropProductionResponseItem
import com.silasonyango.ndma.ui.wealthgroup.responses.CropProductionResponseValueModel
import com.silasonyango.ndma.ui.wealthgroup.responses.WgCropProductionResponseItem

class CropProductionListAdapter(
    context: Context,
    val resource: Int,
    val responseItems: MutableList<WgCropProductionResponseItem>,
    val cropProductionListAdapterCallBack: CropProductionListAdapterCallBack
) :
    ArrayAdapter<WgCropProductionResponseItem>(context, resource, responseItems) {
    private var errorDialog: android.app.AlertDialog? = null

    interface CropProductionListAdapterCallBack {
        fun onCropProductionResponseItemSubmited(responseItem: WgCropProductionResponseItem, position: Int)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.lz_crop_production_item, null, true)

        val currentResponseItem = responseItems.get(position)

        val cropName: TextView = view.findViewById<TextView>(R.id.cropName)
        val tvAccordionIcon: TextView = view.findViewById<TextView>(R.id.tvAccordionIcon)
        val accordionStroke: View = view.findViewById<TextView>(R.id.accordionStroke)
        val accordionContent: View = view.findViewById<TextView>(R.id.accordionContent)
        val shortRainsRainfedCultivatedAreaPercentage: EditText =
            view.findViewById<EditText>(R.id.shortRainsRainfedCultivatedAreaPercentage)
        val shortRainsRainfedAverageYieldPerHa: EditText =
            view.findViewById<EditText>(R.id.shortRainsRainfedAverageYieldPerHa)
        val shortRainsIrrigatedCultivatedArea: EditText =
            view.findViewById<EditText>(R.id.shortRainsIrrigatedCultivatedArea)
        val shortRainsIrrigatedAverageYieldPerHa: EditText =
            view.findViewById<EditText>(R.id.shortRainsIrrigatedAverageYieldPerHa)

        val longRainsRainfedCultivatedAreaPercentage: EditText =
            view.findViewById<EditText>(R.id.longRainsRainfedCultivatedAreaPercentage)
        val longRainsRainfedAverageYieldPerHa: EditText =
            view.findViewById<EditText>(R.id.longRainsRainfedAverageYieldPerHa)
        val longRainsIrrigatedCultivatedArea: EditText =
            view.findViewById<EditText>(R.id.longRainsIrrigatedCultivatedArea)
        val longRainsIrrigatedAverageYieldPerHa: EditText =
            view.findViewById<EditText>(R.id.longRainsIrrigatedAverageYieldPerHa)

        val submitButton: TextView = view.findViewById<TextView>(R.id.cropSubmitButton)

        cropName.text = currentResponseItem.crop.cropName

        if (isAnyValueEmpty(currentResponseItem)) {
            tvAccordionIcon.text == "-"
            accordionStroke.visibility = View.VISIBLE
            accordionContent.visibility = View.VISIBLE
        } else {
            tvAccordionIcon.text == "+"
            accordionStroke.visibility = View.GONE
            accordionContent.visibility = View.GONE
        }

        tvAccordionIcon.setOnClickListener {
            if (shortRainsRainfedCultivatedAreaPercentage.text.toString().isEmpty() || shortRainsRainfedAverageYieldPerHa.text.toString().isEmpty() || shortRainsIrrigatedCultivatedArea.text.toString().isEmpty() || shortRainsIrrigatedAverageYieldPerHa.text.toString().isEmpty()
                || longRainsRainfedCultivatedAreaPercentage.text.toString().isEmpty() || longRainsRainfedAverageYieldPerHa.text.toString().isEmpty() || longRainsIrrigatedCultivatedArea.text.toString().isEmpty() || longRainsIrrigatedAverageYieldPerHa.text.toString().isEmpty()) {
                tvAccordionIcon.text == "-"
                accordionStroke.visibility = View.VISIBLE
                accordionContent.visibility = View.VISIBLE
            } else {
                tvAccordionIcon.text == "+"
                accordionStroke.visibility = View.GONE
                accordionContent.visibility = View.GONE
            }
        }

        submitButton.setOnClickListener {
            if (shortRainsRainfedCultivatedAreaPercentage.text.toString().isEmpty() || shortRainsRainfedAverageYieldPerHa.text.toString().isEmpty() || shortRainsIrrigatedCultivatedArea.text.toString().isEmpty() || shortRainsIrrigatedAverageYieldPerHa.text.toString().isEmpty()
                || longRainsRainfedCultivatedAreaPercentage.text.toString().isEmpty() || longRainsRainfedAverageYieldPerHa.text.toString().isEmpty() || longRainsIrrigatedCultivatedArea.text.toString().isEmpty() || longRainsIrrigatedAverageYieldPerHa.text.toString().isEmpty()) {
                tvAccordionIcon.text == "-"
                accordionStroke.visibility = View.VISIBLE
                accordionContent.visibility = View.VISIBLE
                inflateErrorModal("Missing data", "Fill out or the missing fields")
            } else {
                tvAccordionIcon.text == "+"
                accordionStroke.visibility = View.GONE
                accordionContent.visibility = View.GONE
                currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage = CropProductionResponseValueModel(shortRainsRainfedCultivatedAreaPercentage.text.toString().toDouble(),true)
                currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa = CropProductionResponseValueModel(shortRainsRainfedAverageYieldPerHa.text.toString().toDouble(),true)
                currentResponseItem.shortRainsSeason.irrigatedCultivatedArea = CropProductionResponseValueModel(shortRainsIrrigatedCultivatedArea.text.toString().toDouble(),true)
                currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa = CropProductionResponseValueModel(shortRainsIrrigatedAverageYieldPerHa.text.toString().toDouble(),true)

                currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage = CropProductionResponseValueModel(longRainsRainfedCultivatedAreaPercentage.text.toString().toDouble(),true)
                currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa = CropProductionResponseValueModel(longRainsRainfedAverageYieldPerHa.text.toString().toDouble(),true)
                currentResponseItem.longRainsSeason.irrigatedCultivatedArea = CropProductionResponseValueModel(longRainsIrrigatedCultivatedArea.text.toString().toDouble(),true)
                currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa = CropProductionResponseValueModel(longRainsIrrigatedAverageYieldPerHa.text.toString().toDouble(),true)
                cropProductionListAdapterCallBack.onCropProductionResponseItemSubmited(currentResponseItem,position)
            }
        }

        return view
    }

    fun isAnyValueEmpty(currentResponseItem: WgCropProductionResponseItem): Boolean {
        return !currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
                || !currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted}


    override fun getCount(): Int {
        return responseItems.size
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
}