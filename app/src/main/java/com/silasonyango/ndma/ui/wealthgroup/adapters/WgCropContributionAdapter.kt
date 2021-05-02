package com.silasonyango.ndma.ui.wealthgroup.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.home.adapters.WgCropContributionRankAdapter
import com.silasonyango.ndma.ui.model.CropContributionEditTypeEnum
import com.silasonyango.ndma.ui.model.CropContributionRankTypeEnum
import com.silasonyango.ndma.ui.model.RankResponseItem
import com.silasonyango.ndma.ui.wealthgroup.responses.WgCropContributionResponseItem

class WgCropContributionAdapter(
    private val cropContributionResponses: MutableList<WgCropContributionResponseItem>,
    val wgCropContributionAdapterCallBack: WgCropContributionAdapterCallBack,
    val context: Context,
    val cropCashIncomeContributionRanks: MutableList<RankResponseItem>,
    val cropFoodConsumptionContributionRanks: MutableList<RankResponseItem>
) : RecyclerView.Adapter<WgCropContributionAdapter.ViewHolder>(),
    WgCropContributionRankAdapter.RankAdapterCallBack {

    private var rankDialog: AlertDialog? = null

    var currentViewHolder: ViewHolder? = null

    interface WgCropContributionAdapterCallBack {
        fun onAnyFieldEdited(
            currentResponseItem: WgCropContributionResponseItem,
            position: Int,
            cropContributionEditTypeEnum: CropContributionEditTypeEnum,
            selectedCashIncomeContributionRank: RankResponseItem?,
            selectedFoodConsumptionContributionRank: RankResponseItem?
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cropName: TextView = view.findViewById<TextView>(R.id.cropName)
        var cashIncomeRank: TextView = view.findViewById<TextView>(R.id.cashIncomeRank)
        var cashIncomeApproxPercentage: EditText =
            view.findViewById<EditText>(R.id.cashIncomeApproxPercentage)
        var foodConsumptionRank: TextView = view.findViewById<TextView>(R.id.foodConsumptionRank)
        var foodApproxPercentage: EditText = view.findViewById<EditText>(R.id.foodApproxPercentage)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.wg_crop_contribution_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        currentViewHolder = viewHolder
        val currentResponseItem = cropContributionResponses.get(position)
        viewHolder.cropName.text = currentResponseItem.cropModel.cropName
        viewHolder.cashIncomeRank.text =
            if (currentResponseItem.cashIncomeRank.hasBeenSubmitted) currentResponseItem.cashIncomeRank.actualValue.toInt().toString() else "Select rank..."
        viewHolder.cashIncomeApproxPercentage.setText(if (currentResponseItem.cashIncomeApproxPercentage.hasBeenSubmitted) currentResponseItem.cashIncomeApproxPercentage.actualValue.toString() else "")
        viewHolder.foodConsumptionRank.text =
            if (currentResponseItem.foodConsumptionRank.hasBeenSubmitted) currentResponseItem.foodConsumptionRank.actualValue.toInt().toString() else "Select rank..."
        viewHolder.foodApproxPercentage.setText(if (currentResponseItem.foodConsumptionApproxPercentage.hasBeenSubmitted) currentResponseItem.foodConsumptionApproxPercentage.actualValue.toString() else "")
        viewHolder.cashIncomeRank.setOnClickListener {
            inflateRankModal(
                cropCashIncomeContributionRanks,
                CropContributionRankTypeEnum.CASH_INCOME_RANK,
                currentResponseItem,
                position
            )
        }

        viewHolder.foodConsumptionRank.setOnClickListener {
            inflateRankModal(
                cropFoodConsumptionContributionRanks,
                CropContributionRankTypeEnum.FOOD_CONSUMPTION_RANK,
                currentResponseItem,
                position
            )
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({

                    if (editable == viewHolder.cashIncomeApproxPercentage.editableText) {
                        currentResponseItem.cashIncomeApproxPercentage.actualValue =
                            if (viewHolder.cashIncomeApproxPercentage.text.toString()
                                    .isNotEmpty()
                            ) viewHolder.cashIncomeApproxPercentage.text.toString()
                                .toDouble() else 0.0
                        currentResponseItem.cashIncomeApproxPercentage.hasBeenSubmitted =
                            viewHolder.cashIncomeApproxPercentage.text.toString()
                                .isNotEmpty()
                        wgCropContributionAdapterCallBack.onAnyFieldEdited(
                            currentResponseItem,
                            position,
                            CropContributionEditTypeEnum.NORMAL_EDIT_TEXT,
                            null,
                            null
                        )
                    }

                    if (editable == viewHolder.foodApproxPercentage.editableText) {
                        currentResponseItem.foodConsumptionApproxPercentage.actualValue =
                            if (viewHolder.foodApproxPercentage.text.toString()
                                    .isNotEmpty()
                            ) viewHolder.foodApproxPercentage.text.toString()
                                .toDouble() else 0.0
                        currentResponseItem.foodConsumptionApproxPercentage.hasBeenSubmitted =
                            viewHolder.foodApproxPercentage.text.toString()
                                .isNotEmpty()
                        wgCropContributionAdapterCallBack.onAnyFieldEdited(
                            currentResponseItem,
                            position,
                            CropContributionEditTypeEnum.NORMAL_EDIT_TEXT,
                            null,
                            null
                        )
                    }

                }, 2000)
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

        viewHolder.cashIncomeApproxPercentage.addTextChangedListener(textWatcher)
        viewHolder.foodApproxPercentage.addTextChangedListener(textWatcher)
    }

    override fun getItemCount() = cropContributionResponses.size


    private fun inflateRankModal(
        contributionRanks: MutableList<RankResponseItem>,
        cropContributionRankTypeEnum: CropContributionRankTypeEnum,
        currentResponseItem: WgCropContributionResponseItem,
        currentCropResponseItemPosition: Int
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)
        val list: RecyclerView = v.findViewById(R.id.listRv)

        val ranksAdapter =
            WgCropContributionRankAdapter(
                contributionRanks,
                this,
                cropContributionRankTypeEnum,
                currentResponseItem,
                currentCropResponseItemPosition
            )
        val gridLayoutManager = GridLayoutManager(context, 1)
        list.layoutManager = gridLayoutManager
        list.hasFixedSize()
        list.adapter = ranksAdapter

        openRankModal(v)
    }

    private fun openRankModal(v: View) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        rankDialog = builder.create()
        (rankDialog as AlertDialog).setCancelable(true)
        (rankDialog as AlertDialog).setCanceledOnTouchOutside(true)
        (rankDialog as AlertDialog).window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        (rankDialog as AlertDialog).show()
        val window = (rankDialog as AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onARankItemSelected(
        selectedRankItem: RankResponseItem,
        position: Int,
        cropContributionRankTypeEnum: CropContributionRankTypeEnum,
        currentCropResponseItem: WgCropContributionResponseItem,
        currentCropResponseItemPosition: Int
    ) {
        if (cropContributionRankTypeEnum == CropContributionRankTypeEnum.CASH_INCOME_RANK) {
            currentCropResponseItem.cashIncomeRank.actualValue =
                selectedRankItem.rankPosition.toDouble()
            currentCropResponseItem.cashIncomeRank.hasBeenSubmitted = true
            currentViewHolder?.cashIncomeRank?.text = selectedRankItem.rankPosition.toString()
            wgCropContributionAdapterCallBack.onAnyFieldEdited(
                currentCropResponseItem,
                currentCropResponseItemPosition,
                CropContributionEditTypeEnum.CROP_CASH_INCOME_CONTRIBUTION_RANK,
                selectedRankItem,
                null
            )
        }

        if (cropContributionRankTypeEnum == CropContributionRankTypeEnum.FOOD_CONSUMPTION_RANK) {
            currentCropResponseItem.foodConsumptionRank.actualValue =
                selectedRankItem.rankPosition.toDouble()
            currentCropResponseItem.foodConsumptionRank.hasBeenSubmitted = true
            currentViewHolder?.foodConsumptionRank?.text = selectedRankItem.rankPosition.toString()
            wgCropContributionAdapterCallBack.onAnyFieldEdited(
                currentCropResponseItem,
                currentCropResponseItemPosition,
                CropContributionEditTypeEnum.CROP_FOOD_CONSUMPTION_CONTRIBUTION_RANK,
                null,
                selectedRankItem
            )
        }
        (rankDialog as AlertDialog).dismiss()
    }

}