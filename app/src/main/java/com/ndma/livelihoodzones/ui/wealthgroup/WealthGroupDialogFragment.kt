package com.ndma.livelihoodzones.ui.wealthgroup


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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.appStore.AppStore
import com.ndma.livelihoodzones.appStore.model.WealthGroupQuestionnaire
import com.ndma.livelihoodzones.appStore.model.WealthGroupQuestionnaireListObject
import com.ndma.livelihoodzones.config.Constants
import com.ndma.livelihoodzones.databinding.WealthGroupQuestionnaireLayoutBinding
import com.ndma.livelihoodzones.login.model.GeographyObject
import com.ndma.livelihoodzones.ui.county.model.CropModel
import com.ndma.livelihoodzones.ui.county.model.QuestionnaireSessionLocation
import com.ndma.livelihoodzones.ui.home.HomeViewModel
import com.ndma.livelihoodzones.ui.model.*
import com.ndma.livelihoodzones.ui.wealthgroup.adapters.*
import com.ndma.livelihoodzones.ui.wealthgroup.model.ConstraintCategoryEnum
import com.ndma.livelihoodzones.ui.wealthgroup.model.ConstraintsTypeEnum
import com.ndma.livelihoodzones.ui.wealthgroup.model.FgdParticipantModel
import com.ndma.livelihoodzones.ui.wealthgroup.responses.*
import com.ndma.livelihoodzones.util.Util
import kotlin.math.abs

class WealthGroupDialogFragment : DialogFragment(),
    CropSelectionListAdapter.CropSelectionListAdapterCallBack,
    CropProductionListAdapter.CropProductionListAdapterCallBack,
    WgCropContributionAdapter.WgCropContributionAdapterCallBack,
    LivestockContributionRankAdapter.LivestockContributionRankAdapterCallBack,
    ConstraintsRankingAdapter.ConstraintsRankingAdapterCallBack,
    FgdParticipantsAdapter.FgdParticipantsAdapterCallBack {

    private lateinit var wealthGroupViewModel: WealthGroupViewModel

    private lateinit var binding: WealthGroupQuestionnaireLayoutBinding

    private lateinit var wealthGroupQuestionnaire: WealthGroupQuestionnaire

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    var isAResumeQuestionnaire: Boolean = false

    lateinit var geographyObject: GeographyObject

    var questionnaireSessionLocation: QuestionnaireSessionLocation? = null

    val livestockContributionResponses = LivestockContributionResponses()

    private var subContyDialog: AlertDialog? = null

    private var livestockContributionRankModal: AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    private var constraintsRankDialog: androidx.appcompat.app.AlertDialog? = null

    private lateinit var homeViewModel: HomeViewModel

    private var cropProductionResponseItems: MutableList<WgCropProductionResponseItem> = ArrayList()

    private var cropContributionResponseItems: MutableList<WgCropContributionResponseItem> =
        ArrayList()

    private var livestockCashIncomeContributionRanks: MutableList<RankResponseItem> = ArrayList()

    private var livestockFoodConsumptionContributionRanks: MutableList<RankResponseItem> =
        ArrayList()

    private var cropCashIncomeContributionRanks: MutableList<RankResponseItem> = ArrayList()
    private var cropFoodConsumptionContributionRanks: MutableList<RankResponseItem> = ArrayList()

    private var incomeSourceRanks: MutableList<RankResponseItem> = ArrayList()

    private var incomeConsumptionRanks: MutableList<RankResponseItem> = ArrayList()

    private var livestockProductionRanks: MutableList<RankResponseItem> = ArrayList()

    private var fishingRanks: MutableList<RankResponseItem> = ArrayList()

    private var naturalResourceRanks: MutableList<RankResponseItem> = ArrayList()

    private var smallEnterpriesRanks: MutableList<RankResponseItem> = ArrayList()

    private var crops: MutableList<CropModel> = ArrayList()

    val constraintResponses = ConstraintsResponses()

    val wagedLabourIncomeConstraintsResponses =
        WagedLabourIncomeConstraintsResponses()

    val cropProductionIncomeConstraintsResponses =
        CropProductionIncomeConstraintsResponses()

    val livestockProductionIncomeConstraintsResponses =
        LivestockProductionIncomeConstraintsResponses()

    val fishingIncomeConstraintsResponses = FishingIncomeConstraintsResponses()

    val naturalResourceIncomeConstraintsResponses =
        NaturalResourceIncomeConstraintsResponses()

    val smallEnterpriseIncomeConstraintsResponses =
        SmallEnterpriseIncomeConstraintsResponses()

    val fdgParticipantsModelList: MutableList<FgdParticipantModel> = ArrayList()

    companion object {

        private const val QUESTIONNAIRE_ID = "questionnaireId"

        private const val QUESTIONNAIRE_NAME = "questionnaireName"

        private const val QUESTIONNAIRE_SESSION_LOCATION = "sessionLocation"

        private const val IS_A_RESUME_QUESTIONNAIRE = "IS_A_RESUME_QUESTIONNAIRE"

        @JvmStatic
        fun newInstance(
            questionnaireId: String,
            questionnaireName: String,
            questionnaireSessionLocation: QuestionnaireSessionLocation?,
            isAResumeQuestionnaire: Boolean
        ) =
            WealthGroupDialogFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(QUESTIONNAIRE_ID, questionnaireId)
                        putString(QUESTIONNAIRE_NAME, questionnaireName)
                        putParcelable(QUESTIONNAIRE_SESSION_LOCATION, questionnaireSessionLocation)
                        putBoolean(IS_A_RESUME_QUESTIONNAIRE, isAResumeQuestionnaire)
                    }
                }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            questionnaireId = it.getString(QUESTIONNAIRE_ID)

            questionnaireName = it.getString(QUESTIONNAIRE_NAME)

            isAResumeQuestionnaire = it.getBoolean(IS_A_RESUME_QUESTIONNAIRE)

            if (!isAResumeQuestionnaire) {
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
                            wealthGroupQuestionnaire.questionnaireGeography.selectedLivelihoodZone?.livelihoodZoneName + " Livelihood Zone " + wealthGroupQuestionnaire.questionnaireGeography.selectedWealthGroup?.wealthGroupName + " questionnaire"
            } else {
                wealthGroupQuestionnaire = questionnaireId?.let { it1 ->
                    retrieveASpecificWealthGroupQuestionnaire(
                        it1
                    )
                }!!
            }
        }
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineNavigation() {
        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.MAIN_INCOME_AND_FOOD_SOURCE_STEP
        binding.apply {

            /*Income and food sources navigation*/
            wgIncomeAndFoodSources.apply {

                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(livestockProduction.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    pastureFodderProduction.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(
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
                pastureFodderProduction.addTextChangedListener(textWatcher)
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

                    if (pastureFodderProduction.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pastureFodderProductionWrapper.background =
                            context?.resources?.getDrawable(R.drawable.error_cell)
                        Toast.makeText(
                            context,
                            "No data provided for pasture/fodder production field",
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
                            pastureFodderProduction.text.toString()
                        ).toDouble() + returnZeroStringIfEmpty(
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

                        incomeAndFoodSourceResponses.pastureFodderProduction =
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

                        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.FOOD_CONSUMPTION_SOURCE_PERCENTAGE_STEP

                        wgIncomeAndFoodSources.root.visibility = View.GONE
                        wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE

                    }
                }
            }


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

                        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.WG_CROP_SELECTION_STEP
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

                    if (wealthGroupQuestionnaire.selectedCrops.isNotEmpty()) {

                        for (currentCrop in wealthGroupQuestionnaire.selectedCrops) {
                            cropContributionResponseItems.add(
                                WgCropContributionResponseItem(
                                    currentCrop,
                                    CropContributionResponseValue(0.0, false),
                                    CropContributionResponseValue(0.0, false),
                                    CropContributionResponseValue(0.0, false),
                                    CropContributionResponseValue(0.0, false)
                                )
                            )
                        }


                        for (i in 0..cropContributionResponseItems.size - 1) {
                            cropCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    i + 1,
                                    false
                                )
                            )
                            cropFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    i + 1,
                                    false
                                )
                            )
                        }

                        cropProductionLayout.apply {

                            val cropContributionAdapter =
                                activity?.let { it1 ->
                                    WgCropContributionAdapter(
                                        cropContributionResponseItems,
                                        this@WealthGroupDialogFragment,
                                        it1,
                                        cropCashIncomeContributionRanks,
                                        cropFoodConsumptionContributionRanks
                                    )
                                }
                            val gridLayoutManager = GridLayoutManager(activity, 1)
                            cropResponseList.layoutManager = gridLayoutManager
                            cropResponseList.hasFixedSize()
                            cropResponseList.adapter =
                                cropContributionAdapter
                        }

                        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.WG_CROP_PRODUCTION_STEP

                        cropProductionLayout.root.visibility = View.VISIBLE
                        cropSelectionLayout.root.visibility = View.GONE

                    } else {
                        inflateErrorModal("Data error", "You have not selected any crop")
                    }
                }

            }


            /* Crop production layout */

            cropProductionLayout.apply {

                cropContributionBackButton.setOnClickListener {
                    cropProductionLayout.root.visibility = View.GONE
                    cropSelectionLayout.root.visibility = View.VISIBLE
                }

                cropContributionNextButton.setOnClickListener {
                    if (!isAnyCropContributionValueEmpty() && !doesCropFoodConsumptionContributionIncomeHaveAPercentageError().hasError && !doesCropCashContributionIncomeHaveAPercentageError().hasError) {
                        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.LIVESTOCK_POULTRY_NUMBERS_STEP
                        cropProductionLayout.root.visibility = View.GONE
                        wgLivestockPoultryNumbers.root.visibility = View.VISIBLE
                    } else if (isAnyCropContributionValueEmpty()) {
                        inflateErrorModal("Missing data", "Kindly fill out all the missing data")
                    } else if (doesCropCashContributionIncomeHaveAPercentageError().hasError) {
                        inflateErrorModal(
                            "Percentage error",
                            doesCropCashContributionIncomeHaveAPercentageError().errorMessage
                        )
                    } else if (doesCropFoodConsumptionContributionIncomeHaveAPercentageError().hasError) {
                        inflateErrorModal(
                            "Percentage error",
                            doesCropFoodConsumptionContributionIncomeHaveAPercentageError().errorMessage
                        )
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
                    if (dairyCattleNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        dairyCattleNumbersCell.background =
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
                    if (improvedChickenNumbers.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        improvedChickenNumbersCell.background =
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
                        livestockPoultryOwnershipResponses.dairyCattle =
                            dairyCattleNumbers.text.toString().toDouble()
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
                        livestockPoultryOwnershipResponses.improvedChicken =
                            improvedChickenNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.ducks =
                            duckNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.beeHives =
                            beeHiveNumbers.text.toString().toDouble()
                        livestockPoultryOwnershipResponses.fishPonds =
                            fishPondNumbers.text.toString().toDouble()

                        wealthGroupQuestionnaire.livestockPoultryOwnershipResponses =
                            livestockPoultryOwnershipResponses
                        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.LIVESTOCK_POULTRY_CONTRIBUTION_STEP
                        wgLivestockPoultryContribution.root.visibility = View.VISIBLE
                        wgLivestockPoultryNumbers.root.visibility = View.GONE

                    }
                }
            }


            /*Livestock and poultry contribution navigation*/
            wgLivestockPoultryContribution.apply {

                for (i in 0..11) {
                    livestockCashIncomeContributionRanks.add(
                        RankResponseItem(i + 1, false)
                    )
                    livestockFoodConsumptionContributionRanks.add(
                        RankResponseItem(i + 1, false)
                    )
                }
                cattleIncomeRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.CATTLE
                    )
                }

                cattleFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.CATTLE
                    )
                }

                dairyCattleIncomeRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.DAIRY_CATTLE
                    )
                }

                dairyCattleFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.DAIRY_CATTLE
                    )
                }

                goatsIncomeRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.GOATS
                    )
                }

                goatsFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.GOATS
                    )
                }

                sheepCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.SHEEP
                    )
                }

                sheepFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.SHEEP
                    )
                }

                donkeysCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.DONKEYS
                    )
                }

                donkeysFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.DONKEYS
                    )
                }

                pigscashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.PIGS
                    )
                }

                pigsFoodrank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.PIGS
                    )
                }

                chickenCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.CHICKEN
                    )
                }

                chickenFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.CHICKEN
                    )
                }

                improvedChickenCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.IMPROVED_CHICKEN
                    )
                }

                improvedChickenFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.IMPROVED_CHICKEN
                    )
                }

                camelsCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.CAMELS
                    )
                }

                camelsFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.CAMELS
                    )
                }

                ducksCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.DUCKS
                    )
                }

                ducksFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.DUCKS
                    )
                }

                beeHivesCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.BEE_HIVES
                    )
                }

                beeHivesFoodrank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.BEE_HIVES
                    )
                }

                fishPondsCashRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockCashIncomeContributionRanks,
                        LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                        WgLivestockTypesEnum.FISH_POND
                    )
                }

                fishPondsFoodRank.setOnClickListener {
                    inflateLivestockContributionRankModal(
                        livestockFoodConsumptionContributionRanks,
                        LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                        WgLivestockTypesEnum.FISH_POND
                    )
                }



                livestockPoultryContributionBackButton.setOnClickListener {
                    wgLivestockPoultryNumbers.root.visibility = View.VISIBLE
                    wgLivestockPoultryContribution.root.visibility = View.GONE
                }

                livestockPoultryContributionNextButton.setOnClickListener {

                    if (cattleCashPercentage.text.toString()
                            .isEmpty() || cattleFoodPercentage.text.toString().isEmpty()
                        || dairyCattleCashPercentage.text.toString()
                            .isEmpty() || dairyCattleFoodPercentage.text.toString().isEmpty()
                        || goatsIncomePercentage.text.toString()
                            .isEmpty() || goatsFoodPercentage.text.toString().isEmpty()
                        || sheepCashPercentage.text.toString()
                            .isEmpty() || sheepFoodPercentage.text.toString().isEmpty()
                        || donkeysCashPercentage.text.toString()
                            .isEmpty() || donkeysFoodPercentage.text.toString().isEmpty()
                        || pigsCashPercentage.text.toString()
                            .isEmpty() || pigsFoodPercentage.text.toString().isEmpty()
                        || chickenCashPaercentage.text.toString()
                            .isEmpty() || chickenFoodPercentage.text.toString().isEmpty()
                        || improvedChickenCashPaercentage.text.toString()
                            .isEmpty() || improvedChickenFoodPercentage.text.toString().isEmpty()
                        || camelsCashPercentage.text.toString()
                            .isEmpty() || camelsFoodPercentage.text.toString().isEmpty()
                        || duckscashPercentage.text.toString()
                            .isEmpty() || ducksFoodPercentage.text.toString().isEmpty()
                        || beeHivesCashPercentage.text.toString()
                            .isEmpty() || beeHivesFoodPercentage.text.toString().isEmpty()
                        || fishPondscashPercentage.text.toString()
                            .isEmpty() || fishPondsFoodPercentage.text.toString().isEmpty()
                        || !livestockContributionResponses.cattle.incomeRank.hasBeenSubmitted || !livestockContributionResponses.cattle.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.dairyCattle.incomeRank.hasBeenSubmitted || !livestockContributionResponses.dairyCattle.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.goats.incomeRank.hasBeenSubmitted || !livestockContributionResponses.goats.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.sheep.incomeRank.hasBeenSubmitted || !livestockContributionResponses.sheep.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.donkeys.incomeRank.hasBeenSubmitted || !livestockContributionResponses.donkeys.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.pigs.incomeRank.hasBeenSubmitted || !livestockContributionResponses.pigs.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.chicken.incomeRank.hasBeenSubmitted || !livestockContributionResponses.chicken.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.improvedChicken.incomeRank.hasBeenSubmitted || !livestockContributionResponses.improvedChicken.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.camels.incomeRank.hasBeenSubmitted || !livestockContributionResponses.camels.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.ducks.incomeRank.hasBeenSubmitted || !livestockContributionResponses.ducks.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.beeHives.incomeRank.hasBeenSubmitted || !livestockContributionResponses.beeHives.consumptionRank.hasBeenSubmitted
                        || !livestockContributionResponses.fishPonds.incomeRank.hasBeenSubmitted || !livestockContributionResponses.fishPonds.consumptionRank.hasBeenSubmitted
                    ) {

                        inflateErrorModal("Data error", "Kindly input all the empty fields")

                    } else {

                        if (doesLivestockCashContributionColumnHaveAPercentageError().hasAValidationError) {
                            inflateErrorModal(
                                "Percentage error",
                                doesLivestockCashContributionColumnHaveAPercentageError().validationMessage
                            )
                        } else if (doesLivestockFoodConsumptionContributionColumnHaveAPercentageError().hasAValidationError) {
                            inflateErrorModal(
                                "Percentage error",
                                doesLivestockFoodConsumptionContributionColumnHaveAPercentageError().validationMessage
                            )
                        } else {

                            livestockContributionResponses.cattle.incomePercentage.actualValue =
                                cattleCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.cattle.consumptionPercentage.actualValue =
                                cattleFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.dairyCattle.incomePercentage.actualValue =
                                dairyCattleCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.dairyCattle.consumptionPercentage.actualValue =
                                dairyCattleFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.goats.incomePercentage.actualValue =
                                goatsIncomePercentage.text.toString().toDouble()
                            livestockContributionResponses.goats.consumptionPercentage.actualValue =
                                goatsFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.sheep.incomePercentage.actualValue =
                                sheepCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.sheep.consumptionPercentage.actualValue =
                                sheepFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.donkeys.incomePercentage.actualValue =
                                donkeysCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.donkeys.consumptionPercentage.actualValue =
                                donkeysFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.pigs.incomePercentage.actualValue =
                                pigsCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.pigs.consumptionPercentage.actualValue =
                                pigsFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.chicken.incomePercentage.actualValue =
                                chickenCashPaercentage.text.toString().toDouble()
                            livestockContributionResponses.chicken.consumptionPercentage.actualValue =
                                chickenFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.improvedChicken.incomePercentage.actualValue =
                                improvedChickenCashPaercentage.text.toString().toDouble()
                            livestockContributionResponses.improvedChicken.consumptionPercentage.actualValue =
                                improvedChickenFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.camels.incomePercentage.actualValue =
                                camelsCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.camels.consumptionPercentage.actualValue =
                                camelsFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.ducks.incomePercentage.actualValue =
                                duckscashPercentage.text.toString().toDouble()
                            livestockContributionResponses.ducks.consumptionPercentage.actualValue =
                                ducksFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.beeHives.incomePercentage.actualValue =
                                beeHivesCashPercentage.text.toString().toDouble()
                            livestockContributionResponses.beeHives.consumptionPercentage.actualValue =
                                beeHivesFoodPercentage.text.toString().toDouble()

                            livestockContributionResponses.fishPonds.incomePercentage.actualValue =
                                fishPondscashPercentage.text.toString().toDouble()
                            livestockContributionResponses.fishPonds.consumptionPercentage.actualValue =
                                fishPondsFoodPercentage.text.toString().toDouble()

                            wealthGroupQuestionnaire.livestockContributionResponses =
                                livestockContributionResponses

                            wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.LABOUR_PATTERNS_STEP

                            wgLabourPatterns.root.visibility = View.VISIBLE
                            wgLivestockPoultryContribution.root.visibility = View.GONE

                        }

                    }
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
                                ).toDouble() + returnZeroStringIfEmpty(transportServicesMen.text.toString()).toDouble() + returnZeroStringIfEmpty(wagedLabourmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
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
                transportServicesMen.addTextChangedListener(mentTextWatcher)
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
                                ).toDouble() + returnZeroStringIfEmpty(transportServicesWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(wagedLabourWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
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
                transportServicesWomen.addTextChangedListener(womentTextWatcher)
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
                    if (transportServicesMen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        transportServicesMenCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (transportServicesWomen.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        transportServicesWomenCell.background =
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


                        val menTotalEntry =
                            returnZeroStringIfEmpty(ownFarmmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                livestockHusbandrymen.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(transportServicesMen.text.toString()).toDouble() + returnZeroStringIfEmpty(wagedLabourmen.text.toString()).toDouble() + returnZeroStringIfEmpty(
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


                        val womenTotalEntry =
                            returnZeroStringIfEmpty(ownFarmWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                livestockHusbandryWomen.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(transportServicesWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(wagedLabourWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
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



                        if (menTotalEntry == 100.0 && womenTotalEntry == 100.0) {

                            val labourPatternResponse = LabourPatternResponse()
                            labourPatternResponse.ownFarmCropProduction = LabourPatternResponseItem(
                                ownFarmWomen.text.toString().toDouble(),
                                ownFarmmen.text.toString().toDouble()
                            )

                            labourPatternResponse.livestockHusbandry = LabourPatternResponseItem(
                                livestockHusbandryWomen.text.toString().toDouble(),
                                livestockHusbandrymen.text.toString().toDouble()
                            )

                            labourPatternResponse.transportServices = LabourPatternResponseItem(
                                transportServicesWomen.text.toString().toDouble(),
                                transportServicesMen.text.toString().toDouble()
                            )

                            labourPatternResponse.wagedLabourOnFarms = LabourPatternResponseItem(
                                wagedLabourWomen.text.toString().toDouble(),
                                wagedLabourmen.text.toString().toDouble()
                            )

                            labourPatternResponse.lowSkilledNonFarmLabour =
                                LabourPatternResponseItem(
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

                            wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.EXPENDITURE_PATTERNS_STEP

                            wgExpenditurePatterns.root.visibility = View.VISIBLE
                            wgLabourPatterns.root.visibility = View.GONE

                        } else if (menTotalEntry < 100.0) {
                            inflateErrorModal(
                                "Percentage error",
                                "Male total entries are less than 100% by ${100 - menTotalEntry}"
                            )
                        } else if (womenTotalEntry < 100.0) {
                            inflateErrorModal(
                                "Percentage error",
                                "Female total entries are less than 100% by ${100 - womenTotalEntry}"
                            )
                        }

                    }
                }
            }


            /*Expenditure patterns navigation */
            wgExpenditurePatterns.apply {


                val foodItemsTextWatcher = object : TextWatcher {
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
                                ).toDouble() + returnZeroStringIfEmpty(otherFoods.text.toString()).toDouble()

                            if (totalEntry > 100) {
                                val excessValue = totalEntry - 100.0
                                errorDialog?.isShowing?.let { isDialogShowing ->
                                    if (isDialogShowing) {
                                        return@postDelayed
                                    }
                                }

                                inflateErrorModal(
                                    "Percentage error",
                                    "Food entries exceed 100% by $excessValue"
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

                maizeAndMaizeFlour.addTextChangedListener(foodItemsTextWatcher)
                otherCereals.addTextChangedListener(foodItemsTextWatcher)
                pulses.addTextChangedListener(foodItemsTextWatcher)
                rootsAndTubers.addTextChangedListener(foodItemsTextWatcher)
                vegetablesAndFruits.addTextChangedListener(foodItemsTextWatcher)
                fishandseaFood.addTextChangedListener(foodItemsTextWatcher)
                meat.addTextChangedListener(foodItemsTextWatcher)
                milk.addTextChangedListener(foodItemsTextWatcher)
                eggs.addTextChangedListener(foodItemsTextWatcher)
                oilAndFats.addTextChangedListener(foodItemsTextWatcher)
                otherFoods.addTextChangedListener(foodItemsTextWatcher)


                val nonFoodItemsTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(
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

                schoolFees.addTextChangedListener(nonFoodItemsTextWatcher)
                drugsAndMedicalCare.addTextChangedListener(nonFoodItemsTextWatcher)
                clothingAndBeautyProducts.addTextChangedListener(nonFoodItemsTextWatcher)
                houseRent.addTextChangedListener(nonFoodItemsTextWatcher)
                communicationExpense.addTextChangedListener(nonFoodItemsTextWatcher)
                farmInputs.addTextChangedListener(nonFoodItemsTextWatcher)
                livestockDrugs.addTextChangedListener(nonFoodItemsTextWatcher)
                waterPurchase.addTextChangedListener(nonFoodItemsTextWatcher)
                soaps.addTextChangedListener(nonFoodItemsTextWatcher)
                farrmLabour.addTextChangedListener(nonFoodItemsTextWatcher)
                travelRelatedExpense.addTextChangedListener(nonFoodItemsTextWatcher)
                entertainment.addTextChangedListener(nonFoodItemsTextWatcher)
                electricityBill.addTextChangedListener(nonFoodItemsTextWatcher)
                socialObligation.addTextChangedListener(nonFoodItemsTextWatcher)
                millingCost.addTextChangedListener(nonFoodItemsTextWatcher)
                cookingFuel.addTextChangedListener(nonFoodItemsTextWatcher)
                savingsAndInvestment.addTextChangedListener(nonFoodItemsTextWatcher)
                loanRepayments.addTextChangedListener(nonFoodItemsTextWatcher)


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

                    if (hasNoValidationError) {

                        val totalFoodPercentageEntry =
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
                            ).toDouble() + returnZeroStringIfEmpty(otherFoods.text.toString()).toDouble()

                        val totalNonFoodPercentageEntry =
                            returnZeroStringIfEmpty(
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

                        if (totalFoodPercentageEntry == 100.0 && totalNonFoodPercentageEntry == 100.0) {

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

                            wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.MIGRATION_PATTERNS_STEP

                            wgMigrationPatterns.root.visibility = View.VISIBLE
                            wgExpenditurePatterns.root.visibility = View.GONE

                        } else if (totalFoodPercentageEntry < 100.0) {
                            inflateErrorModal(
                                "Percentage error",
                                "Total food entries are  less than 100% by ${100.0 - totalFoodPercentageEntry}"
                            )
                        } else if (totalNonFoodPercentageEntry < 100.0) {
                            inflateErrorModal(
                                "Percentage error",
                                "Total non-food entries are  less than 100% by ${100.0 - totalNonFoodPercentageEntry}"
                            )
                        }

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

                        val totalEntry =
                            returnZeroStringIfEmpty(fullyNomadic.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                semiNomadic.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(occasionalNomadic.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                outMigrantLabour.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(inMigrantLabour.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                fullySettled.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(internallyDisplaced.text.toString()).toDouble()

                        if (totalEntry == 100.0) {

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

                            wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.CONSTRAINTS_STEP

                            wgConstraints.root.visibility = View.VISIBLE
                            wgMigrationPatterns.root.visibility = View.GONE

                        } else if (totalEntry < 100.0) {
                            inflateErrorModal(
                                "Percentage error",
                                "The total entries are less than 100% by ${100.0 - totalEntry}"
                            )
                        }

                    }
                }
            }


            /*Constraints navigation */
            wgConstraints.apply {
                constraintsBackButton.setOnClickListener {
                    wgMigrationPatterns.root.visibility = View.VISIBLE
                    wgConstraints.root.visibility = View.GONE
                }

                for (i in 0..4) {
                    incomeSourceRanks.add(RankResponseItem(i + 1, false))
                }
                for (i in 0..9) {
                    incomeConsumptionRanks.add(RankResponseItem(i + 1, false))
                }
                for (i in 0..7) {
                    livestockProductionRanks.add(RankResponseItem(i + 1, false))
                }
                for (i in 0..6) {
                    fishingRanks.add(RankResponseItem(i + 1, false))
                }
                for (i in 0..4) {
                    naturalResourceRanks.add(RankResponseItem(i + 1, false))
                }
                for (i in 0..5) {
                    smallEnterpriesRanks.add(RankResponseItem(i + 1, false))
                }

                labourLowEducation.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeSourceRanks,
                        ConstraintsTypeEnum.IS_LOW_EDUCATION,
                        ConstraintCategoryEnum.SOURCE_OF_INCOME
                    )
                }
                labourPoorHealth.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeSourceRanks,
                        ConstraintsTypeEnum.IS_POOR_HEALTH,
                        ConstraintCategoryEnum.SOURCE_OF_INCOME
                    )
                }
                labourFewJobs.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeSourceRanks,
                        ConstraintsTypeEnum.IS_FEW_JOBS,
                        ConstraintCategoryEnum.SOURCE_OF_INCOME
                    )
                }
                labourFarmTime.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeSourceRanks,
                        ConstraintsTypeEnum.IS_TIME_ON_FARM,
                        ConstraintCategoryEnum.SOURCE_OF_INCOME
                    )
                }
                labourLowWageRates.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeSourceRanks,
                        ConstraintsTypeEnum.IS_LOW_WAGE_RATES,
                        ConstraintCategoryEnum.SOURCE_OF_INCOME
                    )
                }


                consumptionHoldings.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_SMALL_LAND,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionLackOfCredit.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_LACK_OF_CREDIT,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionHighInputs.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_HIGH_INPUT_COSTS,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionLowFertility.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_LOW_LAND_FERTILITY,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionUnreliableWater.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_UNRELIABLE_WATER,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionLowTechnicalSkills.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_LOW_TECHNICAL_SKILLS,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionLowSeedQuality.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_LOW_QUALITY_SEED,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionMarketAccess.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_MARKET_ACCESS,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }
                consumptionCropPests.setOnClickListener {
                    inflateConstraintsRankModal(
                        incomeConsumptionRanks,
                        ConstraintsTypeEnum.IC_CROP_PESTS_DISEASES,
                        ConstraintCategoryEnum.INCOME_CONSUMPTION
                    )
                }


                livestockProductionPasture.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_LACK_OF_PASTURE,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }
                livestockProductionDrinkingWater.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_LACK_ANIMAL_DRINKING_WATER,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }
                livestockProductionLowYieldingAnimal.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_LOW_YIELDING_ANIMALS,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }
                livestockProductionVeterinaryDrugs.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_COSTLY_VETERINARY_DRUGS,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }
                livestockProductionPests.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_LIVESTOCK_PESTS_DISEASES,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }
                livestockProductionMarket.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_LACK_OF_MARKET,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }
                livestockProductionInsecurity.setOnClickListener {
                    inflateConstraintsRankModal(
                        livestockProductionRanks,
                        ConstraintsTypeEnum.LP_INSECURITY,
                        ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                    )
                }


                fishingLowStocks.setOnClickListener {
                    inflateConstraintsRankModal(
                        fishingRanks,
                        ConstraintsTypeEnum.F_LOW_FISH_STOCKS,
                        ConstraintCategoryEnum.FISHING
                    )
                }
                fishingPoorMarket.setOnClickListener {
                    inflateConstraintsRankModal(
                        fishingRanks,
                        ConstraintsTypeEnum.F_POOR_MARKET,
                        ConstraintCategoryEnum.FISHING
                    )
                }
                fishingLackOfEquipment.setOnClickListener {
                    inflateConstraintsRankModal(
                        fishingRanks,
                        ConstraintsTypeEnum.F_EQUIPMENT,
                        ConstraintCategoryEnum.FISHING
                    )
                }
                fishingCompetition.setOnClickListener {
                    inflateConstraintsRankModal(
                        fishingRanks,
                        ConstraintsTypeEnum.F_COMPETITION,
                        ConstraintCategoryEnum.FISHING
                    )
                }
                fishingLackOfExpertise.setOnClickListener {
                    inflateConstraintsRankModal(
                        fishingRanks,
                        ConstraintsTypeEnum.F_LACK_OF_EXPERTISE,
                        ConstraintCategoryEnum.FISHING
                    )
                }
                fishingFishingRights.setOnClickListener {
                    inflateConstraintsRankModal(
                        fishingRanks,
                        ConstraintsTypeEnum.F_FISHING_RIGHTS,
                        ConstraintCategoryEnum.FISHING
                    )
                }


                resourceDecline.setOnClickListener {
                    inflateConstraintsRankModal(
                        naturalResourceRanks,
                        ConstraintsTypeEnum.NR_DECLINING_RESOURCE,
                        ConstraintCategoryEnum.NATURAL_RESOURCE
                    )
                }
                resourcePopulationPressure.setOnClickListener {
                    inflateConstraintsRankModal(
                        naturalResourceRanks,
                        ConstraintsTypeEnum.NR_POPULATION_PRESSURE,
                        ConstraintCategoryEnum.NATURAL_RESOURCE
                    )
                }
                resourceRights.setOnClickListener {
                    inflateConstraintsRankModal(
                        naturalResourceRanks,
                        ConstraintsTypeEnum.NR_RIGHTS_RESTRICTIONS,
                        ConstraintCategoryEnum.NATURAL_RESOURCE
                    )
                }
                resourceLowValue.setOnClickListener {
                    inflateConstraintsRankModal(
                        naturalResourceRanks,
                        ConstraintsTypeEnum.NR_LOW_VALUE,
                        ConstraintCategoryEnum.NATURAL_RESOURCE
                    )
                }


                enterpriseLackOfCapital.setOnClickListener {
                    inflateConstraintsRankModal(
                        smallEnterpriesRanks,
                        ConstraintsTypeEnum.SE_LACK_OF_CAPITAL,
                        ConstraintCategoryEnum.SMALL_ENTERPRISE
                    )
                }
                enterpriseRedTape.setOnClickListener {
                    inflateConstraintsRankModal(
                        smallEnterpriesRanks,
                        ConstraintsTypeEnum.SE_RED_TAPE,
                        ConstraintCategoryEnum.SMALL_ENTERPRISE
                    )
                }
                enterpriseTaxes.setOnClickListener {
                    inflateConstraintsRankModal(
                        smallEnterpriesRanks,
                        ConstraintsTypeEnum.SE_TAXES,
                        ConstraintCategoryEnum.SMALL_ENTERPRISE
                    )
                }
                enterpriseMarketAccess.setOnClickListener {
                    inflateConstraintsRankModal(
                        smallEnterpriesRanks,
                        ConstraintsTypeEnum.SE_MARKET_ACCESS,
                        ConstraintCategoryEnum.SMALL_ENTERPRISE
                    )
                }
                enterpriseExpertise.setOnClickListener {
                    inflateConstraintsRankModal(
                        smallEnterpriesRanks,
                        ConstraintsTypeEnum.SE_LACK_OF_EXPERTISE,
                        ConstraintCategoryEnum.SMALL_ENTERPRISE
                    )
                }

                constraintsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (wagedLabourIncomeConstraintsResponses.lowEducation == 0) {
                        hasNoValidationError = false
                        labourLowEducationCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.poorHealth == 0) {
                        hasNoValidationError = false
                        labourPoorHealthCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.fewJobs == 0) {
                        hasNoValidationError = false
                        labourFewJobsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.tooMuchFarmTime == 0) {
                        hasNoValidationError = false
                        labourFarmTimeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.lowAverageWageRates == 0) {
                        hasNoValidationError = false
                        labourLowWageRatesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (cropProductionIncomeConstraintsResponses.smallLandHoldings == 0) {
                        hasNoValidationError = false
                        consumptionHoldingsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lackOfCredit == 0) {
                        hasNoValidationError = false
                        consumptionLackOfCreditCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (cropProductionIncomeConstraintsResponses.highInputCost == 0) {
                        hasNoValidationError = false
                        consumptionHighInputsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lowLandFertility == 0) {
                        hasNoValidationError = false
                        consumptionLowFertilityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lackOfReliableWater == 0) {
                        hasNoValidationError = false
                        consumptionUnreliableWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lowTechnicalSkills == 0) {
                        hasNoValidationError = false
                        consumptionLowTechnicalSkillsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lowQualitySeed == 0) {
                        hasNoValidationError = false
                        consumptionLowSeedQualityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lackOfMarketAccess == 0) {
                        hasNoValidationError = false
                        consumptionMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.endemicCropPests == 0) {
                        hasNoValidationError = false
                        consumptionCropPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lackOfPasture == 0) {
                        hasNoValidationError = false
                        livestockProductionPastureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater == 0) {
                        hasNoValidationError = false
                        livestockProductionDrinkingWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lowYieldingAnimal == 0) {
                        hasNoValidationError = false
                        livestockProductionLowYieldingAnimalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs == 0) {
                        hasNoValidationError = false
                        livestockProductionVeterinaryDrugsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases == 0) {
                        hasNoValidationError = false
                        livestockProductionPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lackofMarket == 0) {
                        hasNoValidationError = false
                        livestockProductionMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.insecurity == 0) {
                        hasNoValidationError = false
                        livestockProductionInsecurityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.lowFishStocks == 0) {
                        hasNoValidationError = false
                        fishingLowStocksCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.poorMarket == 0) {
                        hasNoValidationError = false
                        fishingPoorMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.lackOfEquipment == 0) {
                        hasNoValidationError = false
                        fishingLackOfEquipmentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.extremeCompetition == 0) {
                        hasNoValidationError = false
                        fishingCompetitionCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.lackOfExpertise == 0) {
                        hasNoValidationError = false
                        fishingLackOfExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.fishingRightsRestrictions == 0) {
                        hasNoValidationError = false
                        fishingFishingRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.decliningNaturalResources == 0) {
                        hasNoValidationError = false
                        resourceDeclineCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.populationPressure == 0) {
                        hasNoValidationError = false
                        resourcePopulationPressureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights == 0) {
                        hasNoValidationError = false
                        resourceRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts == 0) {
                        hasNoValidationError = false
                        resourceLowValueCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfCapital == 0) {
                        hasNoValidationError = false
                        enterpriseLackOfCapitalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape == 0) {
                        hasNoValidationError = false
                        enterpriseRedTapeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.tooManyTaxes == 0) {
                        hasNoValidationError = false
                        enterpriseTaxesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket == 0) {
                        hasNoValidationError = false
                        enterpriseMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfExpertise == 0) {
                        hasNoValidationError = false
                        enterpriseExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }


                    if (hasNoValidationError) {


                        constraintResponses.wagedLabourIncomeConstraintsResponses =
                            wagedLabourIncomeConstraintsResponses


                        constraintResponses.cropProductionIncomeConstraintsResponses =
                            cropProductionIncomeConstraintsResponses


                        constraintResponses.livestockProductionIncomeConstraintsResponses =
                            livestockProductionIncomeConstraintsResponses


                        constraintResponses.fishingIncomeConstraintsResponses =
                            fishingIncomeConstraintsResponses


                        constraintResponses.naturalResourceIncomeConstraintsResponses =
                            naturalResourceIncomeConstraintsResponses


                        constraintResponses.smallEnterpriseIncomeConstraintsResponses =
                            smallEnterpriseIncomeConstraintsResponses

                        wealthGroupQuestionnaire.constraintsResponses = constraintResponses

                        wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.COPING_STRATEGIES_STEP

                        wgCopingStrategies.root.visibility = View.VISIBLE
                        wgConstraints.root.visibility = View.GONE

                    }
                }
            }

            /* I) Coping strategies */
            wgCopingStrategies.apply {

                copingBackButton.setOnClickListener {
                    wgCopingStrategies.root.visibility = View.GONE
                    wgConstraints.root.visibility = View.VISIBLE
                }

                copingNextButton.setOnClickListener {

                    if (soldHouseHoldAssets.text.toString()
                            .isEmpty() || reducedNonFoodExpenses.text.toString()
                            .isEmpty() || soldProductiveAssets.text.toString()
                            .isEmpty() || spentSavings.text.toString()
                            .isEmpty() || borrowedMoneyFromLender.text.toString()
                            .isEmpty() || soldHouseOrLand.text.toString()
                            .isEmpty() || withdrewSchoolChildren.text.toString()
                            .isEmpty() || soldFemaleAnimals.text.toString()
                            .isEmpty() || begging.text.toString()
                            .isEmpty() || soldMoreAnimals.text.toString()
                            .isEmpty() || lessExpensiveFood.text.toString()
                            .isEmpty() || reducedFoodQuantity.text.toString()
                            .isEmpty() || borrowedFood.text.toString()
                            .isEmpty() || reducedNoMealsPerDay.text.toString()
                            .isEmpty() || reducedMealPortionSize.text.toString().isEmpty()
                    ) {
                        inflateErrorModal("Data error", "Kindly input all the empty fields")
                    } else {

                        if (consumptionStrategiesResponseMoreThanSevenDays(lessExpensiveFood.text.toString()) || consumptionStrategiesResponseMoreThanSevenDays(
                                reducedFoodQuantity.text.toString()
                            ) || consumptionStrategiesResponseMoreThanSevenDays(borrowedFood.text.toString()) || consumptionStrategiesResponseMoreThanSevenDays(
                                reducedNoMealsPerDay.text.toString()
                            ) || consumptionStrategiesResponseMoreThanSevenDays(
                                reducedMealPortionSize.text.toString()
                            )
                        ) {
                            inflateErrorModal(
                                "Validation error",
                                "A response on consumption based strategies(part a) is greater than 7"
                            )
                        } else if (livelihoodStrategiesResponseNotWithinRange(soldHouseHoldAssets.text.toString()) || livelihoodStrategiesResponseNotWithinRange(
                                reducedNonFoodExpenses.text.toString()
                            ) || livelihoodStrategiesResponseNotWithinRange(
                                soldProductiveAssets.text.toString()
                            ) || livelihoodStrategiesResponseNotWithinRange(spentSavings.text.toString()) || livelihoodStrategiesResponseNotWithinRange(
                                borrowedMoneyFromLender.text.toString()
                            ) || livelihoodStrategiesResponseNotWithinRange(soldHouseOrLand.text.toString()) || livelihoodStrategiesResponseNotWithinRange(
                                withdrewSchoolChildren.text.toString()
                            ) || livelihoodStrategiesResponseNotWithinRange(soldFemaleAnimals.text.toString()) || livelihoodStrategiesResponseNotWithinRange(
                                begging.text.toString()
                            ) || livelihoodStrategiesResponseNotWithinRange(soldMoreAnimals.text.toString())
                        ) {
                            inflateErrorModal(
                                "Validation error",
                                "Some responses under  the livelihood based strategies are not within range"
                            )
                        } else {

                            val copingStrategiesResponses = CopingStrategiesResponses()

                            val consumptionBasedStrategies = ConsumptionBasedStrategies()
                            consumptionBasedStrategies.lessExpensiveFood =
                                lessExpensiveFood.text.toString().toDouble()
                            consumptionBasedStrategies.reducedAdultFoodQuantity =
                                reducedFoodQuantity.text.toString().toDouble()
                            consumptionBasedStrategies.borrowedFood =
                                borrowedFood.text.toString().toDouble()
                            consumptionBasedStrategies.reducedMealsPerDay =
                                reducedNoMealsPerDay.text.toString().toDouble()
                            consumptionBasedStrategies.reducedMealPortionSize =
                                reducedMealPortionSize.text.toString().toDouble()

                            val livelihoodBasedStrategies = LivelihoodBasedStrategies()
                            livelihoodBasedStrategies.soldHouseHoldAssets =
                                soldHouseHoldAssets.text.toString().toInt()
                            livelihoodBasedStrategies.reducedNonFoodExpense =
                                reducedNonFoodExpenses.text.toString().toInt()
                            livelihoodBasedStrategies.soldProductiveAssets =
                                soldProductiveAssets.text.toString().toInt()
                            livelihoodBasedStrategies.spentSavings =
                                spentSavings.text.toString().toInt()
                            livelihoodBasedStrategies.borrowedMoneyFromLender =
                                borrowedMoneyFromLender.text.toString().toInt()
                            livelihoodBasedStrategies.soldHouseOrLand =
                                soldHouseOrLand.text.toString().toInt()
                            livelihoodBasedStrategies.withdrewSchoolChildren =
                                withdrewSchoolChildren.text.toString().toInt()
                            livelihoodBasedStrategies.soldFemaleAnimals =
                                soldFemaleAnimals.text.toString().toInt()
                            livelihoodBasedStrategies.begging = begging.text.toString().toInt()
                            livelihoodBasedStrategies.soldMoreAnimals =
                                soldMoreAnimals.text.toString().toInt()

                            copingStrategiesResponses.consumptionBasedStrategies =
                                consumptionBasedStrategies
                            copingStrategiesResponses.livelihoodBasedStrategies =
                                livelihoodBasedStrategies
                            wealthGroupQuestionnaire.copingStrategiesResponses =
                                copingStrategiesResponses

                            wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.FGD_PARTICIPANTS_STEP

                            fdgParticipants.root.visibility = View.VISIBLE
                            wgCopingStrategies.root.visibility = View.GONE
                        }
                    }

                }

            }


            fdgParticipants.apply {

                fgdConfigurationSubmitButton.setOnClickListener {

                    if (noFdgParticipants.text.toString().isNotEmpty()) {

                        for (i in 0..noFdgParticipants.text.toString().toInt() - 1) {
                            fdgParticipantsModelList.add(
                                FgdParticipantModel(
                                    "",
                                    0.0,
                                    0,
                                    0,
                                    0,
                                    0
                                )
                            )
                        }

                        val fgdParticipantAdapter = activity?.let { it1 ->
                            FgdParticipantsAdapter(
                                fdgParticipantsModelList, this@WealthGroupDialogFragment,
                                it1
                            )
                        }
                        val gridLayoutManager = GridLayoutManager(activity, 1)
                        participantsList.layoutManager = gridLayoutManager
                        participantsList.hasFixedSize()
                        participantsList.adapter = fgdParticipantAdapter

                        numberFgdParticipantsConfiguration.visibility = View.GONE
                        participantsListWrapper.visibility = View.VISIBLE

                    }

                }

                fdgParticipantsBackButton.setOnClickListener {
                    fdgParticipants.root.visibility = View.GONE
                    wgCopingStrategies.root.visibility = View.VISIBLE
                }

                fdgParticipantsNextButton.setOnClickListener {

                    wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.WG_COMPLETION_PAGE
                    fdgParticipants.root.visibility = View.GONE
                    wgCompletionPage.root.visibility = View.VISIBLE
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
                    val existingQuestionnaires = questionnairesListObject.questionnaireList.filter {
                        it.uniqueId == wealthGroupQuestionnaire.uniqueId
                    }

                    if (existingQuestionnaires.isEmpty()) {
                        questionnairesListObject.addQuestionnaire(wealthGroupQuestionnaire)
                    } else {
                        questionnairesListObject.updateQuestionnaire(questionnairesListObject.questionnaireList.indexOf(existingQuestionnaires.get(0)), wealthGroupQuestionnaire)
                    }
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

    fun consumptionStrategiesResponseMoreThanSevenDays(responseString: String): Boolean {
        return responseString.toDouble() > 7.0
    }

    fun livelihoodStrategiesResponseNotWithinRange(responseString: String): Boolean {
        return responseString.toInt() != 1 && responseString.toInt() != 2 && responseString.toInt() != 3 && responseString.toInt() != 4
    }

    private fun returnZeroStringIfEmpty(inputString: String): String {
        if (inputString.isNullOrEmpty()) {
            return "0"
        }
        return inputString
    }

    private fun returnZeroDoubleIfEmpty(inputString: String): Double {
        if (inputString.isNullOrEmpty()) {
            return 0.0
        }
        return inputString.toDouble()
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
        return !currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.shortRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
                || !currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.hasBeenSubmitted || !currentResponseItem.longRainsSeason.rainfedAverageYieldPerHa.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedCultivatedArea.hasBeenSubmitted || !currentResponseItem.longRainsSeason.irrigatedAverageYieldPerHa.hasBeenSubmitted
    }


    fun isAnyCropContributionValueEmpty(): Boolean {
        for (currentResponseItem in cropContributionResponseItems) {
            if (isAnyCropContributionItemEmpty(currentResponseItem)) {
                return true
            }
        }
        return false
    }

    fun isAnyCropContributionItemEmpty(currentResponseItem: WgCropContributionResponseItem): Boolean {
        return !currentResponseItem.cashIncomeRank.hasBeenSubmitted || !currentResponseItem.cashIncomeApproxPercentage.hasBeenSubmitted
                || !currentResponseItem.foodConsumptionRank.hasBeenSubmitted || !currentResponseItem.foodConsumptionApproxPercentage.hasBeenSubmitted
    }

    fun doesCropCashContributionIncomeHaveAPercentageError(): CropContributionValidationResponse {
        var totalValue = 0.0
        for (currentResponseItem in cropContributionResponseItems) {
            totalValue = totalValue + currentResponseItem.cashIncomeApproxPercentage.actualValue
        }
        return CropContributionValidationResponse(
            totalValue != 100.0,
            "Crop cash contribution is ${if (totalValue > 100) "greater than 100%" else "less than 100%"} by ${abs(
                100 - totalValue
            )}"
        )
    }

    fun doesCropFoodConsumptionContributionIncomeHaveAPercentageError(): CropContributionValidationResponse {
        var totalValue = 0.0
        for (currentResponseItem in cropContributionResponseItems) {
            totalValue =
                totalValue + currentResponseItem.foodConsumptionApproxPercentage.actualValue
        }
        return CropContributionValidationResponse(
            totalValue != 100.0,
            "Crop food consumption contribution is ${if (totalValue > 100) "greater than 100%" else "less than 100%"} by ${abs(
                100 - totalValue
            )}"
        )
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


    private fun inflateLivestockContributionRankModal(
        contributionRanks: MutableList<RankResponseItem>,
        livestockContributionRankTypeEnum: LivestockContributionRankTypeEnum,
        animalType: WgLivestockTypesEnum
    ) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)
        val list: RecyclerView = v.findViewById(R.id.listRv)

        val ranksAdapter =
            LivestockContributionRankAdapter(
                contributionRanks,
                this,
                livestockContributionRankTypeEnum,
                animalType
            )
        val gridLayoutManager = GridLayoutManager(context, 1)
        list.layoutManager = gridLayoutManager
        list.hasFixedSize()
        list.adapter = ranksAdapter

        openLivestockContributionRankModal(v)
    }

    private fun openLivestockContributionRankModal(v: View) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setView(v)
        builder.setCancelable(true)
        livestockContributionRankModal = builder.create()
        (livestockContributionRankModal as AlertDialog).setCancelable(true)
        (livestockContributionRankModal as AlertDialog).setCanceledOnTouchOutside(true)
        (livestockContributionRankModal as AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (livestockContributionRankModal as AlertDialog).show()
        val window = (livestockContributionRankModal as AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onCropItemSelectedFromSelectionList(selectedCrop: CropModel, position: Int) {
        crops.set(position, selectedCrop)
        if (selectedCrop.hasBeenSelected) {
            wealthGroupQuestionnaire.selectedCrops.add(selectedCrop)
        } else {
            wealthGroupQuestionnaire.selectedCrops.remove(selectedCrop)
        }
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
                    cropsList.setSelection(position)
                }
            }
        }
    }

    override fun onCropProductionResponseItemSubmited(
        responseItem: WgCropProductionResponseItem,
        position: Int
    ) {
        cropProductionResponseItems.set(position, responseItem)
    }

    override fun onAnyFieldEdited(
        currentResponseItem: WgCropContributionResponseItem,
        position: Int,
        cropContributionEditTypeEnum: CropContributionEditTypeEnum,
        selectedCashIncomeContributionRank: RankResponseItem?,
        selectedFoodConsumptionContributionRank: RankResponseItem?
    ) {

        if (cropContributionEditTypeEnum == CropContributionEditTypeEnum.CROP_CASH_INCOME_CONTRIBUTION_RANK) {
            cropCashIncomeContributionRanks.remove(selectedCashIncomeContributionRank)
        }

        if (cropContributionEditTypeEnum == CropContributionEditTypeEnum.CROP_FOOD_CONSUMPTION_CONTRIBUTION_RANK) {
            cropFoodConsumptionContributionRanks.remove(selectedFoodConsumptionContributionRank)
        }

        cropContributionResponseItems.set(position, currentResponseItem)

        binding.apply {
            cropProductionLayout.apply {

                val cropContributionAdapter =
                    activity?.let { it1 ->
                        WgCropContributionAdapter(
                            cropContributionResponseItems,
                            this@WealthGroupDialogFragment,
                            it1,
                            cropCashIncomeContributionRanks,
                            cropFoodConsumptionContributionRanks
                        )
                    }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                cropResponseList.layoutManager = gridLayoutManager
                cropResponseList.hasFixedSize()
                cropResponseList.adapter =
                    cropContributionAdapter
            }
        }
    }

    override fun onALivestockContributionRankItemSelected(
        selectedRankItem: RankResponseItem,
        position: Int,
        livestockContributionRankTypeEnum: LivestockContributionRankTypeEnum,
        animalType: WgLivestockTypesEnum
    ) {

        if (animalType == WgLivestockTypesEnum.CATTLE) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.cattle.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.cattle.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.cattleIncomeRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.cattle.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.cattle.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.cattleFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.DAIRY_CATTLE) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.dairyCattle.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.dairyCattle.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.dairyCattleIncomeRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.dairyCattle.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.dairyCattle.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.dairyCattleFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }


        if (animalType == WgLivestockTypesEnum.GOATS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.goats.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.goats.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.goatsIncomeRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.goats.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.goats.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.goatsFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.SHEEP) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.sheep.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.sheep.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.sheepCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.sheep.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.sheep.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.sheepFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.DONKEYS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.donkeys.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.donkeys.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.donkeysCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.donkeys.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.donkeys.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.donkeysFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.CAMELS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.camels.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.camels.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.camelsCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.camels.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.camels.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.camelsFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.PIGS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.pigs.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.pigs.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.pigscashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.pigs.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.pigs.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.pigsFoodrank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.CHICKEN) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.chicken.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.chicken.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.chickenCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.chicken.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.chicken.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.chickenFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.IMPROVED_CHICKEN) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.improvedChicken.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.improvedChicken.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.improvedChickenCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.improvedChicken.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.improvedChicken.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.improvedChickenFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.DUCKS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.ducks.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.ducks.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.ducksCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.ducks.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.ducks.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.ducksFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.BEE_HIVES) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.beeHives.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.beeHives.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.beeHivesCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.beeHives.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.beeHives.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.beeHivesFoodrank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.FISH_POND) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.fishPonds.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.fishPonds.incomeRank.hasBeenSubmitted = true
                livestockCashIncomeContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.fishPondsCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.fishPonds.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.fishPonds.consumptionRank.hasBeenSubmitted = true
                livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                binding.wgLivestockPoultryContribution.fishPondsFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }
        (livestockContributionRankModal as AlertDialog).dismiss()
    }

    fun doesLivestockCashContributionColumnHaveAPercentageError(): ValidationResponseObject {
        binding.apply {

            wgLivestockPoultryContribution.apply {
                val totalValue =
                    returnZeroDoubleIfEmpty(cattleCashPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        goatsIncomePercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(sheepCashPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        donkeysCashPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(pigsCashPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        chickenCashPaercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(camelsCashPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        duckscashPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(beeHivesCashPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        fishPondscashPercentage.text.toString()
                    )

                return ValidationResponseObject(
                    totalValue != 100.0,
                    "Total inputted percentage for livestock cash contribution column is ${if (totalValue > 100) "greater than" else "less than"} 100% by ${abs(
                        100 - totalValue
                    )}"
                )
            }

        }
    }

    fun doesLivestockFoodConsumptionContributionColumnHaveAPercentageError(): ValidationResponseObject {
        binding.apply {

            wgLivestockPoultryContribution.apply {
                val totalValue =
                    returnZeroDoubleIfEmpty(
                        cattleFoodPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(goatsFoodPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        sheepFoodPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(donkeysFoodPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        pigsFoodPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(chickenFoodPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        camelsFoodPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(ducksFoodPercentage.text.toString()) + returnZeroDoubleIfEmpty(
                        beeHivesFoodPercentage.text.toString()
                    ) + returnZeroDoubleIfEmpty(fishPondsFoodPercentage.text.toString())

                return ValidationResponseObject(
                    totalValue != 100.0,
                    "Total inputted percentage for livestock food consumption contribution is ${if (totalValue > 100) "greater than" else "less than"} 100% by ${abs(
                        100 - totalValue
                    )}"
                )
            }

        }
    }


    private fun inflateConstraintsRankModal(
        ranks: MutableList<RankResponseItem>,
        constraintsTypeEnum: ConstraintsTypeEnum,
        constraintCategoryEnum: ConstraintCategoryEnum
    ) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)
        val list: RecyclerView = v.findViewById(R.id.listRv)

        val ranksAdapter =
            ConstraintsRankingAdapter(
                ranks,
                this,
                constraintsTypeEnum,
                constraintCategoryEnum
            )
        val gridLayoutManager = GridLayoutManager(context, 1)
        list.layoutManager = gridLayoutManager
        list.hasFixedSize()
        list.adapter = ranksAdapter

        openConstraintsRankModal(v)
    }

    private fun openConstraintsRankModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setView(v)
        builder.setCancelable(true)
        constraintsRankDialog = builder.create()
        (constraintsRankDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (constraintsRankDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (constraintsRankDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (constraintsRankDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (constraintsRankDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAConstraintsRankItemSelected(
        selectedRankItem: RankResponseItem,
        position: Int,
        constraintsTypeEnum: ConstraintsTypeEnum,
        constraintCategoryEnum: ConstraintCategoryEnum
    ) {

        binding.apply {
            wgConstraints.apply {

                if (constraintCategoryEnum == ConstraintCategoryEnum.SOURCE_OF_INCOME) {

                    incomeSourceRanks.remove(selectedRankItem)

                    if (constraintsTypeEnum == ConstraintsTypeEnum.IS_LOW_EDUCATION) {
                        wagedLabourIncomeConstraintsResponses.lowEducation =
                            selectedRankItem.rankPosition
                        labourLowEducation.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IS_POOR_HEALTH) {
                        wagedLabourIncomeConstraintsResponses.poorHealth =
                            selectedRankItem.rankPosition
                        labourPoorHealth.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IS_FEW_JOBS) {
                        wagedLabourIncomeConstraintsResponses.fewJobs =
                            selectedRankItem.rankPosition
                        labourFewJobs.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IS_TIME_ON_FARM) {
                        wagedLabourIncomeConstraintsResponses.tooMuchFarmTime =
                            selectedRankItem.rankPosition
                        labourFarmTime.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IS_LOW_WAGE_RATES) {
                        wagedLabourIncomeConstraintsResponses.lowAverageWageRates =
                            selectedRankItem.rankPosition
                        labourLowWageRates.text = selectedRankItem.rankPosition.toString()
                    }
                }


                if (constraintCategoryEnum == ConstraintCategoryEnum.INCOME_CONSUMPTION) {

                    incomeConsumptionRanks.remove(selectedRankItem)

                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_SMALL_LAND) {
                        cropProductionIncomeConstraintsResponses.smallLandHoldings =
                            selectedRankItem.rankPosition
                        consumptionHoldings.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_LACK_OF_CREDIT) {
                        cropProductionIncomeConstraintsResponses.lackOfCredit =
                            selectedRankItem.rankPosition
                        consumptionLackOfCredit.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_HIGH_INPUT_COSTS) {
                        cropProductionIncomeConstraintsResponses.highInputCost =
                            selectedRankItem.rankPosition
                        consumptionHighInputs.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_LOW_LAND_FERTILITY) {
                        cropProductionIncomeConstraintsResponses.lowLandFertility =
                            selectedRankItem.rankPosition
                        consumptionLowFertility.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_UNRELIABLE_WATER) {
                        cropProductionIncomeConstraintsResponses.lackOfReliableWater =
                            selectedRankItem.rankPosition
                        consumptionUnreliableWater.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_LOW_TECHNICAL_SKILLS) {
                        cropProductionIncomeConstraintsResponses.lowTechnicalSkills =
                            selectedRankItem.rankPosition
                        consumptionLowTechnicalSkills.text =
                            selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_LOW_QUALITY_SEED) {
                        cropProductionIncomeConstraintsResponses.lowQualitySeed =
                            selectedRankItem.rankPosition
                        consumptionLowSeedQuality.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_MARKET_ACCESS) {
                        cropProductionIncomeConstraintsResponses.lackOfMarketAccess =
                            selectedRankItem.rankPosition
                        consumptionMarketAccess.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.IC_CROP_PESTS_DISEASES) {
                        cropProductionIncomeConstraintsResponses.endemicCropPests =
                            selectedRankItem.rankPosition
                        consumptionCropPests.text = selectedRankItem.rankPosition.toString()
                    }
                }

                if (constraintCategoryEnum == ConstraintCategoryEnum.LIVESTOCK_PRODUCTION) {

                    livestockProductionRanks.remove(selectedRankItem)

                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_LACK_OF_PASTURE) {
                        livestockProductionIncomeConstraintsResponses.lackOfPasture =
                            selectedRankItem.rankPosition
                        livestockProductionPasture.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_LACK_ANIMAL_DRINKING_WATER) {
                        livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater =
                            selectedRankItem.rankPosition
                        livestockProductionDrinkingWater.text =
                            selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_LOW_YIELDING_ANIMALS) {
                        livestockProductionIncomeConstraintsResponses.lowYieldingAnimal =
                            selectedRankItem.rankPosition
                        livestockProductionLowYieldingAnimal.text =
                            selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_COSTLY_VETERINARY_DRUGS) {
                        livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs =
                            selectedRankItem.rankPosition
                        livestockProductionVeterinaryDrugs.text =
                            selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_LIVESTOCK_PESTS_DISEASES) {
                        livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases =
                            selectedRankItem.rankPosition
                        livestockProductionPests.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_LACK_OF_MARKET) {
                        livestockProductionIncomeConstraintsResponses.lackofMarket =
                            selectedRankItem.rankPosition
                        livestockProductionMarket.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.LP_INSECURITY) {
                        livestockProductionIncomeConstraintsResponses.insecurity =
                            selectedRankItem.rankPosition
                        livestockProductionInsecurity.text =
                            selectedRankItem.rankPosition.toString()
                    }

                }


                if (constraintCategoryEnum == ConstraintCategoryEnum.FISHING) {

                    fishingRanks.remove(selectedRankItem)

                    if (constraintsTypeEnum == ConstraintsTypeEnum.F_LOW_FISH_STOCKS) {
                        fishingIncomeConstraintsResponses.lowFishStocks =
                            selectedRankItem.rankPosition
                        fishingLowStocks.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.F_POOR_MARKET) {
                        fishingIncomeConstraintsResponses.poorMarket = selectedRankItem.rankPosition
                        fishingPoorMarket.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.F_EQUIPMENT) {
                        fishingIncomeConstraintsResponses.lackOfEquipment =
                            selectedRankItem.rankPosition
                        fishingLackOfEquipment.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.F_COMPETITION) {
                        fishingIncomeConstraintsResponses.extremeCompetition =
                            selectedRankItem.rankPosition
                        fishingCompetition.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.F_LACK_OF_EXPERTISE) {
                        fishingIncomeConstraintsResponses.lackOfExpertise =
                            selectedRankItem.rankPosition
                        fishingLackOfExpertise.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.F_FISHING_RIGHTS) {
                        fishingIncomeConstraintsResponses.fishingRightsRestrictions =
                            selectedRankItem.rankPosition
                        fishingFishingRights.text = selectedRankItem.rankPosition.toString()
                    }
                }


                if (constraintCategoryEnum == ConstraintCategoryEnum.NATURAL_RESOURCE) {

                    naturalResourceRanks.remove(selectedRankItem)

                    if (constraintsTypeEnum == ConstraintsTypeEnum.NR_DECLINING_RESOURCE) {
                        naturalResourceIncomeConstraintsResponses.decliningNaturalResources =
                            selectedRankItem.rankPosition
                        resourceDecline.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.NR_POPULATION_PRESSURE) {
                        naturalResourceIncomeConstraintsResponses.populationPressure =
                            selectedRankItem.rankPosition
                        resourcePopulationPressure.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.NR_RIGHTS_RESTRICTIONS) {
                        naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights =
                            selectedRankItem.rankPosition
                        resourceRights.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.NR_LOW_VALUE) {
                        naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts =
                            selectedRankItem.rankPosition
                        resourceLowValue.text = selectedRankItem.rankPosition.toString()
                    }
                }


                if (constraintCategoryEnum == ConstraintCategoryEnum.SMALL_ENTERPRISE) {

                    smallEnterpriesRanks.remove(selectedRankItem)

                    if (constraintsTypeEnum == ConstraintsTypeEnum.SE_LACK_OF_CAPITAL) {
                        smallEnterpriseIncomeConstraintsResponses.lackOfCapital =
                            selectedRankItem.rankPosition
                        enterpriseLackOfCapital.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.SE_RED_TAPE) {
                        smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape =
                            selectedRankItem.rankPosition
                        enterpriseRedTape.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.SE_TAXES) {
                        smallEnterpriseIncomeConstraintsResponses.tooManyTaxes =
                            selectedRankItem.rankPosition
                        enterpriseTaxes.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.SE_MARKET_ACCESS) {
                        smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket =
                            selectedRankItem.rankPosition
                        enterpriseMarketAccess.text = selectedRankItem.rankPosition.toString()
                    }
                    if (constraintsTypeEnum == ConstraintsTypeEnum.SE_LACK_OF_EXPERTISE) {
                        smallEnterpriseIncomeConstraintsResponses.lackOfExpertise =
                            selectedRankItem.rankPosition
                        enterpriseExpertise.text = selectedRankItem.rankPosition.toString()
                    }
                }

            }
        }

        (constraintsRankDialog as androidx.appcompat.app.AlertDialog).dismiss()
    }

    override fun onAParticipantUpdated(updatedParticipant: FgdParticipantModel, position: Int) {
        fdgParticipantsModelList.set(position, updatedParticipant)
//        binding.apply {
//
//            fdgParticipants.apply {
//
//                val fgdParticipantAdapter = activity?.let { it1 ->
//                    FgdParticipantsAdapter(
//                        fdgParticipantsModelList, this@WealthGroupDialogFragment,
//                        it1
//                    )
//                }
//                val gridLayoutManager = GridLayoutManager(activity, 1)
//                participantsList.layoutManager = gridLayoutManager
//                participantsList.hasFixedSize()
//                participantsList.adapter = fgdParticipantAdapter
//
//            }
//
//        }
    }

    fun retrieveASpecificWealthGroupQuestionnaire(questionnaireId: String): WealthGroupQuestionnaire {
        val gson = Gson()
        val sharedPreferences: SharedPreferences? =
            context?.applicationContext?.getSharedPreferences(
                "MyPref",
                Context.MODE_PRIVATE
            )


        val questionnairesListString =
            sharedPreferences?.getString(Constants.WEALTH_GROUP_LIST_OBJECT, null)
        val questionnairesListObject: WealthGroupQuestionnaireListObject =
            gson.fromJson(
                questionnairesListString,
                WealthGroupQuestionnaireListObject::class.java
            )

        val existingQuestionnaires = questionnairesListObject.questionnaireList.filter {
            it.uniqueId == questionnaireId
        }

        return existingQuestionnaires.get(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        saveQuestionnaireAsDraft()
    }

    override fun onStop() {
        super.onStop()
        saveQuestionnaireAsDraft()
    }

    fun saveQuestionnaireAsDraft() {
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

        val existingQuestionnaires = questionnairesListObject.questionnaireList.filter {
            it.uniqueId == wealthGroupQuestionnaire.uniqueId
        }

        if (existingQuestionnaires.isEmpty()) {
            questionnairesListObject.addQuestionnaire(wealthGroupQuestionnaire)
        } else {
            questionnairesListObject.updateQuestionnaire(questionnairesListObject.questionnaireList.indexOf(existingQuestionnaires.get(0)), wealthGroupQuestionnaire)
        }
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
    }


}