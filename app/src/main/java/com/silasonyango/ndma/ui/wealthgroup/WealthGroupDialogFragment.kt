package com.silasonyango.ndma.ui.wealthgroup


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.databinding.WealthGroupQuestionnaireLayoutBinding
import com.silasonyango.ndma.login.model.GeographyObject
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.county.model.QuestionnaireSessionLocation
import com.silasonyango.ndma.ui.county.responses.LzCropProductionResponseItem
import com.silasonyango.ndma.ui.home.HomeViewModel
import com.silasonyango.ndma.ui.model.QuestionnaireStatus
import com.silasonyango.ndma.ui.wealthgroup.adapters.CropProductionListAdapter
import com.silasonyango.ndma.ui.wealthgroup.adapters.CropSelectionListAdapter
import com.silasonyango.ndma.ui.wealthgroup.responses.*
import com.silasonyango.ndma.util.Util

class WealthGroupDialogFragment : DialogFragment(),
    CropSelectionListAdapter.CropSelectionListAdapterCallBack, CropProductionListAdapter.CropProductionListAdapterCallBack {

    private lateinit var wealthGroupViewModel: WealthGroupViewModel

    private lateinit var binding: WealthGroupQuestionnaireLayoutBinding

    private lateinit var wealthGroupQuestionnaire: WealthGroupQuestionnaire

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    lateinit var geographyObject: GeographyObject

    var questionnaireSessionLocation: QuestionnaireSessionLocation? = null

    private var subContyDialog: AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    private lateinit var homeViewModel: HomeViewModel

    private var cropProductionResponseItems: MutableList<WgCropProductionResponseItem> = ArrayList()

    private var crops: MutableList<CropModel> = ArrayList()

    companion object {

        private const val QUESTIONNAIRE_ID = "questionnaireId"

        private const val QUESTIONNAIRE_NAME = "questionnaireName"

        private const val QUESTIONNAIRE_SESSION_LOCATION = "sessionLocation"

        @JvmStatic
        fun newInstance(
            questionnaireId: String,
            questionnaireName: String,
            questionnaireSessionLocation: QuestionnaireSessionLocation
        ) =
            WealthGroupDialogFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(QUESTIONNAIRE_ID, questionnaireId)
                        putString(QUESTIONNAIRE_NAME, questionnaireName)
                        putParcelable(QUESTIONNAIRE_SESSION_LOCATION, questionnaireSessionLocation)
                    }
                }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            questionnaireId = it.getString(QUESTIONNAIRE_ID)

            questionnaireName = it.getString(QUESTIONNAIRE_NAME)

            questionnaireSessionLocation =
                it.getParcelable<QuestionnaireSessionLocation>(QUESTIONNAIRE_SESSION_LOCATION)

            wealthGroupQuestionnaire =
                questionnaireId?.let { it1 ->
                    questionnaireName?.let { it2 ->
                        WealthGroupQuestionnaire(
                            it1,
                            it2
                        )
                    }
                }!!

            wealthGroupQuestionnaire.questionnaireGeography = this.questionnaireSessionLocation!!
            wealthGroupQuestionnaire.questionnaireStartDate = Util.getNow()
            wealthGroupQuestionnaire.questionnaireName =
                AppStore.getInstance().sessionDetails?.geography?.county?.countyName + " " +
                        wealthGroupQuestionnaire.questionnaireGeography.selectedLivelihoodZone.livelihoodZoneName + " Livelihood Zone " + wealthGroupQuestionnaire.questionnaireGeography.selectedWealthGroup.wealthGroupName + " questionnaire"
        }

        inflateSubCountyModal()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        wealthGroupViewModel =
            ViewModelProvider(this).get(WealthGroupViewModel::class.java)
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = WealthGroupQuestionnaireLayoutBinding.inflate(inflater, container, false)

        val gson = Gson()
        val sharedPreferences: SharedPreferences? =
            context?.applicationContext?.getSharedPreferences(
                "MyPref",
                Context.MODE_PRIVATE
            )
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        val geographyString =
            sharedPreferences?.getString(Constants.GEOGRAPHY_OBJECT, null)
        geographyObject =
            gson.fromJson(
                geographyString,
                GeographyObject::class.java
            )
        crops = geographyObject.crops
        defineViews()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(matchParent, matchParent)
            window?.setBackgroundDrawable(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineViews() {
        defineNavigation()
        defineIncomeandFoodSource()
    }

    private fun defineIncomeandFoodSource() {
        binding.apply {
            wgIncomeAndFoodSources.apply {

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineNavigation() {
        binding.apply {

            /*Income and food sources navigation*/
            wgIncomeAndFoodSources.apply {

                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(livestockProduction.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    poultryProduction.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(cashCropProduction.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    foodCropProduction.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(casualOrWagedLabour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    formalWagedLabour.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(fishing.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    huntingAndGathering.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(smallBusiness.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    firewoodOrCharcoal.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(pettyTrading.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    remittance.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(bodaboda.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    beeKeeping.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(sandHarvesting.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    other.text.toString()
                                ).toDouble()

                            if (totalEntry > 100) {
                                errorDialog?.isShowing?.let { isDialogShowing ->
                                    if (isDialogShowing) {
                                        return@postDelayed
                                    }
                                }

                                inflateErrorModal("Percentage error", "Entries cannot exceed 100%")

                            }


                        }, 1500)
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

                livestockProduction.addTextChangedListener(textWatcher)
                poultryProduction.addTextChangedListener(textWatcher)
                cashCropProduction.addTextChangedListener(textWatcher)
                foodCropProduction.addTextChangedListener(textWatcher)
                casualOrWagedLabour.addTextChangedListener(textWatcher)
                formalWagedLabour.addTextChangedListener(textWatcher)
                fishing.addTextChangedListener(textWatcher)
                huntingAndGathering.addTextChangedListener(textWatcher)
                smallBusiness.addTextChangedListener(textWatcher)
                firewoodOrCharcoal.addTextChangedListener(textWatcher)
                pettyTrading.addTextChangedListener(textWatcher)
                remittance.addTextChangedListener(textWatcher)
                bodaboda.addTextChangedListener(textWatcher)
                beeKeeping.addTextChangedListener(textWatcher)
                sandHarvesting.addTextChangedListener(textWatcher)
                other.addTextChangedListener(textWatcher)


                foodSourcesNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (livestockProduction.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for livestock production field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (poultryProduction.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        poultryProductionWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for poultry production field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (cashCropProduction.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        cashCropProductionWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for cash crop production field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (foodCropProduction.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        foodCropProductionWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for food crop production field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (casualOrWagedLabour.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        casualOrWagedLabourWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for casual/waged labour field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (formalWagedLabour.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        formalWagedLabourWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for formal/waged labour field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (fishing.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for fishing field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (huntingAndGathering.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        huntingAndGatheringWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for hunting and gathering field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (smallBusiness.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        smallBusinessWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for small business field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (firewoodOrCharcoal.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        firewoodOrCharcoalWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for firewood/charcoal field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (pettyTrading.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pettyTradingWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for petty trading field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (remittance.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        remittanceWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for remittance field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (bodaboda.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        bodabodaWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for bodaboda field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (beeKeeping.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beeKeepingWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for bee keeping field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (sandHarvesting.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sandHarvestingWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for sand harvesting field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    if (other.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        otherWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for other field",
                            Toast.LENGTH_SHORT
                        ).show();
                    }

                    val totalEntry =
                        returnZeroStringIfEmpty(livestockProduction.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            poultryProduction.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(cashCropProduction.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            foodCropProduction.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(casualOrWagedLabour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            formalWagedLabour.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(fishing.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            huntingAndGathering.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(smallBusiness.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            firewoodOrCharcoal.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(pettyTrading.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            remittance.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(bodaboda.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            beeKeeping.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(sandHarvesting.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            other.text.toString()
                        ).toDouble()

                    if (totalEntry < 100) {
                        hasNoValidationError = false
                        val remainingPercentage = 100 - totalEntry
                        inflateErrorModal(
                            "Percentage error",
                            "Entries are less than 100% by $remainingPercentage %"
                        )
                    }

                    if (hasNoValidationError) {

                        val incomeAndFoodSourceResponses = IncomeAndFoodSourceResponses()

                        incomeAndFoodSourceResponses.livestockProduction =
                            livestockProduction.text.toString().toDouble()

                        incomeAndFoodSourceResponses.poultryProduction =
                            poultryProduction.text.toString().toDouble()

                        incomeAndFoodSourceResponses.cashCropProduction =
                            cashCropProduction.text.toString().toDouble()

                        incomeAndFoodSourceResponses.foodCropProduction =
                            foodCropProduction.text.toString().toDouble()

                        incomeAndFoodSourceResponses.casualOrWagedLabour =
                            casualOrWagedLabour.text.toString().toDouble()

                        incomeAndFoodSourceResponses.formalWagedLabour =
                            formalWagedLabour.text.toString().toDouble()

                        incomeAndFoodSourceResponses.fishing =
                            fishing.text.toString().toDouble()

                        incomeAndFoodSourceResponses.huntingAndGathering =
                            huntingAndGathering.text.toString().toDouble()

                        incomeAndFoodSourceResponses.smallBusiness =
                            smallBusiness.text.toString().toDouble()

                        incomeAndFoodSourceResponses.firewoodOrCharcoal =
                            firewoodOrCharcoal.text.toString().toDouble()

                        incomeAndFoodSourceResponses.pettyTrading =
                            pettyTrading.text.toString().toDouble()

                        incomeAndFoodSourceResponses.remittance =
                            remittance.text.toString().toDouble()

                        incomeAndFoodSourceResponses.bodaboda =
                            bodaboda.text.toString().toDouble()

                        incomeAndFoodSourceResponses.beeKeeping =
                            beeKeeping.text.toString().toDouble()

                        incomeAndFoodSourceResponses.sandHarvesting =
                            sandHarvesting.text.toString().toDouble()

                        incomeAndFoodSourceResponses.other =
                            other.text.toString().toDouble()

                        wealthGroupQuestionnaire.incomeAndFoodSourceResponses =
                            incomeAndFoodSourceResponses

                        wgIncomeAndFoodSources.root.visibility = View.GONE
                        wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE

                    }
                }
            }

            /*Income and food sources percentages navigation
            * todo: Change this navigation to crop production question
            * */

            wgPercentFoodConsumptionIncome.apply {

                var hasNoPercentageError: Boolean = true

                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            if (editable == maizeOwnFarm.editableText ||
                                editable == maizeMarket.editableText ||
                                editable == maizeGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(maizeOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        maizeMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(maizeGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    maizeOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    maizeMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    maizeGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    maizeOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    maizeMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    maizeGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }


                            if (editable == wheatOwnFarm.editableText ||
                                editable == wheatMarket.editableText ||
                                editable == wheatGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(wheatOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        wheatMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(wheatGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    wheatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    wheatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    wheatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    wheatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    wheatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    wheatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == sorghumOwnFarm.editableText ||
                                editable == sorghumMarket.editableText ||
                                editable == sorghumGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(sorghumOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        sorghumMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(sorghumGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    sorghumOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    sorghumMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    sorghumGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    sorghumOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    sorghumMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    sorghumGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }


                            if (editable == riceOwnFarm.editableText ||
                                editable == riceMarket.editableText ||
                                editable == riceGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(riceOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        riceMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(riceGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    riceOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    riceMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    riceGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    riceOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    riceMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    riceGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == beansOwnfarm.editableText ||
                                editable == beansMarket.editableText ||
                                editable == beansGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(beansOwnfarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        beansMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(beansGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    beansOwnfarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    beansMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    beansGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    beansOwnfarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    beansMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    beansGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == pulsesOwnFarm.editableText ||
                                editable == pulsesMarket.editableText ||
                                editable == pulsesGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(pulsesOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        pulsesMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(pulsesGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    pulsesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    pulsesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    pulsesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    pulsesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    pulsesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    pulsesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == vegetablesOwnFarm.editableText ||
                                editable == vegetablesMarket.editableText ||
                                editable == vegetablesGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(vegetablesOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        vegetablesMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(vegetablesGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    vegetablesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    vegetablesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    vegetablesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    vegetablesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    vegetablesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    vegetablesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == fruitsOwnFarm.editableText ||
                                editable == fruitsMarket.editableText ||
                                editable == fruitsGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(fruitsOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        fruitsMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(fruitsGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    fruitsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    fruitsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    fruitsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    fruitsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fruitsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fruitsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == whiteRootsOwnFarm.editableText ||
                                editable == whiteRootsMarket.editableText ||
                                editable == whiteRootsGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(whiteRootsOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        whiteRootsMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(whiteRootsGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    whiteRootsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    whiteRootsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    whiteRootsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    whiteRootsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    whiteRootsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    whiteRootsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == meatOwnFarm.editableText ||
                                editable == meatMarket.editableText ||
                                editable == meatGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(meatOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        meatMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(meatGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    meatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    meatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    meatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    meatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    meatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    meatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == milkOwnFarm.editableText ||
                                editable == milkMarket.editableText ||
                                editable == milkGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(milkOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        milkMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(milkGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    milkOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    milkMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    milkGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    milkOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    milkMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    milkGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == fishOwnFarm.editableText ||
                                editable == fishOwnMarket.editableText ||
                                editable == fishGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(fishOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        fishOwnMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(fishGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    fishOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    fishOwnMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    fishGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    fishOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fishOwnMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fishGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }




                            if (editable == eggsOwnFarm.editableText ||
                                editable == eggsMarket.editableText ||
                                editable == eggsGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(eggsOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        eggsMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(eggsGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    eggsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    eggsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    eggsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    eggsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    eggsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    eggsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }




                            if (editable == cookingFatOwnFarm.editableText ||
                                editable == cookingFatMarket.editableText ||
                                editable == cookingFatGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(cookingFatOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        cookingFatMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(cookingFatGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    cookingFatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    cookingFatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    cookingFatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    cookingFatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    cookingFatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    cookingFatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }



                            if (editable == spicesOwnFarm.editableText ||
                                editable == spicesMarket.editableText ||
                                editable == spicesGift.editableText
                            ) {
                                val totalPercentage =
                                    returnZeroStringIfEmpty(spicesOwnFarm.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                        spicesMarket.text.toString()
                                    ).toDouble() + returnZeroStringIfEmpty(spicesGift.text.toString()).toDouble()
                                if (totalPercentage != 100.0) {
                                    spicesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    spicesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    spicesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.error_cell, null)
                                    hasNoPercentageError = false
                                } else {
                                    spicesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    spicesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    spicesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    hasNoPercentageError = true
                                }
                            }

                        }, 1500)
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

                maizeOwnFarm.addTextChangedListener(textWatcher)
                maizeMarket.addTextChangedListener(textWatcher)
                maizeGift.addTextChangedListener(textWatcher)

                wheatOwnFarm.addTextChangedListener(textWatcher)
                wheatMarket.addTextChangedListener(textWatcher)
                wheatGift.addTextChangedListener(textWatcher)

                sorghumOwnFarm.addTextChangedListener(textWatcher)
                sorghumMarket.addTextChangedListener(textWatcher)
                sorghumGift.addTextChangedListener(textWatcher)

                riceOwnFarm.addTextChangedListener(textWatcher)
                riceMarket.addTextChangedListener(textWatcher)
                riceGift.addTextChangedListener(textWatcher)

                beansOwnfarm.addTextChangedListener(textWatcher)
                beansMarket.addTextChangedListener(textWatcher)
                beansGift.addTextChangedListener(textWatcher)

                pulsesOwnFarm.addTextChangedListener(textWatcher)
                pulsesMarket.addTextChangedListener(textWatcher)
                pulsesGift.addTextChangedListener(textWatcher)

                vegetablesOwnFarm.addTextChangedListener(textWatcher)
                vegetablesMarket.addTextChangedListener(textWatcher)
                vegetablesGift.addTextChangedListener(textWatcher)

                fruitsOwnFarm.addTextChangedListener(textWatcher)
                fruitsMarket.addTextChangedListener(textWatcher)
                fruitsGift.addTextChangedListener(textWatcher)

                whiteRootsOwnFarm.addTextChangedListener(textWatcher)
                whiteRootsMarket.addTextChangedListener(textWatcher)
                whiteRootsGift.addTextChangedListener(textWatcher)

                meatOwnFarm.addTextChangedListener(textWatcher)
                meatMarket.addTextChangedListener(textWatcher)
                meatGift.addTextChangedListener(textWatcher)

                milkOwnFarm.addTextChangedListener(textWatcher)
                milkMarket.addTextChangedListener(textWatcher)
                milkGift.addTextChangedListener(textWatcher)

                fishOwnFarm.addTextChangedListener(textWatcher)
                fishOwnMarket.addTextChangedListener(textWatcher)
                fishGift.addTextChangedListener(textWatcher)

                eggsOwnFarm.addTextChangedListener(textWatcher)
                eggsMarket.addTextChangedListener(textWatcher)
                eggsGift.addTextChangedListener(textWatcher)

                cookingFatOwnFarm.addTextChangedListener(textWatcher)
                cookingFatMarket.addTextChangedListener(textWatcher)
                cookingFatGift.addTextChangedListener(textWatcher)

                spicesOwnFarm.addTextChangedListener(textWatcher)
                spicesMarket.addTextChangedListener(textWatcher)
                spicesGift.addTextChangedListener(textWatcher)



                foodSourcesPercentNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (maizeOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        maizeOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (maizeMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        maizeMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (maizeGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        maizeGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wheatOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        wheatOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wheatMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        wheatMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wheatGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        wheatGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (sorghumOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sorghumOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (sorghumMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sorghumMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (sorghumGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sorghumMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (riceOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        riceOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (riceMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        riceMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (riceGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        riceGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (beansOwnfarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beansOwnfarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (beansMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beansMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (beansGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beansGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pulsesOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pulsesOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pulsesMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pulsesMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pulsesGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pulsesGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (vegetablesOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        vegetablesOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (vegetablesMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        vegetablesMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (vegetablesGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        vegetablesGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fruitsOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fruitsOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fruitsMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fruitsMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fruitsGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fruitsGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (whiteRootsOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        whiteRootsOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (whiteRootsMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        whiteRootsMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (whiteRootsGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        whiteRootsGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (meatOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        meatOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (meatMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        meatMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (meatGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        meatGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (milkOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        milkOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (milkMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        milkMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (milkGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        milkGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishOwnMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishOwnMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (eggsOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        eggsOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (eggsMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        eggsMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (eggsGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        eggsGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cookingFatOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        cookingFatOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cookingFatMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        cookingFatMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cookingFatGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        cookingFatGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (spicesOwnFarm.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        spicesOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (spicesMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        spicesMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (spicesGift.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        spicesGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (hasNoValidationError && hasNoPercentageError) {

                        val foodConsumptionResponses = FoodConsumptionResponses()

                        foodConsumptionResponses.maizeAndPosho = FoodConsumptionResponseItem(
                            maizeOwnFarm.text.toString().toDouble(),
                            maizeMarket.text.toString().toDouble(),
                            maizeGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.wheatOrBarley = FoodConsumptionResponseItem(
                            wheatOwnFarm.text.toString().toDouble(),
                            wheatMarket.text.toString().toDouble(),
                            wheatGift.text.toString().toDouble()
                        )


                        foodConsumptionResponses.sorghumOrMillet = FoodConsumptionResponseItem(
                            sorghumOwnFarm.text.toString().toDouble(),
                            sorghumMarket.text.toString().toDouble(),
                            sorghumGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.rice = FoodConsumptionResponseItem(
                            riceOwnFarm.text.toString().toDouble(),
                            riceMarket.text.toString().toDouble(),
                            riceGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.beans = FoodConsumptionResponseItem(
                            beansOwnfarm.text.toString().toDouble(),
                            beansMarket.text.toString().toDouble(),
                            beansGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.pulses = FoodConsumptionResponseItem(
                            pulsesOwnFarm.text.toString().toDouble(),
                            pulsesMarket.text.toString().toDouble(),
                            pulsesGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.vegetables = FoodConsumptionResponseItem(
                            vegetablesOwnFarm.text.toString().toDouble(),
                            vegetablesMarket.text.toString().toDouble(),
                            vegetablesGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.fruits = FoodConsumptionResponseItem(
                            fruitsOwnFarm.text.toString().toDouble(),
                            fruitsMarket.text.toString().toDouble(),
                            fruitsGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.whiteRoots = FoodConsumptionResponseItem(
                            whiteRootsOwnFarm.text.toString().toDouble(),
                            whiteRootsMarket.text.toString().toDouble(),
                            whiteRootsGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.meat = FoodConsumptionResponseItem(
                            meatOwnFarm.text.toString().toDouble(),
                            meatMarket.text.toString().toDouble(),
                            meatGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.milk = FoodConsumptionResponseItem(
                            milkOwnFarm.text.toString().toDouble(),
                            milkMarket.text.toString().toDouble(),
                            milkGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.fish = FoodConsumptionResponseItem(
                            fishOwnFarm.text.toString().toDouble(),
                            fishOwnMarket.text.toString().toDouble(),
                            fishGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.eggs = FoodConsumptionResponseItem(
                            eggsOwnFarm.text.toString().toDouble(),
                            eggsMarket.text.toString().toDouble(),
                            eggsGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.cookingFats = FoodConsumptionResponseItem(
                            cookingFatOwnFarm.text.toString().toDouble(),
                            cookingFatMarket.text.toString().toDouble(),
                            cookingFatGift.text.toString().toDouble()
                        )

                        foodConsumptionResponses.spices = FoodConsumptionResponseItem(
                            spicesOwnFarm.text.toString().toDouble(),
                            spicesMarket.text.toString().toDouble(),
                            spicesGift.text.toString().toDouble()
                        )

                        wealthGroupQuestionnaire.foodConsumptionResponses = foodConsumptionResponses

                        cropSelectionLayout.apply {
                            activity?.let { context ->
                                val adapter =
                                    CropSelectionListAdapter(
                                        context,
                                        R.layout.lz_selection_item,
                                        crops,
                                        this@WealthGroupDialogFragment
                                    )
                                cropsList.adapter = adapter
                            }
                        }

                        wgPercentFoodConsumptionIncome.root.visibility = View.GONE
                        cropSelectionLayout.root.visibility = View.VISIBLE

                    }
                }

                foodSourcesPercentBackButton.setOnClickListener {
                    wgPercentFoodConsumptionIncome.root.visibility = View.GONE
                    wgIncomeAndFoodSources.root.visibility = View.VISIBLE
                }
            }


            /* Crop Selection navigation */

            cropSelectionLayout.apply {

                cropSelectionBackButton.setOnClickListener {
                    wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE
                    cropSelectionLayout.root.visibility = View.GONE
                }

                cropSelectionNextButton.setOnClickListener {


                    for (currentCrop in wealthGroupQuestionnaire.selectedCrops) {
                        cropProductionResponseItems.add(
                            WgCropProductionResponseItem(
                                currentCrop,
                                CropProductionResponseValueModel(0.0, false),
                                CropProductionResponseValueModel(0.0, false),
                                CropProductionResponseValueModel(0.0, false),
                                CropProductionResponseValueModel(0.0, false)
                            )
                        )
                    }

                    cropProductionLayout.apply {
                        activity?.let { context ->
                            val adapter =
                                CropProductionListAdapter(
                                    context,
                                    R.layout.lz_crop_production_item,
                                    cropProductionResponseItems,
                                    this@WealthGroupDialogFragment
                                )
                            cropsList.adapter = adapter
                        }
                    }

                    cropProductionLayout.root.visibility = View.VISIBLE
                    cropSelectionLayout.root.visibility = View.GONE
                }

            }


            /* Crop production layout */

            cropProductionLayout.apply {

                cropProductionBackButton.setOnClickListener {
                    cropProductionLayout.root.visibility = View.GONE
                    cropSelectionLayout.root.visibility = View.VISIBLE
                }

                cropProductionNextButton.setOnClickListener {
                    if (!isAnyCropProductionFieldEmpty()) {
                        cropProductionLayout.root.visibility = View.GONE
                        wgLivestockPoultryNumbers.root.visibility = View.VISIBLE
                    } else {
                        activity?.let { context ->
                            val adapter =
                                CropProductionListAdapter(
                                    context,
                                    R.layout.lz_crop_production_item,
                                    cropProductionResponseItems,
                                    this@WealthGroupDialogFragment
                                )
                            cropsList.adapter = adapter
                        }
                        inflateErrorModal("Missing Data", "Kindly fill out all the fields")
                    }
                }

            }


            /*Livestock and poultry navigation*/
            wgLivestockPoultryNumbers.apply {
                livestockPoultryNumbertsBackButton.setOnClickListener {
                    cropProductionLayout.root.visibility = View.VISIBLE
                    wgLivestockPoultryNumbers.root.visibility = View.GONE
                }

                livestockPoultryNumbertsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (cattleNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        cattleNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (goatNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        goatNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (sheepNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sheepNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (donkeyNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        donkeyNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (camelNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        camelNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pigNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pigNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (chickenNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        chickenNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (duckNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        duckNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (duckNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        duckNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (beeHiveNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beeHiveNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishPondNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishPondNumbersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (hasNoValidationError) {

                        val livestockPoultryOwnershipResponses =
                            LivestockPoultryOwnershipResponses()
                        livestockPoultryOwnershipResponses.cattle =
                            cattleNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.goats =
                            goatNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.sheep =
                            sheepNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.donkeys =
                            donkeyNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.camels =
                            camelNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.pigs =
                            pigNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.chicken =
                            chickenNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.ducks =
                            duckNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.beeHives =
                            beeHiveNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.fishPonds =
                            fishPondNumbers.text.toString().toDouble()

                        wealthGroupQuestionnaire.livestockPoultryOwnershipResponses =
                            livestockPoultryOwnershipResponses
                        wgLivestockPoultryContribution.root.visibility = View.VISIBLE
                        wgLivestockPoultryNumbers.root.visibility = View.GONE

                    }
                }
            }


            /*Livestock and poultry contribution navigation*/
            wgLivestockPoultryContribution.apply {
                livestockPoultryContributionBackButton.setOnClickListener {
                    wgLivestockPoultryNumbers.root.visibility = View.VISIBLE
                    wgLivestockPoultryContribution.root.visibility = View.GONE
                }

                livestockPoultryContributionNextButton.setOnClickListener {

                    val livestockContributionResponses = LivestockContributionResponses()

                    livestockContributionResponses.cattle = LivestockContributionResponseItem(
                        cattleIncomeRank.text.toString().toInt(),
                        cattleCashPercentage.text.toString().toDouble(),
                        cattleFoodRank.text.toString().toInt(),
                        cattleFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.goats = LivestockContributionResponseItem(
                        goatsIncomeRank.text.toString().toInt(),
                        goatsIncomePercentage.text.toString().toDouble(),
                        goatsCashRank.text.toString().toInt(),
                        goatsCashPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.sheep = LivestockContributionResponseItem(
                        sheepCashRank.text.toString().toInt(),
                        sheepCashPercentage.text.toString().toDouble(),
                        sheepFoodRank.text.toString().toInt(),
                        sheepFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.donkeys = LivestockContributionResponseItem(
                        donkeysCahRank.text.toString().toInt(),
                        donkeysCashPercentage.text.toString().toDouble(),
                        donkeysFoodRank.text.toString().toInt(),
                        donkeysFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.pigs = LivestockContributionResponseItem(
                        pigscashRank.text.toString().toInt(),
                        pigsCashPercentage.text.toString().toDouble(),
                        pigsFoodrank.text.toString().toInt(),
                        pigsFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.chicken = LivestockContributionResponseItem(
                        chickenCashRank.text.toString().toInt(),
                        chickenCashPaercentage.text.toString().toDouble(),
                        chickenFooRank.text.toString().toInt(),
                        chickenFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.camels = LivestockContributionResponseItem(
                        camelsCashRank.text.toString().toInt(),
                        camelsCashPercentage.text.toString().toDouble(),
                        camelsFoodRank.text.toString().toInt(),
                        camelsFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.ducks = LivestockContributionResponseItem(
                        ducksCashRank.text.toString().toInt(),
                        duckscashPercentage.text.toString().toDouble(),
                        ducksFoodRank.text.toString().toInt(),
                        ducksFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.beeHives = LivestockContributionResponseItem(
                        beeHivesCashRank.text.toString().toInt(),
                        beeHivesCashPercentage.text.toString().toDouble(),
                        beeHivesFoodrank.text.toString().toInt(),
                        beeHivesFoodPercentage.text.toString().toDouble()
                    )

                    livestockContributionResponses.fishPonds = LivestockContributionResponseItem(
                        fishPondsCashRank.text.toString().toInt(),
                        fishPondscashPercentage.text.toString().toDouble(),
                        fishPondsFoodRank.text.toString().toInt(),
                        fishPondsFoodPercentage.text.toString().toDouble()
                    )

                    wealthGroupQuestionnaire.livestockContributionResponses =
                        livestockContributionResponses

                    wgLabourPatterns.root.visibility = View.VISIBLE
                    wgLivestockPoultryContribution.root.visibility = View.GONE
                }
            }

            /*Labour patterns navigation */
            wgLabourPatterns.apply {

                val mentTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({


                            val menTotalEntry =
                                returnZeroStringIfEmpty(ownFarmmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    livestockHusbandrymen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(wagedLabourmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    lowSkilledNonFarmmen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(skilledLabourmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    formalEmploymentmen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(huntingAndGatheringmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    fishingmen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(tradingmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    domesticUnpaidWorkmen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(leisuremen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    sexWorkmen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(beggingmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    inactivitymen.text.toString()
                                ).toDouble()

                            if (menTotalEntry > 100) {
                                val excessValue = menTotalEntry - 100.0
                                errorDialog?.isShowing?.let { isDialogShowing ->
                                    if (isDialogShowing) {
                                        return@postDelayed
                                    }
                                }

                                inflateErrorModal(
                                    "Percentage error",
                                    "Entries exceed 100% by $excessValue"
                                )

                            }


                        }, 1500)
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

                ownFarmmen.addTextChangedListener(mentTextWatcher)
                livestockHusbandrymen.addTextChangedListener(mentTextWatcher)
                wagedLabourmen.addTextChangedListener(mentTextWatcher)
                lowSkilledNonFarmmen.addTextChangedListener(mentTextWatcher)
                skilledLabourmen.addTextChangedListener(mentTextWatcher)
                formalEmploymentmen.addTextChangedListener(mentTextWatcher)
                huntingAndGatheringmen.addTextChangedListener(mentTextWatcher)
                fishingmen.addTextChangedListener(mentTextWatcher)
                tradingmen.addTextChangedListener(mentTextWatcher)
                domesticUnpaidWorkmen.addTextChangedListener(mentTextWatcher)
                leisuremen.addTextChangedListener(mentTextWatcher)
                sexWorkmen.addTextChangedListener(mentTextWatcher)
                beggingmen.addTextChangedListener(mentTextWatcher)
                inactivitymen.addTextChangedListener(mentTextWatcher)


                val womentTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({


                            val womenTotalEntry =
                                returnZeroStringIfEmpty(ownFarmWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    livestockHusbandryWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(wagedLabourWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    lowSkilledNonFarmWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(skilledLabourWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    formalEmploymentWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(huntingAndGatheringWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    fishingWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(tradingWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    domesticUnpaidWorkWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(leisureWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    sexWorkWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(beggingWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    inactivityWomen.text.toString()
                                ).toDouble()

                            if (womenTotalEntry > 100) {
                                val excessValue = womenTotalEntry - 100.0
                                errorDialog?.isShowing?.let { isDialogShowing ->
                                    if (isDialogShowing) {
                                        return@postDelayed
                                    }
                                }

                                inflateErrorModal(
                                    "Percentage error",
                                    "Entries exceed 100% by $excessValue"
                                )

                            }


                        }, 1500)
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

                ownFarmWomen.addTextChangedListener(womentTextWatcher)
                livestockHusbandryWomen.addTextChangedListener(womentTextWatcher)
                wagedLabourWomen.addTextChangedListener(womentTextWatcher)
                lowSkilledNonFarmWomen.addTextChangedListener(womentTextWatcher)
                skilledLabourWomen.addTextChangedListener(womentTextWatcher)
                formalEmploymentWomen.addTextChangedListener(womentTextWatcher)
                huntingAndGatheringWomen.addTextChangedListener(womentTextWatcher)
                fishingWomen.addTextChangedListener(womentTextWatcher)
                tradingWomen.addTextChangedListener(womentTextWatcher)
                domesticUnpaidWorkWomen.addTextChangedListener(womentTextWatcher)
                leisureWomen.addTextChangedListener(womentTextWatcher)
                sexWorkWomen.addTextChangedListener(womentTextWatcher)
                beggingWomen.addTextChangedListener(womentTextWatcher)
                inactivityWomen.addTextChangedListener(womentTextWatcher)

                labourPatternsBackButton.setOnClickListener {
                    wgLivestockPoultryContribution.root.visibility = View.VISIBLE
                    wgLabourPatterns.root.visibility = View.GONE
                }

                labourPatternsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    val menTotalEntry =
                        returnZeroStringIfEmpty(ownFarmmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            livestockHusbandrymen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(wagedLabourmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            lowSkilledNonFarmmen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(skilledLabourmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            formalEmploymentmen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(huntingAndGatheringmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            fishingmen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(tradingmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            domesticUnpaidWorkmen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(leisuremen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            sexWorkmen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(beggingmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            inactivitymen.text.toString()
                        ).toDouble()

                    if (menTotalEntry < 100) {
                        val deficitValue = 100 - menTotalEntry
                        inflateErrorModal(
                            "Percentage error",
                            "Entries for men are less than 100% by $deficitValue"
                        )

                    }


                    val womenTotalEntry =
                        returnZeroStringIfEmpty(ownFarmWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            livestockHusbandryWomen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(wagedLabourWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            lowSkilledNonFarmWomen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(skilledLabourWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            formalEmploymentWomen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(huntingAndGatheringWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            fishingWomen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(tradingWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            domesticUnpaidWorkWomen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(leisureWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            sexWorkWomen.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(beggingWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            inactivityWomen.text.toString()
                        ).toDouble()


                    if (womenTotalEntry < 100) {
                        val womenDeficitValue = 100 - womenTotalEntry
                        inflateErrorModal(
                            "Percentage error",
                            "Entries for women are less than 100% by $womenDeficitValue"
                        )

                    }

                    if (ownFarmWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        ownFarmWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (ownFarmmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        ownFarmmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockHusbandryWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockHusbandryWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockHusbandrymen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockHusbandrymenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        wagedLabourWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        wagedLabourmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (lowSkilledNonFarmWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        lowSkilledNonFarmWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (lowSkilledNonFarmmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        lowSkilledNonFarmmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (skilledLabourWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        skilledLabourWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (skilledLabourmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        skilledLabourmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (formalEmploymentWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        formalEmploymentWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (formalEmploymentmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        formalEmploymentmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (huntingAndGatheringWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        huntingAndGatheringWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (huntingAndGatheringmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        huntingAndGatheringmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (tradingWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        tradingWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (tradingmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        tradingmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (domesticUnpaidWorkWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        domesticUnpaidWorkWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (domesticUnpaidWorkmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        domesticUnpaidWorkmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (leisureWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        leisureWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (leisuremen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        leisuremenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (sexWorkWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sexWorkWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (sexWorkmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        sexWorkmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (beggingWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beggingWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (beggingmen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        beggingmenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (inactivityWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        inactivityWomenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (inactivitymen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        inactivitymenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }


                    if (hasNoValidationError) {

                        val labourPatternResponse = LabourPatternResponse()
                        labourPatternResponse.ownFarmCropProduction = LabourPatternResponseItem(
                            ownFarmWomen.text.toString().toDouble(),
                            ownFarmmen.text.toString().toDouble()
                        )

                        labourPatternResponse.livestockHusbandry = LabourPatternResponseItem(
                            livestockHusbandryWomen.text.toString().toDouble(),
                            livestockHusbandrymen.text.toString().toDouble()
                        )

                        labourPatternResponse.wagedLabourOnFarms = LabourPatternResponseItem(
                            wagedLabourWomen.text.toString().toDouble(),
                            wagedLabourmen.text.toString().toDouble()
                        )

                        labourPatternResponse.lowSkilledNonFarmLabour = LabourPatternResponseItem(
                            lowSkilledNonFarmWomen.text.toString().toDouble(),
                            lowSkilledNonFarmmen.text.toString().toDouble()
                        )

                        labourPatternResponse.skilledLabour = LabourPatternResponseItem(
                            skilledLabourWomen.text.toString().toDouble(),
                            skilledLabourmen.text.toString().toDouble()
                        )

                        labourPatternResponse.formalEmployment = LabourPatternResponseItem(
                            formalEmploymentWomen.text.toString().toDouble(),
                            formalEmploymentmen.text.toString().toDouble()
                        )

                        labourPatternResponse.huntingAndGathering = LabourPatternResponseItem(
                            huntingAndGatheringWomen.text.toString().toDouble(),
                            huntingAndGatheringmen.text.toString().toDouble()
                        )

                        labourPatternResponse.fishing = LabourPatternResponseItem(
                            fishingWomen.text.toString().toDouble(),
                            fishingmen.text.toString().toDouble()
                        )

                        labourPatternResponse.trading = LabourPatternResponseItem(
                            tradingWomen.text.toString().toDouble(),
                            tradingmen.text.toString().toDouble()
                        )

                        labourPatternResponse.domesticUnpaidWork = LabourPatternResponseItem(
                            domesticUnpaidWorkWomen.text.toString().toDouble(),
                            domesticUnpaidWorkmen.text.toString().toDouble()
                        )

                        labourPatternResponse.leisure = LabourPatternResponseItem(
                            leisureWomen.text.toString().toDouble(),
                            leisuremen.text.toString().toDouble()
                        )

                        labourPatternResponse.commercialSexWork = LabourPatternResponseItem(
                            sexWorkWomen.text.toString().toDouble(),
                            sexWorkmen.text.toString().toDouble()
                        )

                        labourPatternResponse.begging = LabourPatternResponseItem(
                            beggingWomen.text.toString().toDouble(),
                            beggingmen.text.toString().toDouble()
                        )

                        labourPatternResponse.inactivity = LabourPatternResponseItem(
                            inactivityWomen.text.toString().toDouble(),
                            inactivitymen.text.toString().toDouble()
                        )

                        wealthGroupQuestionnaire.labourPatternResponses = labourPatternResponse

                        wgExpenditurePatterns.root.visibility = View.VISIBLE
                        wgLabourPatterns.root.visibility = View.GONE

                    }
                }
            }


            /*Expenditure patterns navigation */
            wgExpenditurePatterns.apply {


                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(maizeAndMaizeFlour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    otherCereals.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(pulses.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    rootsAndTubers.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(vegetablesAndFruits.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    fishandseaFood.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(meat.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    milk.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(eggs.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    oilAndFats.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(otherFoods.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    schoolFees.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(drugsAndMedicalCare.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    clothingAndBeautyProducts.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(houseRent.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    communicationExpense.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(farmInputs.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    livestockDrugs.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(waterPurchase.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    soaps.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(farrmLabour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    travelRelatedExpense.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(entertainment.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    electricityBill.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(socialObligation.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    millingCost.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(cookingFuel.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    savingsAndInvestment.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(loanRepayments.text.toString()).toDouble()

                            if (totalEntry > 100) {
                                val excessValue = totalEntry - 100.0
                                errorDialog?.isShowing?.let { isDialogShowing ->
                                    if (isDialogShowing) {
                                        return@postDelayed
                                    }
                                }

                                inflateErrorModal(
                                    "Percentage error",
                                    "Entries exceed 100% by $excessValue"
                                )

                            }


                        }, 1500)
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

                maizeAndMaizeFlour.addTextChangedListener(textWatcher)
                otherCereals.addTextChangedListener(textWatcher)
                pulses.addTextChangedListener(textWatcher)
                rootsAndTubers.addTextChangedListener(textWatcher)
                vegetablesAndFruits.addTextChangedListener(textWatcher)
                fishandseaFood.addTextChangedListener(textWatcher)
                meat.addTextChangedListener(textWatcher)
                milk.addTextChangedListener(textWatcher)
                eggs.addTextChangedListener(textWatcher)
                oilAndFats.addTextChangedListener(textWatcher)
                otherFoods.addTextChangedListener(textWatcher)
                schoolFees.addTextChangedListener(textWatcher)
                drugsAndMedicalCare.addTextChangedListener(textWatcher)
                clothingAndBeautyProducts.addTextChangedListener(textWatcher)
                houseRent.addTextChangedListener(textWatcher)
                communicationExpense.addTextChangedListener(textWatcher)
                farmInputs.addTextChangedListener(textWatcher)
                livestockDrugs.addTextChangedListener(textWatcher)
                waterPurchase.addTextChangedListener(textWatcher)
                soaps.addTextChangedListener(textWatcher)
                farrmLabour.addTextChangedListener(textWatcher)
                travelRelatedExpense.addTextChangedListener(textWatcher)
                entertainment.addTextChangedListener(textWatcher)
                electricityBill.addTextChangedListener(textWatcher)
                socialObligation.addTextChangedListener(textWatcher)
                millingCost.addTextChangedListener(textWatcher)
                cookingFuel.addTextChangedListener(textWatcher)
                savingsAndInvestment.addTextChangedListener(textWatcher)
                loanRepayments.addTextChangedListener(textWatcher)


                expenditurePatternsBackButton.setOnClickListener {
                    wgLabourPatterns.root.visibility = View.VISIBLE
                    wgExpenditurePatterns.root.visibility = View.GONE
                }

                expenditurePatternsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (maizeAndMaizeFlour.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        maizeAndMaizeFlourCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (otherCereals.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        otherCerealsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pulses.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pulsesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (rootsAndTubers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        rootsAndTubersCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (vegetablesAndFruits.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        vegetablesAndFruitsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishandseaFood.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishandseaFoodCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (meat.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        meatCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (milk.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        milkCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (eggs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        eggsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (oilAndFats.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        oilAndFatsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (otherFoods.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        otherFoodsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (schoolFees.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        schoolFeesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (drugsAndMedicalCare.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        drugsAndMedicalCareCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (clothingAndBeautyProducts.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        clothingAndBeautyProductsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (houseRent.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        houseRentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (communicationExpense.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        communicationExpenseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (farmInputs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        farmInputsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockDrugs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockDrugsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (waterPurchase.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        waterPurchaseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (soaps.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        soapsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (farrmLabour.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        farrmLabourCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (travelRelatedExpense.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        travelRelatedExpenseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (entertainment.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        entertainmentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (electricityBill.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        electricityBillCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (socialObligation.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        socialObligationCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (millingCost.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        millingCostCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cookingFuel.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        cookingFuelcell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (savingsAndInvestment.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        savingsAndInvestmentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (loanRepayments.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        loanRepaymentsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    val totalPercentageEntry =
                        returnZeroStringIfEmpty(maizeAndMaizeFlour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            otherCereals.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(pulses.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            rootsAndTubers.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(vegetablesAndFruits.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            fishandseaFood.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(meat.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            milk.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(eggs.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            oilAndFats.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(otherFoods.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            schoolFees.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(drugsAndMedicalCare.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            clothingAndBeautyProducts.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(houseRent.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            communicationExpense.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(farmInputs.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            livestockDrugs.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(waterPurchase.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            soaps.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(farrmLabour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            travelRelatedExpense.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(entertainment.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            electricityBill.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(socialObligation.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            millingCost.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(cookingFuel.text.toString()).toDouble() + returnZeroStringIfEmpty(
                            savingsAndInvestment.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(loanRepayments.text.toString()).toDouble()

                    if (totalPercentageEntry < 100) {

                        val deficitValue = 100 - totalPercentageEntry
                        inflateErrorModal(
                            "Percentage error",
                            "Entries are less than 100% by $deficitValue"
                        )

                    }

                    if (hasNoValidationError) {

                        val expenditurePatternsResponses = ExpenditurePatternsResponses()

                        expenditurePatternsResponses.maizeAndMaizeFlour =
                            maizeAndMaizeFlour.text.toString().toDouble()
                        expenditurePatternsResponses.otherCereals =
                            otherCereals.text.toString().toDouble()
                        expenditurePatternsResponses.pulses = pulses.text.toString().toDouble()
                        expenditurePatternsResponses.rootsAndTubers =
                            rootsAndTubers.text.toString().toDouble()
                        expenditurePatternsResponses.vegetablesAndFruits =
                            vegetablesAndFruits.text.toString().toDouble()
                        expenditurePatternsResponses.fishandSeaFood =
                            fishandseaFood.text.toString().toDouble()
                        expenditurePatternsResponses.meat = meat.text.toString().toDouble()
                        expenditurePatternsResponses.milk = milk.text.toString().toDouble()
                        expenditurePatternsResponses.eggs = eggs.text.toString().toDouble()
                        expenditurePatternsResponses.oilsAndFats =
                            oilAndFats.text.toString().toDouble()
                        expenditurePatternsResponses.otherFoods =
                            otherFoods.text.toString().toDouble()
                        expenditurePatternsResponses.schoolFees =
                            schoolFees.text.toString().toDouble()
                        expenditurePatternsResponses.drugsAndMedicalCare =
                            drugsAndMedicalCare.text.toString().toDouble()
                        expenditurePatternsResponses.clothingAndBeautyProducts =
                            clothingAndBeautyProducts.text.toString().toDouble()
                        expenditurePatternsResponses.houseRent =
                            houseRent.text.toString().toDouble()
                        expenditurePatternsResponses.communicationExpenses =
                            communicationExpense.text.toString().toDouble()
                        expenditurePatternsResponses.farmInputs =
                            farmInputs.text.toString().toDouble()
                        expenditurePatternsResponses.livestockDrugs =
                            livestockDrugs.text.toString().toDouble()
                        expenditurePatternsResponses.waterPurchase =
                            waterPurchase.text.toString().toDouble()
                        expenditurePatternsResponses.soaps = soaps.text.toString().toDouble()
                        expenditurePatternsResponses.farmLabour =
                            farrmLabour.text.toString().toDouble()
                        expenditurePatternsResponses.travelRelatedExpenses =
                            travelRelatedExpense.text.toString().toDouble()
                        expenditurePatternsResponses.leisureAndEntertainment =
                            entertainment.text.toString().toDouble()
                        expenditurePatternsResponses.electricityBills =
                            electricityBill.text.toString().toDouble()
                        expenditurePatternsResponses.socialObligation =
                            socialObligation.text.toString().toDouble()
                        expenditurePatternsResponses.millingCosts =
                            millingCost.text.toString().toDouble()
                        expenditurePatternsResponses.cookingFuel =
                            cookingFuel.text.toString().toDouble()
                        expenditurePatternsResponses.savingsAndInvestments =
                            savingsAndInvestment.text.toString().toDouble()
                        expenditurePatternsResponses.loanRepayments =
                            loanRepayments.text.toString().toDouble()

                        wealthGroupQuestionnaire.expenditurePatternsResponses =
                            expenditurePatternsResponses

                        wgMigrationPatterns.root.visibility = View.VISIBLE
                        wgExpenditurePatterns.root.visibility = View.GONE

                    }
                }
            }


            /*Migration patterns */
            wgMigrationPatterns.apply {
                migrationPatternsBackButton.setOnClickListener {
                    wgExpenditurePatterns.root.visibility = View.VISIBLE
                    wgMigrationPatterns.root.visibility = View.GONE
                }


                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(fullyNomadic.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    semiNomadic.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(occasionalNomadic.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    outMigrantLabour.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(inMigrantLabour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    fullySettled.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(internallyDisplaced.text.toString()).toDouble()

                            if (totalEntry > 100) {
                                val excessValue = totalEntry - 100.0
                                errorDialog?.isShowing?.let { isDialogShowing ->
                                    if (isDialogShowing) {
                                        return@postDelayed
                                    }
                                }

                                inflateErrorModal(
                                    "Percentage error",
                                    "Entries exceed 100% by $excessValue"
                                )

                            }


                        }, 1500)
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

                fullyNomadic.addTextChangedListener(textWatcher)
                semiNomadic.addTextChangedListener(textWatcher)
                occasionalNomadic.addTextChangedListener(textWatcher)
                outMigrantLabour.addTextChangedListener(textWatcher)
                inMigrantLabour.addTextChangedListener(textWatcher)
                fullySettled.addTextChangedListener(textWatcher)
                internallyDisplaced.addTextChangedListener(textWatcher)

                migrationPatternsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (fullyNomadic.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fullyNomadicCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (semiNomadic.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        semiNomadicCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (occasionalNomadic.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        occasionalNomadicCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (outMigrantLabour.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        outMigrantLabourCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (inMigrantLabour.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        inMigrantLabourCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fullySettled.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fullySettledCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (internallyDisplaced.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        internallyDisplacedCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (hasNoValidationError) {

                        val migrationPatternResponses = MigrationPatternResponses()

                        migrationPatternResponses.fullyNomadic =
                            fullyNomadic.text.toString().toDouble()
                        migrationPatternResponses.semiNomadic =
                            semiNomadic.text.toString().toDouble()
                        migrationPatternResponses.occasionalNomadic =
                            occasionalNomadic.text.toString().toDouble()
                        migrationPatternResponses.outMigrantLabour =
                            outMigrantLabour.text.toString().toDouble()
                        migrationPatternResponses.inMigrantLabour =
                            inMigrantLabour.text.toString().toDouble()
                        migrationPatternResponses.fullysettled =
                            fullySettled.text.toString().toDouble()
                        migrationPatternResponses.internallyDisplaced =
                            internallyDisplaced.text.toString().toDouble()

                        wealthGroupQuestionnaire.migrationPatternResponses =
                            migrationPatternResponses

                        wgConstraints.root.visibility = View.VISIBLE
                        wgMigrationPatterns.root.visibility = View.GONE

                    }
                }
            }


            /*Constraints navigation */
            wgConstraints.apply {
                constraintsBackButton.setOnClickListener {
                    wgMigrationPatterns.root.visibility = View.VISIBLE
                    wgConstraints.root.visibility = View.GONE
                }

                constraintsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (labourLowEducation.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourLowEducationCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (labourPoorHealth.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourPoorHealthCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (labourFewJobs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourFewJobsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (labourFarmTime.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourFarmTimeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (labourLowWageRates.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourLowWageRatesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (consumptionHoldings.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionHoldingsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionLackOfCredit.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLackOfCreditCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (consumptionHighInputs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionHighInputsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionLowFertility.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLowFertilityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionUnreliableWater.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionUnreliableWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionLowTechnicalSkills.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLowTechnicalSkillsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionLowSeedQuality.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLowSeedQualityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionMarketAccess.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (consumptionCropPests.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionCropPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionPasture.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionPastureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionDrinkingWater.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionDrinkingWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionLowYieldingAnimal.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionLowYieldingAnimalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionVeterinaryDrugs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionVeterinaryDrugsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionPests.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionInsecurity.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionInsecurityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingLowStocks.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingLowStocksCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingPoorMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingPoorMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingLackOfEquipment.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingLackOfEquipmentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingCompetition.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingCompetitionCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingLackOfExpertise.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingLackOfExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingFishingRights.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingFishingRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (resourceDecline.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourceDeclineCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (resourcePopulationPressure.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourcePopulationPressureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (resourceRights.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourceRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (resourceLowValue.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourceLowValueCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (enterpriseLackOfCapital.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseLackOfCapitalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (enterpriseRedTape.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseRedTapeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (enterpriseTaxes.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseTaxesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (enterpriseMarketAccess.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (enterpriseExpertise.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }


                    if (hasNoValidationError) {

                        val constraintResponses = ConstraintsResponses()

                        val wagedLabourIncomeConstraintsResponses =
                            WagedLabourIncomeConstraintsResponses()

                        wagedLabourIncomeConstraintsResponses.lowEducation =
                            labourLowEducation.text.toString().toInt()
                        wagedLabourIncomeConstraintsResponses.poorHealth =
                            labourPoorHealth.text.toString().toInt()
                        wagedLabourIncomeConstraintsResponses.fewJobs =
                            labourFewJobs.text.toString().toInt()
                        wagedLabourIncomeConstraintsResponses.tooMuchFarmTime =
                            labourFarmTime.text.toString().toInt()
                        wagedLabourIncomeConstraintsResponses.lowAverageWageRates =
                            labourLowWageRates.text.toString().toInt()

                        constraintResponses.wagedLabourIncomeConstraintsResponses =
                            wagedLabourIncomeConstraintsResponses


                        val cropProductionIncomeConstraintsResponses =
                            CropProductionIncomeConstraintsResponses()

                        cropProductionIncomeConstraintsResponses.smallLandHoldings =
                            consumptionHoldings.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.lackOfCredit =
                            consumptionLackOfCredit.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.highInputCost =
                            consumptionHighInputs.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.lowLandFertility =
                            consumptionLowFertility.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.lackOfReliableWater =
                            consumptionUnreliableWater.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.lowTechnicalSkills =
                            consumptionLowTechnicalSkills.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.lowQualitySeed =
                            consumptionLowSeedQuality.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.lackOfMarketAccess =
                            consumptionMarketAccess.text.toString().toInt()
                        cropProductionIncomeConstraintsResponses.endemicCropPests =
                            consumptionCropPests.text.toString().toInt()

                        constraintResponses.cropProductionIncomeConstraintsResponses =
                            cropProductionIncomeConstraintsResponses

                        val livestockProductionIncomeConstraintsResponses =
                            LivestockProductionIncomeConstraintsResponses()

                        livestockProductionIncomeConstraintsResponses.lackOfPasture =
                            livestockProductionPasture.text.toString().toInt()
                        livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater =
                            livestockProductionDrinkingWater.text.toString().toInt()
                        livestockProductionIncomeConstraintsResponses.lowYieldingAnimal =
                            livestockProductionLowYieldingAnimal.text.toString().toInt()
                        livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs =
                            livestockProductionVeterinaryDrugs.text.toString().toInt()
                        livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases =
                            livestockProductionPests.text.toString().toInt()
                        livestockProductionIncomeConstraintsResponses.lackofMarket =
                            livestockProductionMarket.text.toString().toInt()
                        livestockProductionIncomeConstraintsResponses.insecurity =
                            livestockProductionInsecurity.text.toString().toInt()

                        constraintResponses.livestockProductionIncomeConstraintsResponses =
                            livestockProductionIncomeConstraintsResponses


                        val fishingIncomeConstraintsResponses = FishingIncomeConstraintsResponses()

                        fishingIncomeConstraintsResponses.lowFishStocks =
                            fishingLowStocks.text.toString().toInt()
                        fishingIncomeConstraintsResponses.poorMarket =
                            fishingPoorMarket.text.toString().toInt()
                        fishingIncomeConstraintsResponses.lackOfEquipment =
                            fishingLackOfEquipment.text.toString().toInt()
                        fishingIncomeConstraintsResponses.extremeCompetition =
                            fishingCompetition.text.toString().toInt()
                        fishingIncomeConstraintsResponses.lackOfExpertise =
                            fishingLackOfExpertise.text.toString().toInt()
                        fishingIncomeConstraintsResponses.fishingRightsRestrictions =
                            fishingFishingRights.text.toString().toInt()

                        constraintResponses.fishingIncomeConstraintsResponses =
                            fishingIncomeConstraintsResponses

                        val naturalResourceIncomeConstraintsResponses =
                            NaturalResourceIncomeConstraintsResponses()

                        naturalResourceIncomeConstraintsResponses.decliningNaturalResources =
                            resourceDecline.text.toString().toInt()
                        naturalResourceIncomeConstraintsResponses.populationPressure =
                            resourcePopulationPressure.text.toString().toInt()
                        naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights =
                            resourceRights.text.toString().toInt()
                        naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts =
                            resourceLowValue.text.toString().toInt()

                        constraintResponses.naturalResourceIncomeConstraintsResponses =
                            naturalResourceIncomeConstraintsResponses

                        val smallEnterpriseIncomeConstraintsResponses =
                            SmallEnterpriseIncomeConstraintsResponses()

                        smallEnterpriseIncomeConstraintsResponses.lackOfCapital =
                            enterpriseLackOfCapital.text.toString().toInt()
                        smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape =
                            enterpriseRedTape.text.toString().toInt()
                        smallEnterpriseIncomeConstraintsResponses.tooManyTaxes =
                            enterpriseTaxes.text.toString().toInt()
                        smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket =
                            enterpriseMarketAccess.text.toString().toInt()
                        smallEnterpriseIncomeConstraintsResponses.lackOfExpertise =
                            enterpriseExpertise.text.toString().toInt()

                        constraintResponses.smallEnterpriseIncomeConstraintsResponses =
                            smallEnterpriseIncomeConstraintsResponses

                        wealthGroupQuestionnaire.constraintsResponses = constraintResponses


                        wgCompletionPage.root.visibility = View.VISIBLE
                        wgConstraints.root.visibility = View.GONE

                    }
                }
            }


            /*wgCompletion page navigation*/
            wgCompletionPage.apply {
                closeButton.setOnClickListener {
                    wealthGroupQuestionnaire.questionnaireStatus =
                        QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION
                    wealthGroupQuestionnaire.questionnaireEndDate = Util.getNow()
                    val gson = Gson()
                    val sharedPreferences: SharedPreferences? =
                        context?.applicationContext?.getSharedPreferences(
                            "MyPref",
                            Context.MODE_PRIVATE
                        )
                    val editor: SharedPreferences.Editor? = sharedPreferences?.edit()


                    val questionnairesListString =
                        sharedPreferences?.getString(Constants.WEALTH_GROUP_LIST_OBJECT, null)
                    val questionnairesListObject: WealthGroupQuestionnaireListObject =
                        gson.fromJson(
                            questionnairesListString,
                            WealthGroupQuestionnaireListObject::class.java
                        )
                    questionnairesListObject.addQuestionnaire(wealthGroupQuestionnaire)
                    editor?.remove(Constants.WEALTH_GROUP_LIST_OBJECT)

                    val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
                    editor?.putString(
                        Constants.WEALTH_GROUP_LIST_OBJECT,
                        newQuestionnaireObjectString
                    )
                    editor?.commit()

                    val intent = Intent()
                    intent.action = Constants.QUESTIONNAIRE_COMPLETED
                    activity?.applicationContext?.sendBroadcast(intent)
                    this@WealthGroupDialogFragment.dismiss()

                }
            }
        }
    }

    private fun returnZeroStringIfEmpty(inputString: String): String {
        if (inputString.isNullOrEmpty()) {
            return "0"
        }
        return inputString
    }

    private fun isAnyCropProductionFieldEmpty(): Boolean {
        for (currentResponseItem in cropProductionResponseItems) {
            if (isAnyValueEmpty(currentResponseItem)) {
                return true
            }
        }
        return false
    }

    fun isAnyValueEmpty(currentResponseItem: WgCropProductionResponseItem): Boolean {
        return !currentResponseItem.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.irrigatedAverageYieldPerHa.hasBeenSubmitted
    }


    private fun inflateErrorModal(errorTitle: String, errorMessage: String) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
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
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
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


    private fun inflateSubCountyModal() {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.geographic_configuration_layout, null)

        openSubCountyModal(v)
    }

    private fun openSubCountyModal(v: View) {
        val builder: AlertDialog.Builder = activity?.let { AlertDialog.Builder(it) }!!
        builder.setView(v)
        builder.setCancelable(true)
        subContyDialog = builder.create()
        (subContyDialog as AlertDialog).setCancelable(true)
        (subContyDialog as AlertDialog).setCanceledOnTouchOutside(true)
        (subContyDialog as AlertDialog).window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        (subContyDialog as AlertDialog).show()
        val window = (subContyDialog as AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCropItemSelectedFromSelectionList(selectedCrop: CropModel, position: Int) {
        crops.set(position, selectedCrop)
        binding.apply {
            cropSelectionLayout.apply {
                activity?.let { context ->
                    val adapter =
                        CropSelectionListAdapter(
                            context,
                            R.layout.lz_selection_item,
                            crops,
                            this@WealthGroupDialogFragment
                        )
                    cropsList.adapter = adapter
                }
            }
        }

        if (selectedCrop.hasBeenSelected) {
            wealthGroupQuestionnaire.selectedCrops.add(selectedCrop)
        }
    }

    override fun onCropProductionResponseItemSubmited(
        responseItem: WgCropProductionResponseItem,
        position: Int
    ) {
        cropProductionResponseItems.set(position, responseItem)
    }
}