package com.ndma.livelihoodzones.ui.county.adapters

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
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.model.HazardTypeEnum
import com.ndma.livelihoodzones.ui.model.RankResponseItem
import com.ndma.livelihoodzones.ui.wealthgroup.adapters.CropProductionListAdapter
import com.ndma.livelihoodzones.ui.wealthgroup.responses.CropProductionResponseValueModel
import com.ndma.livelihoodzones.ui.wealthgroup.responses.WgCropProductionResponseItem

class ZonalCropProductionAdapter(
    val context: Context,
    val resource: Int,
    val responseItems: MutableList<WgCropProductionResponseItem>,
    val zonalCropProductionAdapterCallBack: ZonalCropProductionAdapter.ZonalCropProductionAdapterCallBack,
    val isAResume: Boolean
) : RecyclerView.Adapter<ZonalCropProductionAdapter.ViewHolder>() {

    private var errorDialog: android.app.AlertDialog? = null

    interface ZonalCropProductionAdapterCallBack {
        fun onZoneLevelCropProductionResponseItemSubmited(
            responseItem: WgCropProductionResponseItem,
            position: Int
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.crop_cultivation_item, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentResponseItem = responseItems.get(position)
        viewHolder.cropName.text = currentResponseItem.crop.cropName

        if (isAResume) {
            viewHolder.shortRainsRainfedCultivatedAreaPercentage.setText(returnStringDoubleOrEmptyString(currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.value))
            viewHolder.shortRainsRainfedAverageYieldPerHa.setText(returnStringDoubleOrEmptyString(currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa.value))
            viewHolder.shortRainsIrrigatedCultivatedArea.setText(returnStringDoubleOrEmptyString(currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.value))
            viewHolder.shortRainsIrrigatedAverageYieldPerHa.setText(returnStringDoubleOrEmptyString(currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa.value))

            viewHolder.longRainsRainfedCultivatedAreaPercentage.setText(returnStringDoubleOrEmptyString(currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.value))
            viewHolder.longRainsRainfedAverageYieldPerHa.setText(returnStringDoubleOrEmptyString(currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa.value))
            viewHolder.longRainsIrrigatedCultivatedArea.setText(returnStringDoubleOrEmptyString(currentResponseItem.longRainsSeason.irrigatedCultivatedArea.value))
            viewHolder.longRainsIrrigatedAverageYieldPerHa.setText(returnStringDoubleOrEmptyString(currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa.value))
        }


        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({

                    if (editable == viewHolder.shortRainsRainfedCultivatedAreaPercentage.editableText) {
                        if (viewHolder.shortRainsRainfedCultivatedAreaPercentage.text.toString()
                                .trim().isNotEmpty()
                        ) {
                            currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage =
                                CropProductionResponseValueModel(
                                    viewHolder.shortRainsRainfedCultivatedAreaPercentage.text.toString()
                                        .toDouble(),
                                    true
                                )
                        }
                    }
                    if (editable == viewHolder.shortRainsRainfedAverageYieldPerHa.editableText) {
                        if (viewHolder.shortRainsRainfedAverageYieldPerHa.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    viewHolder.shortRainsRainfedAverageYieldPerHa.text.toString()
                                        .toDouble(), true
                                )
                        }
                    }
                    if (editable == viewHolder.shortRainsIrrigatedCultivatedArea.editableText) {
                        if (viewHolder.shortRainsIrrigatedCultivatedArea.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            currentResponseItem.shortRainsSeason.irrigatedCultivatedArea =
                                CropProductionResponseValueModel(
                                    viewHolder.shortRainsIrrigatedCultivatedArea.text.toString()
                                        .toDouble(), true
                                )
                        }
                    }
                    if (editable == viewHolder.shortRainsIrrigatedAverageYieldPerHa.editableText) {
                        if (viewHolder.shortRainsIrrigatedAverageYieldPerHa.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    viewHolder.shortRainsIrrigatedAverageYieldPerHa.text.toString()
                                        .toDouble(),
                                    true
                                )
                        }
                    }


                    if (editable == viewHolder.longRainsRainfedCultivatedAreaPercentage.editableText) {
                        if (viewHolder.longRainsRainfedCultivatedAreaPercentage.text.toString()
                                .trim().isNotEmpty()
                        ) {
                            currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage =
                                CropProductionResponseValueModel(
                                    viewHolder.longRainsRainfedCultivatedAreaPercentage.text.toString()
                                        .toDouble(),
                                    true
                                )
                        }
                    }
                    if (editable == viewHolder.longRainsRainfedAverageYieldPerHa.editableText) {
                        if (viewHolder.longRainsRainfedAverageYieldPerHa.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    viewHolder.longRainsRainfedAverageYieldPerHa.text.toString()
                                        .toDouble(), true
                                )
                        }
                    }
                    if (editable == viewHolder.longRainsIrrigatedCultivatedArea.editableText) {
                        if (viewHolder.longRainsIrrigatedCultivatedArea.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            currentResponseItem.longRainsSeason.irrigatedCultivatedArea =
                                CropProductionResponseValueModel(
                                    viewHolder.longRainsIrrigatedCultivatedArea.text.toString()
                                        .toDouble(), true
                                )
                        }
                    }
                    if (editable == viewHolder.longRainsIrrigatedAverageYieldPerHa.editableText) {
                        if (viewHolder.longRainsIrrigatedAverageYieldPerHa.text.toString().trim()
                                .isNotEmpty()
                        ) {
                            currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa =
                                CropProductionResponseValueModel(
                                    viewHolder.longRainsIrrigatedAverageYieldPerHa.text.toString()
                                        .toDouble(), true
                                )
                        }
                    }

                    zonalCropProductionAdapterCallBack.onZoneLevelCropProductionResponseItemSubmited(
                        currentResponseItem,
                        position
                    )

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

        viewHolder.shortRainsRainfedCultivatedAreaPercentage.addTextChangedListener(textWatcher)
        viewHolder.shortRainsRainfedAverageYieldPerHa.addTextChangedListener(textWatcher)
        viewHolder.shortRainsIrrigatedCultivatedArea.addTextChangedListener(textWatcher)
        viewHolder.shortRainsIrrigatedAverageYieldPerHa.addTextChangedListener(textWatcher)

        viewHolder.longRainsRainfedCultivatedAreaPercentage.addTextChangedListener(textWatcher)
        viewHolder.longRainsRainfedAverageYieldPerHa.addTextChangedListener(textWatcher)
        viewHolder.longRainsIrrigatedCultivatedArea.addTextChangedListener(textWatcher)
        viewHolder.longRainsIrrigatedAverageYieldPerHa.addTextChangedListener(textWatcher)
    }

    override fun getItemCount() = responseItems.size

    fun isAnyValueEmpty(currentResponseItem: WgCropProductionResponseItem): Boolean {
        return !currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
                || !currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
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

    fun returnStringDoubleOrEmptyString(value: Double): String {
        if (value != 0.0) {
            return value.toString()
        }
        return ""
    }

}