package com.ndma.livelihoodzones.ui.wealthgroup.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.wealthgroup.responses.CropProductionResponseValueModel
import com.ndma.livelihoodzones.ui.wealthgroup.responses.WgCropProductionResponseItem

class CropProductionListAdapter(
    context: Context,
    val resource: Int,
    val responseItems: MutableList<WgCropProductionResponseItem>,
    val cropProductionListAdapterCallBack: CropProductionListAdapterCallBack,
    val isAResume: Boolean
) :
    ArrayAdapter<WgCropProductionResponseItem>(context, resource, responseItems) {
    private var errorDialog: android.app.AlertDialog? = null

    interface CropProductionListAdapterCallBack {
        fun onCropProductionResponseItemSubmited(
            responseItem: WgCropProductionResponseItem,
            position: Int
        )
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.crop_cultivation_item, null, true)

        val currentResponseItem = responseItems.get(position)

        val cropName: TextView = view.findViewById<TextView>(R.id.cropName)
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



        cropName.text = currentResponseItem.crop.cropName

        if (isAResume) {
            shortRainsRainfedCultivatedAreaPercentage.setText(currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.value.toString())
            shortRainsRainfedAverageYieldPerHa.setText(currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa.value.toString())
            shortRainsIrrigatedCultivatedArea.setText(currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.value.toString())
            shortRainsIrrigatedAverageYieldPerHa.setText(currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa.value.toString())

            longRainsRainfedCultivatedAreaPercentage.setText(currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.value.toString())
            longRainsRainfedAverageYieldPerHa.setText(currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa.value.toString())
            longRainsIrrigatedCultivatedArea.setText(currentResponseItem.longRainsSeason.irrigatedCultivatedArea.value.toString())
            longRainsIrrigatedAverageYieldPerHa.setText(currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa.value.toString())
        }


        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({

                    if (editable == shortRainsRainfedCultivatedAreaPercentage.editableText) {
                        if (shortRainsRainfedCultivatedAreaPercentage.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage =
                                CropProductionResponseValueModel(
                                    shortRainsRainfedCultivatedAreaPercentage.text.toString()
                                        .toDouble(),
                                    true
                                )
                        }
                    }
                    if (editable == shortRainsRainfedAverageYieldPerHa.editableText) {
                        if (shortRainsRainfedAverageYieldPerHa.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    shortRainsRainfedAverageYieldPerHa.text.toString().toDouble(), true
                                )
                        }
                    }
                    if (editable == shortRainsIrrigatedCultivatedArea.editableText) {
                        if (shortRainsIrrigatedCultivatedArea.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.shortRainsSeason.irrigatedCultivatedArea =
                                CropProductionResponseValueModel(
                                    shortRainsIrrigatedCultivatedArea.text.toString().toDouble(), true
                                )
                        }
                    }
                    if (editable == shortRainsIrrigatedAverageYieldPerHa.editableText) {
                        if (shortRainsIrrigatedAverageYieldPerHa.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    shortRainsIrrigatedAverageYieldPerHa.text.toString().toDouble(),
                                    true
                                )
                        }
                    }


                    if (editable == longRainsRainfedCultivatedAreaPercentage.editableText) {
                        if (longRainsRainfedCultivatedAreaPercentage.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage =
                                CropProductionResponseValueModel(
                                    longRainsRainfedCultivatedAreaPercentage.text.toString().toDouble(),
                                    true
                                )
                        }
                    }
                    if (editable == longRainsRainfedAverageYieldPerHa.editableText) {
                        if (longRainsRainfedAverageYieldPerHa.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    longRainsRainfedAverageYieldPerHa.text.toString().toDouble(), true
                                )
                        }
                    }
                    if (editable == longRainsIrrigatedCultivatedArea.editableText) {
                        if (longRainsIrrigatedCultivatedArea.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.longRainsSeason.irrigatedCultivatedArea =
                                CropProductionResponseValueModel(
                                    longRainsIrrigatedCultivatedArea.text.toString().toDouble(), true
                                )
                        }
                    }
                    if (editable == longRainsIrrigatedAverageYieldPerHa.editableText) {
                        if (longRainsIrrigatedAverageYieldPerHa.text.toString().trim().isNotEmpty()) {
                            currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    longRainsIrrigatedAverageYieldPerHa.text.toString().toDouble(), true
                                )
                        }
                    }

                    cropProductionListAdapterCallBack.onCropProductionResponseItemSubmited(
                        currentResponseItem,
                        position
                    )

                }, 1000)
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

        shortRainsRainfedCultivatedAreaPercentage.addTextChangedListener(textWatcher)
        shortRainsRainfedAverageYieldPerHa.addTextChangedListener(textWatcher)
        shortRainsIrrigatedCultivatedArea.addTextChangedListener(textWatcher)
        shortRainsIrrigatedAverageYieldPerHa.addTextChangedListener(textWatcher)

        longRainsRainfedCultivatedAreaPercentage.addTextChangedListener(textWatcher)
        longRainsRainfedAverageYieldPerHa.addTextChangedListener(textWatcher)
        longRainsIrrigatedCultivatedArea.addTextChangedListener(textWatcher)
        longRainsIrrigatedAverageYieldPerHa.addTextChangedListener(textWatcher)

        return view
    }

    fun isAnyValueEmpty(currentResponseItem: WgCropProductionResponseItem): Boolean {
        return !currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
                || !currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
    }


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