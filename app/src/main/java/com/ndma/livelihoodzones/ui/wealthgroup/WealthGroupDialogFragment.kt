package com.ndma.livelihoodzones.ui.wealthgroup


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
import com.ndma.livelihoodzones.login.model.LoginResponseModel
import com.ndma.livelihoodzones.ui.county.model.CropModel
import com.ndma.livelihoodzones.ui.county.model.QuestionnaireSessionLocation
import com.ndma.livelihoodzones.ui.home.HomeViewModel
import com.ndma.livelihoodzones.ui.model.*
import com.ndma.livelihoodzones.ui.wealthgroup.adapters.*
import com.ndma.livelihoodzones.ui.wealthgroup.model.ConstraintCategoryEnum
import com.ndma.livelihoodzones.ui.wealthgroup.model.ConstraintsTypeEnum
import com.ndma.livelihoodzones.ui.wealthgroup.model.FgdParticipantModel
import com.ndma.livelihoodzones.ui.wealthgroup.model.FoodConsumptionEnum
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

    private var incomeFoodSourcesOthersSpecifyDialog: android.app.AlertDialog? = null

    private lateinit var binding: WealthGroupQuestionnaireLayoutBinding

    private lateinit var wealthGroupQuestionnaire: WealthGroupQuestionnaire

    var incomeAndFoodSourceResponses = IncomeAndFoodSourceResponses()

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    var storedSessionDetails: LoginResponseModel? = null

    var isAResumeQuestionnaire: Boolean = false

    lateinit var geographyObject: GeographyObject

    var questionnaireSessionLocation: QuestionnaireSessionLocation? = null

    var livestockContributionResponses = LivestockContributionResponses()

    private var subContyDialog: AlertDialog? = null

    private var livestockContributionRankModal: AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    private var foodConsumptionNotApplicableDialog: android.app.AlertDialog? = null

    private var constraintsRankDialog: androidx.appcompat.app.AlertDialog? = null

    var foodConsumptionHasNoPercentageError: Boolean = true

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

    var constraintResponses = ConstraintsResponses()

    var wagedLabourIncomeConstraintsResponses =
        WagedLabourIncomeConstraintsResponses()

    var cropProductionIncomeConstraintsResponses =
        CropProductionIncomeConstraintsResponses()

    var livestockProductionIncomeConstraintsResponses =
        LivestockProductionIncomeConstraintsResponses()

    var fishingIncomeConstraintsResponses = FishingIncomeConstraintsResponses()

    var naturalResourceIncomeConstraintsResponses =
        NaturalResourceIncomeConstraintsResponses()

    var smallEnterpriseIncomeConstraintsResponses =
        SmallEnterpriseIncomeConstraintsResponses()

    var fdgParticipantsModelList: MutableList<FgdParticipantModel> = ArrayList()

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
        val storedSessionDetailsString =
            sharedPreferences?.getString(Constants.SESSION_DETAILS, null)
        storedSessionDetails = gson.fromJson(
            storedSessionDetailsString,
            LoginResponseModel::class.java
        )

        storedSessionDetails?.let {
            AppStore.getInstance().sessionDetails = it
        }

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

                wealthGroupQuestionnaire.questionnaireGeography =
                    this.questionnaireSessionLocation!!
                wealthGroupQuestionnaire.questionnaireStartDate = Util.getNow()
                wealthGroupQuestionnaire.questionnaireName =
                    geographyObject.county.countyName + " " +
                            wealthGroupQuestionnaire.questionnaireGeography.selectedLivelihoodZone?.livelihoodZoneName + " Livelihood Zone " + wealthGroupQuestionnaire.questionnaireGeography.selectedWealthGroup?.wealthGroupName + " wealth group " + questionnaireSessionLocation!!.selectedWgQuestionnaireType?.wgQuestionnaireTypeDescription
            } else {
                wealthGroupQuestionnaire = questionnaireId?.let { it1 ->
                    retrieveASpecificWealthGroupQuestionnaire(
                        it1
                    )
                }!!
            }
        }
        updateCurrentQuestionnaireToStore()
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
        binding.apply {
            toolBar.apply {
                topLeftBackIcon.setOnClickListener {
                    if (wealthGroupQuestionnaire.lastQuestionnaireStep == Constants.WG_COMPLETION_PAGE) {
                        populateFgdParticipants()
                        wgCompletionPage.root.visibility = View.GONE
                        fdgParticipants.root.visibility = View.VISIBLE
                    }
                }
            }
        }
        if (isAResumeQuestionnaire) {
            binding.wgIncomeAndFoodSources.root.visibility = View.GONE
            determineTheResumeStep()
        }
    }

    fun determineTheResumeStep() {
        when (determineTheFurthestCoveredStep(wealthGroupQuestionnaire.questionnaireCoveredSteps)) {
            Constants.MAIN_INCOME_AND_FOOD_SOURCE_STEP -> {
                resumeMainIncomeandFoodSources()
            }
            Constants.FOOD_CONSUMPTION_SOURCE_PERCENTAGE_STEP -> {
                resumeFoodConsumptionSourcePercentages()
            }
            Constants.WG_CROP_SELECTION_STEP -> {
                resumeCropSelection()
            }
            Constants.WG_CROP_PRODUCTION_STEP -> {
                resumeCropProduction()
            }
            Constants.LIVESTOCK_POULTRY_NUMBERS_STEP -> {
                resumeLivestockPoultryNumbers()
            }
            Constants.LIVESTOCK_POULTRY_CONTRIBUTION_STEP -> {
                resumeLivestockPoultryContributions()
            }
            Constants.LABOUR_PATTERNS_STEP -> {
                resumeLabourPatterns()
            }
            Constants.EXPENDITURE_PATTERNS_STEP -> {
                resumeExpenditurePatterns()
            }
            Constants.MIGRATION_PATTERNS_STEP -> {
                resumeExpenditurePatterns()
            }
            Constants.CONSTRAINTS_STEP -> {
                resumeConstraints()
            }
            Constants.COPING_STRATEGIES_STEP -> {
                resumeCopingStrategies()
            }
            Constants.FGD_PARTICIPANTS_STEP -> {
                resumeFgdParticipants()
            }
            Constants.WG_COMPLETION_PAGE -> {
                wealthGroupQuestionnaire.lastQuestionnaireStep =
                    Constants.WG_COMPLETION_PAGE

                if (!doesStepExist(
                        Constants.WG_COMPLETION_PAGE,
                        wealthGroupQuestionnaire.questionnaireCoveredSteps
                    )
                ) {
                    wealthGroupQuestionnaire.questionnaireCoveredSteps.add(
                        Constants.WG_COMPLETION_PAGE
                    )
                }
                resumeCompletionPage()
            }
        }
    }

    fun resumeMainIncomeandFoodSources() {
        binding.apply {
            wgIncomeAndFoodSources.root.visibility = View.VISIBLE
        }
    }

    fun resumeFoodConsumptionSourcePercentages() {
        binding.apply {
            wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE
        }
    }

    fun resumeCropSelection() {
        binding.apply {
            prepareCropSelectionListView()
            cropSelectionLayout.root.visibility = View.VISIBLE
        }
    }

    fun resumeCropProduction() {
        binding.apply {
            prepareCropProduction()
            cropProductionLayout.root.visibility = View.VISIBLE
        }
    }

    fun resumeLivestockPoultryNumbers() {
        binding.apply {
            wgLivestockPoultryNumbers.root.visibility = View.VISIBLE
        }
    }

    fun resumeLivestockPoultryContributions() {
        binding.apply {
            wgLivestockPoultryContribution.root.visibility = View.VISIBLE
        }
    }

    fun resumeLabourPatterns() {
        binding.apply {
            wgLabourPatterns.root.visibility = View.VISIBLE
        }
    }

    fun resumeExpenditurePatterns() {
        binding.apply {
            wgExpenditurePatterns.root.visibility = View.VISIBLE
        }
    }

    fun resumeMigrationPatterns() {
        binding.apply {
            wgMigrationPatterns.root.visibility = View.VISIBLE
        }
    }

    fun resumeConstraints() {
        binding.apply {
            wgConstraints.root.visibility = View.VISIBLE
        }
    }

    fun resumeCopingStrategies() {
        binding.apply {
            wgCopingStrategies.root.visibility = View.VISIBLE
        }
    }

    fun resumeFgdParticipants() {
        binding.apply {
            fdgParticipants.root.visibility = View.VISIBLE
        }
    }

    fun resumeCompletionPage() {
        binding.apply {
            wgCompletionPage.root.visibility = View.VISIBLE
        }
    }

    fun doesStepExist(step: Int, existingSteps: MutableList<Int>): Boolean {
        for (item in existingSteps) {
            if (item == step) {
                return true
            }
        }
        return false
    }

    fun determineTheFurthestCoveredStep(steps: MutableList<Int>): Int {
        if (steps.isNotEmpty()) {
            steps.sort()
            return steps.last()
        }
        return 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineNavigation() {
        if (!isAResumeQuestionnaire) {
            wealthGroupQuestionnaire.lastQuestionnaireStep =
                Constants.MAIN_INCOME_AND_FOOD_SOURCE_STEP
            if (!doesStepExist(
                    Constants.MAIN_INCOME_AND_FOOD_SOURCE_STEP,
                    wealthGroupQuestionnaire.questionnaireCoveredSteps
                )
            ) {
                wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.MAIN_INCOME_AND_FOOD_SOURCE_STEP)
            }
        }

        binding.apply {

            /*Income and food sources navigation*/
            wgIncomeAndFoodSources.apply {
                val fontAwesome: Typeface =
                    Typeface.createFromAsset(activity?.applicationContext?.getAssets(), "fontawesome-webfont.ttf")
                otherEdit.setTypeface(fontAwesome)
                otherEdit.setOnClickListener {
                    inflateIncomeFoodSourcesOthersSpecifyModal()
                }
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

                        incomeAndFoodSourceResponses.livestockProduction =
                            livestockProduction.text.toString().toDouble()

                        incomeAndFoodSourceResponses.pastureFodderProduction =
                            pastureFodderProduction.text.toString().toDouble()

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

                        incomeAndFoodSourceResponses.other.value =
                            other.text.toString().toDouble()

                        wealthGroupQuestionnaire.incomeAndFoodSourceResponses =
                            incomeAndFoodSourceResponses

                        wealthGroupQuestionnaire.lastQuestionnaireStep =
                            Constants.FOOD_CONSUMPTION_SOURCE_PERCENTAGE_STEP

                        if (!doesStepExist(
                                Constants.FOOD_CONSUMPTION_SOURCE_PERCENTAGE_STEP,
                                wealthGroupQuestionnaire.questionnaireCoveredSteps
                            )
                        ) {
                            wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.FOOD_CONSUMPTION_SOURCE_PERCENTAGE_STEP)
                        }

                        updateCurrentQuestionnaireToStore()

                        wgIncomeAndFoodSources.root.visibility = View.GONE
                        wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE

                    }
                }
            }


            wgPercentFoodConsumptionIncome.apply {

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
                                maizeTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {

                                    if (maizeOwnFarm.editableText.toString()
                                            .isNotEmpty() && maizeMarket.editableText.toString()
                                            .isNotEmpty() && maizeGift.editableText.toString()
                                            .isNotEmpty() && maizeOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && maizeMarket.editableText.toString()
                                            .toDouble() == 0.0 && maizeGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.MAIZE_AND_POSHO)

                                    } else {
                                        maizeOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        maizeMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        maizeGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    maizeOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    maizeMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    maizeGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                wheatTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (wheatOwnFarm.editableText.toString()
                                            .isNotEmpty() && wheatMarket.editableText.toString()
                                            .isNotEmpty() && wheatGift.editableText.toString()
                                            .isNotEmpty() && wheatOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && wheatMarket.editableText.toString()
                                            .toDouble() == 0.0 && wheatGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.WHEAT_BARLEY)

                                    } else {
                                        wheatOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        wheatMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        wheatGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    wheatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    wheatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    wheatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                sorghumTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (sorghumOwnFarm.editableText.toString()
                                            .isNotEmpty() && sorghumMarket.editableText.toString()
                                            .isNotEmpty() && sorghumGift.editableText.toString()
                                            .isNotEmpty() && sorghumOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && sorghumMarket.editableText.toString()
                                            .toDouble() == 0.0 && sorghumGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.SORGHUM)

                                    } else {
                                        sorghumOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        sorghumMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        sorghumGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    sorghumOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    sorghumMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    sorghumGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                riceTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (riceOwnFarm.editableText.toString()
                                            .isNotEmpty() && riceMarket.editableText.toString()
                                            .isNotEmpty() && riceGift.editableText.toString()
                                            .isNotEmpty() && riceOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && riceMarket.editableText.toString()
                                            .toDouble() == 0.0 && riceGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.RICE_AND_PRODUCTS)

                                    } else {
                                        riceOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        riceMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        riceGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    riceOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    riceMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    riceGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                beansTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (beansOwnfarm.editableText.toString()
                                            .isNotEmpty() && beansMarket.editableText.toString()
                                            .isNotEmpty() && beansGift.editableText.toString()
                                            .isNotEmpty() && beansOwnfarm.editableText.toString()
                                            .toDouble() == 0.0 && beansMarket.editableText.toString()
                                            .toDouble() == 0.0 && beansGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.BEANS)

                                    } else {
                                        beansOwnfarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        beansMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        beansGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    beansOwnfarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    beansMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    beansGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                pulsesTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (pulsesOwnFarm.editableText.toString()
                                            .isNotEmpty() && pulsesMarket.editableText.toString()
                                            .isNotEmpty() && pulsesGift.editableText.toString()
                                            .isNotEmpty() && pulsesOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && pulsesMarket.editableText.toString()
                                            .toDouble() == 0.0 && pulsesGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.OTHER_PULSES)

                                    } else {
                                        pulsesOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        pulsesMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        pulsesGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    pulsesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    pulsesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    pulsesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                vegetablesTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (vegetablesOwnFarm.editableText.toString()
                                            .isNotEmpty() && vegetablesMarket.editableText.toString()
                                            .isNotEmpty() && vegetablesGift.editableText.toString()
                                            .isNotEmpty() && vegetablesOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && vegetablesMarket.editableText.toString()
                                            .toDouble() == 0.0 && vegetablesGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.VEGETABLES)

                                    } else {
                                        vegetablesOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        vegetablesMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        vegetablesGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    vegetablesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    vegetablesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    vegetablesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                fruitsTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (fruitsOwnFarm.editableText.toString()
                                            .isNotEmpty() && fruitsMarket.editableText.toString()
                                            .isNotEmpty() && fruitsGift.editableText.toString()
                                            .isNotEmpty() && fruitsOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && fruitsMarket.editableText.toString()
                                            .toDouble() == 0.0 && fruitsGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.FRUITS_AND_BERRIES)

                                    } else {
                                        fruitsOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        fruitsMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        fruitsGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    fruitsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fruitsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fruitsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                whiteRootsTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (whiteRootsOwnFarm.editableText.toString()
                                            .isNotEmpty() && whiteRootsMarket.editableText.toString()
                                            .isNotEmpty() && whiteRootsGift.editableText.toString()
                                            .isNotEmpty() && whiteRootsOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && whiteRootsMarket.editableText.toString()
                                            .toDouble() == 0.0 && whiteRootsGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.WHITE_ROOTS_AND_TUBERS)

                                    } else {
                                        whiteRootsOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        whiteRootsMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        whiteRootsGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    whiteRootsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    whiteRootsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    whiteRootsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                meatTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (meatOwnFarm.editableText.toString()
                                            .isNotEmpty() && meatMarket.editableText.toString()
                                            .isNotEmpty() && meatGift.editableText.toString()
                                            .isNotEmpty() && meatOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && meatMarket.editableText.toString()
                                            .toDouble() == 0.0 && meatGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.MEAT)

                                    } else {
                                        meatOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        meatMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        meatGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    meatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    meatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    meatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                milkTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (milkOwnFarm.editableText.toString()
                                            .isNotEmpty() && milkMarket.editableText.toString()
                                            .isNotEmpty() && milkGift.editableText.toString()
                                            .isNotEmpty() && milkOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && milkMarket.editableText.toString()
                                            .toDouble() == 0.0 && milkGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.MILK_AND_DAIRY)

                                    } else {
                                        milkOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        milkMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        milkGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    milkOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    milkMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    milkGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                fishTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (fishOwnFarm.editableText.toString()
                                            .isNotEmpty() && fishOwnMarket.editableText.toString()
                                            .isNotEmpty() && fishGift.editableText.toString()
                                            .isNotEmpty() && fishOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && fishOwnMarket.editableText.toString()
                                            .toDouble() == 0.0 && fishGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.FISH_AND_SEA_FOOD)

                                    } else {
                                        fishOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        fishOwnMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        fishGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    fishOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fishOwnMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    fishGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                eggsTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (eggsOwnFarm.editableText.toString()
                                            .isNotEmpty() && eggsMarket.editableText.toString()
                                            .isNotEmpty() && eggsGift.editableText.toString()
                                            .isNotEmpty() && eggsOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && eggsMarket.editableText.toString()
                                            .toDouble() == 0.0 && eggsGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.EGGS)

                                    } else {
                                        eggsOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        eggsMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        eggsGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    eggsOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    eggsMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    eggsGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                fatsAndOilsTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (cookingFatOwnFarm.editableText.toString()
                                            .isNotEmpty() && cookingFatMarket.editableText.toString()
                                            .isNotEmpty() && cookingFatGift.editableText.toString()
                                            .isNotEmpty() && cookingFatOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && cookingFatMarket.editableText.toString()
                                            .toDouble() == 0.0 && cookingFatGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.FATS_AND_OILS)

                                    } else {
                                        cookingFatOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        cookingFatMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        cookingFatGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    cookingFatOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    cookingFatMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    cookingFatGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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
                                spicesTotal.text = totalPercentage.toString()
                                if (totalPercentage != 100.0) {
                                    if (spicesOwnFarm.editableText.toString()
                                            .isNotEmpty() && spicesMarket.editableText.toString()
                                            .isNotEmpty() && spicesGift.editableText.toString()
                                            .isNotEmpty() && spicesOwnFarm.editableText.toString()
                                            .toDouble() == 0.0 && spicesMarket.editableText.toString()
                                            .toDouble() == 0.0 && spicesGift.editableText.toString()
                                            .toDouble() == 0.0
                                    ) {

                                        if (foodConsumptionNotApplicableDialog?.isShowing ?: false)
                                            return@postDelayed
                                        inflateFoodConsumptionNotApplicableModal(FoodConsumptionEnum.SPICES)

                                    } else {
                                        spicesOwnFarmCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        spicesMarketCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        spicesGiftCell.background =
                                            context?.resources?.getDrawable(
                                                R.drawable.error_cell,
                                                null
                                            )
                                        foodConsumptionHasNoPercentageError = false
                                    }
                                } else {
                                    spicesOwnFarmCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    spicesMarketCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    spicesGiftCell.background =
                                        context?.resources?.getDrawable(R.drawable.cell_shape, null)
                                    foodConsumptionHasNoPercentageError = true
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

                    if (hasNoValidationError && foodConsumptionHasNoPercentageError) {

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

                        prepareCropSelectionListView()

                        wealthGroupQuestionnaire.lastQuestionnaireStep =
                            Constants.WG_CROP_SELECTION_STEP

                        if (!doesStepExist(
                                Constants.WG_CROP_SELECTION_STEP,
                                wealthGroupQuestionnaire.questionnaireCoveredSteps
                            )
                        ) {
                            wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.WG_CROP_SELECTION_STEP)
                        }

                        updateCurrentQuestionnaireToStore()

                        wgPercentFoodConsumptionIncome.root.visibility = View.GONE
                        cropSelectionLayout.root.visibility = View.VISIBLE

                    }
                }

                foodSourcesPercentBackButton.setOnClickListener {
                    populateMainSourcesIncomeAndfood()
                    wgPercentFoodConsumptionIncome.root.visibility = View.GONE
                    wgIncomeAndFoodSources.root.visibility = View.VISIBLE
                }
            }


            /* Crop Selection navigation */

            cropSelectionLayout.apply {

                cropSelectionBackButton.setOnClickListener {
                    populateFoodConsunptionPercentages()
                    wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE
                    cropSelectionLayout.root.visibility = View.GONE
                }

                cropSelectionNextButton.setOnClickListener {

                    if (wealthGroupQuestionnaire.selectedCrops.isNotEmpty()) {

                        if (determineTheFurthestCoveredStep(wealthGroupQuestionnaire.questionnaireCoveredSteps) < Constants.WG_CROP_PRODUCTION_STEP) {
                            prepareCropProduction()
                        } else {
                            updateCropProductionPage(
                                processUpdatedCropProductionresponses(
                                    wealthGroupQuestionnaire.selectedCrops,
                                    wealthGroupQuestionnaire.cropContributionResponseItems
                                )
                            )
                        }

                        wealthGroupQuestionnaire.lastQuestionnaireStep =
                            Constants.WG_CROP_PRODUCTION_STEP

                        if (!doesStepExist(
                                Constants.WG_CROP_PRODUCTION_STEP,
                                wealthGroupQuestionnaire.questionnaireCoveredSteps
                            )
                        ) {
                            wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.WG_CROP_PRODUCTION_STEP)
                        }

                        updateCurrentQuestionnaireToStore()

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
                    populateCropSelectionSection()
                    cropProductionLayout.root.visibility = View.GONE
                    cropSelectionLayout.root.visibility = View.VISIBLE
                }

                cropContributionNextButton.setOnClickListener {
                    if (!isAnyCropContributionValueEmpty() && !doesCropFoodConsumptionContributionIncomeHaveAPercentageError().hasError && !doesCropCashContributionIncomeHaveAPercentageError().hasError) {

                        wealthGroupQuestionnaire.cropContributionResponseItems =
                            cropContributionResponseItems
                        wealthGroupQuestionnaire.lastQuestionnaireStep =
                            Constants.LIVESTOCK_POULTRY_NUMBERS_STEP

                        if (!doesStepExist(
                                Constants.LIVESTOCK_POULTRY_NUMBERS_STEP,
                                wealthGroupQuestionnaire.questionnaireCoveredSteps
                            )
                        ) {
                            wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.LIVESTOCK_POULTRY_NUMBERS_STEP)
                        }

                        updateCurrentQuestionnaireToStore()
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
                    populateCropProduction()
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
                        wealthGroupQuestionnaire.lastQuestionnaireStep =
                            Constants.LIVESTOCK_POULTRY_CONTRIBUTION_STEP
                        if (!doesStepExist(
                                Constants.LIVESTOCK_POULTRY_CONTRIBUTION_STEP,
                                wealthGroupQuestionnaire.questionnaireCoveredSteps
                            )
                        ) {
                            wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.LIVESTOCK_POULTRY_CONTRIBUTION_STEP)
                        }

                        updateCurrentQuestionnaireToStore()
                        wgLivestockPoultryContribution.root.visibility = View.VISIBLE
                        wgLivestockPoultryNumbers.root.visibility = View.GONE

                    }
                }
            }


            /*Livestock and poultry contribution navigation*/
            wgLivestockPoultryContribution.apply {


                if (!doesRankItemAlreadyExistInTheRankList(
                        0,
                        livestockCashIncomeContributionRanks
                    )
                ) {
                    livestockCashIncomeContributionRanks.add(
                        RankResponseItem(
                            0,
                            false
                        )
                    )
                }

                if (!doesRankItemAlreadyExistInTheRankList(
                        0,
                        livestockFoodConsumptionContributionRanks
                    )
                ) {
                    livestockFoodConsumptionContributionRanks.add(
                        RankResponseItem(
                            0,
                            false
                        )
                    )
                }

                for (i in 0..11) {
                    livestockCashIncomeContributionRanks.add(
                        RankResponseItem(i + 1, false)
                    )
                    livestockFoodConsumptionContributionRanks.add(
                        RankResponseItem(i + 1, false)
                    )
                }
                cattleIncomeRank.setOnClickListener {
                    if (livestockContributionResponses.cattle.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.cattle.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.cattle.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        cattleIncomeRank.text = "Select rank..."
                        livestockContributionResponses.cattle.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.CATTLE
                        )
                    }
                }

                cattleFoodRank.setOnClickListener {
                    if (livestockContributionResponses.cattle.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.cattle.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.cattle.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        cattleFoodRank.text = "Select rank..."
                        livestockContributionResponses.cattle.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.CATTLE
                        )
                    }
                }

                dairyCattleIncomeRank.setOnClickListener {
                    if (livestockContributionResponses.dairyCattle.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.dairyCattle.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.dairyCattle.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        dairyCattleIncomeRank.text = "Select rank..."
                        livestockContributionResponses.dairyCattle.incomeRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.DAIRY_CATTLE
                        )
                    }
                }

                dairyCattleFoodRank.setOnClickListener {
                    if (livestockContributionResponses.dairyCattle.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.dairyCattle.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.dairyCattle.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        dairyCattleFoodRank.text = "Select rank..."
                        livestockContributionResponses.dairyCattle.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.DAIRY_CATTLE
                        )
                    }
                }

                goatsIncomeRank.setOnClickListener {
                    if (livestockContributionResponses.goats.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.goats.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.goats.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        goatsIncomeRank.text = "Select rank..."
                        livestockContributionResponses.goats.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.GOATS
                        )
                    }
                }

                goatsFoodRank.setOnClickListener {
                    if (livestockContributionResponses.goats.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.goats.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.goats.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        goatsFoodRank.text = "Select rank..."
                        livestockContributionResponses.goats.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.GOATS
                        )
                    }
                }

                sheepCashRank.setOnClickListener {
                    if (livestockContributionResponses.sheep.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.sheep.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.sheep.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        sheepCashRank.text = "Select rank..."
                        livestockContributionResponses.sheep.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.SHEEP
                        )
                    }
                }

                sheepFoodRank.setOnClickListener {
                    if (livestockContributionResponses.sheep.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.sheep.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.sheep.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        sheepFoodRank.text = "Select rank..."
                        livestockContributionResponses.sheep.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.SHEEP
                        )
                    }
                }

                donkeysCashRank.setOnClickListener {
                    if (livestockContributionResponses.donkeys.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.donkeys.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.donkeys.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        donkeysCashRank.text = "Select rank..."
                        livestockContributionResponses.donkeys.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.DONKEYS
                        )
                    }
                }

                donkeysFoodRank.setOnClickListener {
                    if (livestockContributionResponses.donkeys.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.donkeys.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.donkeys.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        donkeysFoodRank.text = "Select rank..."
                        livestockContributionResponses.donkeys.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.DONKEYS
                        )
                    }
                }

                pigscashRank.setOnClickListener {
                    if (livestockContributionResponses.pigs.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.pigs.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.pigs.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        pigscashRank.text = "Select rank..."
                        livestockContributionResponses.pigs.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.PIGS
                        )
                    }
                }

                pigsFoodrank.setOnClickListener {
                    if (livestockContributionResponses.pigs.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.pigs.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.pigs.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        pigsFoodrank.text = "Select rank..."
                        livestockContributionResponses.pigs.consumptionRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.PIGS
                        )
                    }
                }

                chickenCashRank.setOnClickListener {
                    if (livestockContributionResponses.chicken.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.chicken.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.chicken.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        chickenCashRank.text = "Select rank..."
                        livestockContributionResponses.chicken.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.CHICKEN
                        )
                    }
                }

                chickenFoodRank.setOnClickListener {
                    if (livestockContributionResponses.chicken.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.chicken.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.chicken.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        chickenFoodRank.text = "Select rank..."
                        livestockContributionResponses.chicken.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.CHICKEN
                        )
                    }
                }

                improvedChickenCashRank.setOnClickListener {
                    if (livestockContributionResponses.improvedChicken.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.improvedChicken.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.improvedChicken.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        improvedChickenCashRank.text = "Select rank..."
                        livestockContributionResponses.improvedChicken.incomeRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.IMPROVED_CHICKEN
                        )
                    }
                }

                improvedChickenFoodRank.setOnClickListener {
                    if (livestockContributionResponses.improvedChicken.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.improvedChicken.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.improvedChicken.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        improvedChickenFoodRank.text = "Select rank..."
                        livestockContributionResponses.improvedChicken.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.IMPROVED_CHICKEN
                        )
                    }
                }

                camelsCashRank.setOnClickListener {
                    if (livestockContributionResponses.camels.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.camels.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.camels.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        camelsCashRank.text = "Select rank..."
                        livestockContributionResponses.camels.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.CAMELS
                        )
                    }
                }

                camelsFoodRank.setOnClickListener {
                    if (livestockContributionResponses.camels.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.camels.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.camels.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        camelsFoodRank.text = "Select rank..."
                        livestockContributionResponses.camels.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.CAMELS
                        )
                    }
                }

                ducksCashRank.setOnClickListener {
                    if (livestockContributionResponses.ducks.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.ducks.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.ducks.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        ducksCashRank.text = "Select rank..."
                        livestockContributionResponses.ducks.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.DUCKS
                        )
                    }
                }

                ducksFoodRank.setOnClickListener {
                    if (livestockContributionResponses.ducks.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.ducks.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.ducks.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        ducksFoodRank.text = "Select rank..."
                        livestockContributionResponses.ducks.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.DUCKS
                        )
                    }
                }

                beeHivesCashRank.setOnClickListener {
                    if (livestockContributionResponses.beeHives.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.beeHives.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.beeHives.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        beeHivesCashRank.text = "Select rank..."
                        livestockContributionResponses.beeHives.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.BEE_HIVES
                        )
                    }
                }

                beeHivesFoodrank.setOnClickListener {
                    if (livestockContributionResponses.beeHives.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.beeHives.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.beeHives.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        beeHivesFoodrank.text = "Select rank..."
                        livestockContributionResponses.beeHives.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.BEE_HIVES
                        )
                    }
                }

                fishPondsCashRank.setOnClickListener {
                    if (livestockContributionResponses.fishPonds.incomeRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.fishPonds.incomeRank.actualValue.toInt(),
                                livestockCashIncomeContributionRanks
                            )
                        ) {
                            livestockCashIncomeContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.fishPonds.incomeRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        fishPondsCashRank.text = "Select rank..."
                        livestockContributionResponses.fishPonds.incomeRank.hasBeenSubmitted = false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockCashIncomeContributionRanks,
                            LivestockContributionRankTypeEnum.CASH_CONTRIBUTION,
                            WgLivestockTypesEnum.FISH_POND
                        )
                    }
                }

                fishPondsFoodRank.setOnClickListener {
                    if (livestockContributionResponses.fishPonds.consumptionRank.hasBeenSubmitted) {
                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockContributionResponses.fishPonds.consumptionRank.actualValue.toInt(),
                                livestockFoodConsumptionContributionRanks
                            )
                        ) {
                            livestockFoodConsumptionContributionRanks.add(
                                RankResponseItem(
                                    livestockContributionResponses.fishPonds.consumptionRank.actualValue.toInt(),
                                    false
                                )
                            )
                        }
                        fishPondsFoodRank.text = "Select rank..."
                        livestockContributionResponses.fishPonds.consumptionRank.hasBeenSubmitted =
                            false
                    } else {
                        inflateLivestockContributionRankModal(
                            livestockFoodConsumptionContributionRanks,
                            LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION,
                            WgLivestockTypesEnum.FISH_POND
                        )
                    }
                }



                livestockPoultryContributionBackButton.setOnClickListener {
                    populateLivestockAndPoultryOwnership()
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

                            wealthGroupQuestionnaire.lastQuestionnaireStep =
                                Constants.LABOUR_PATTERNS_STEP

                            if (!doesStepExist(
                                    Constants.LABOUR_PATTERNS_STEP,
                                    wealthGroupQuestionnaire.questionnaireCoveredSteps
                                )
                            ) {
                                wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.LABOUR_PATTERNS_STEP)
                            }

                            updateCurrentQuestionnaireToStore()

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
                                ).toDouble() + returnZeroStringIfEmpty(transportServicesMen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    wagedLabourmen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(
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

                            menTotal.text = menTotalEntry.toString()

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
                                ).toDouble() + returnZeroStringIfEmpty(transportServicesWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    wagedLabourWomen.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(
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

                            womenTotal.text = womenTotalEntry.toString()

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
                    populateLivestockAndPoultryContribution()
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
                            ).toDouble() + returnZeroStringIfEmpty(transportServicesMen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                wagedLabourmen.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(
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
                            ).toDouble() + returnZeroStringIfEmpty(transportServicesWomen.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                wagedLabourWomen.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(
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

                            wealthGroupQuestionnaire.lastQuestionnaireStep =
                                Constants.EXPENDITURE_PATTERNS_STEP

                            if (!doesStepExist(
                                    Constants.EXPENDITURE_PATTERNS_STEP,
                                    wealthGroupQuestionnaire.questionnaireCoveredSteps
                                )
                            ) {
                                wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.EXPENDITURE_PATTERNS_STEP)
                            }

                            updateCurrentQuestionnaireToStore()

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
                    populateLabourPatterns()
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

                            wealthGroupQuestionnaire.lastQuestionnaireStep =
                                Constants.MIGRATION_PATTERNS_STEP

                            if (!doesStepExist(
                                    Constants.MIGRATION_PATTERNS_STEP,
                                    wealthGroupQuestionnaire.questionnaireCoveredSteps
                                )
                            ) {
                                wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.MIGRATION_PATTERNS_STEP)
                            }

                            updateCurrentQuestionnaireToStore()

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
                    populateExpenditurePatterns()
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

                            migrationTotal.text = totalEntry.toString()
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

                            wealthGroupQuestionnaire.lastQuestionnaireStep =
                                Constants.CONSTRAINTS_STEP

                            if (!doesStepExist(
                                    Constants.CONSTRAINTS_STEP,
                                    wealthGroupQuestionnaire.questionnaireCoveredSteps
                                )
                            ) {
                                wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.CONSTRAINTS_STEP)
                            }

                            updateCurrentQuestionnaireToStore()

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
                    populateSettlementAndMigration()
                    wgMigrationPatterns.root.visibility = View.VISIBLE
                    wgConstraints.root.visibility = View.GONE
                }

                for (i in 0..5) {
                    incomeSourceRanks.add(RankResponseItem(i, false))
                }
                for (i in 0..9) {
                    incomeConsumptionRanks.add(RankResponseItem(i, false))
                }
                for (i in 0..7) {
                    livestockProductionRanks.add(RankResponseItem(i, false))
                }
                for (i in 0..6) {
                    fishingRanks.add(RankResponseItem(i, false))
                }
                for (i in 0..4) {
                    naturalResourceRanks.add(RankResponseItem(i, false))
                }
                for (i in 0..5) {
                    smallEnterpriesRanks.add(RankResponseItem(i, false))
                }

                labourLowEducation.setOnClickListener {
                    if (wagedLabourIncomeConstraintsResponses.lowEducation != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                wagedLabourIncomeConstraintsResponses.lowEducation,
                                incomeSourceRanks
                            )
                        ) {

                            incomeSourceRanks.add(
                                RankResponseItem(
                                    wagedLabourIncomeConstraintsResponses.lowEducation,
                                    false
                                )
                            )

                        }

                        labourLowEducation.text = "Select rank..."
                        wagedLabourIncomeConstraintsResponses.lowEducation = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeSourceRanks,
                            ConstraintsTypeEnum.IS_LOW_EDUCATION,
                            ConstraintCategoryEnum.SOURCE_OF_INCOME
                        )
                    }
                }
                labourPoorHealth.setOnClickListener {
                    if (wagedLabourIncomeConstraintsResponses.poorHealth != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                wagedLabourIncomeConstraintsResponses.poorHealth,
                                incomeSourceRanks
                            )
                        ) {

                            incomeSourceRanks.add(
                                RankResponseItem(
                                    wagedLabourIncomeConstraintsResponses.poorHealth,
                                    false
                                )
                            )

                        }

                        labourPoorHealth.text = "Select rank..."
                        wagedLabourIncomeConstraintsResponses.poorHealth = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeSourceRanks,
                            ConstraintsTypeEnum.IS_POOR_HEALTH,
                            ConstraintCategoryEnum.SOURCE_OF_INCOME
                        )
                    }
                }
                labourFewJobs.setOnClickListener {
                    if (wagedLabourIncomeConstraintsResponses.fewJobs != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                wagedLabourIncomeConstraintsResponses.fewJobs,
                                incomeSourceRanks
                            )
                        ) {

                            incomeSourceRanks.add(
                                RankResponseItem(
                                    wagedLabourIncomeConstraintsResponses.fewJobs,
                                    false
                                )
                            )

                        }

                        labourFewJobs.text = "Select rank..."
                        wagedLabourIncomeConstraintsResponses.fewJobs = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeSourceRanks,
                            ConstraintsTypeEnum.IS_FEW_JOBS,
                            ConstraintCategoryEnum.SOURCE_OF_INCOME
                        )
                    }
                }
                labourFarmTime.setOnClickListener {
                    if (wagedLabourIncomeConstraintsResponses.tooMuchFarmTime != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                wagedLabourIncomeConstraintsResponses.tooMuchFarmTime,
                                incomeSourceRanks
                            )
                        ) {

                            incomeSourceRanks.add(
                                RankResponseItem(
                                    wagedLabourIncomeConstraintsResponses.tooMuchFarmTime,
                                    false
                                )
                            )

                        }

                        labourFarmTime.text = "Select rank..."
                        wagedLabourIncomeConstraintsResponses.tooMuchFarmTime = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeSourceRanks,
                            ConstraintsTypeEnum.IS_TIME_ON_FARM,
                            ConstraintCategoryEnum.SOURCE_OF_INCOME
                        )
                    }
                }
                labourLowWageRates.setOnClickListener {
                    if (wagedLabourIncomeConstraintsResponses.lowAverageWageRates != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                wagedLabourIncomeConstraintsResponses.lowAverageWageRates,
                                incomeSourceRanks
                            )
                        ) {

                            incomeSourceRanks.add(
                                RankResponseItem(
                                    wagedLabourIncomeConstraintsResponses.lowAverageWageRates,
                                    false
                                )
                            )

                        }

                        labourLowWageRates.text = "Select rank..."
                        wagedLabourIncomeConstraintsResponses.lowAverageWageRates = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeSourceRanks,
                            ConstraintsTypeEnum.IS_LOW_WAGE_RATES,
                            ConstraintCategoryEnum.SOURCE_OF_INCOME
                        )
                    }
                }


                consumptionHoldings.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.smallLandHoldings != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.smallLandHoldings,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.smallLandHoldings,
                                    false
                                )
                            )

                        }

                        consumptionHoldings.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.smallLandHoldings = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_SMALL_LAND,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionLackOfCredit.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.lackOfCredit != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.lackOfCredit,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.lackOfCredit,
                                    false
                                )
                            )

                        }

                        consumptionLackOfCredit.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.lackOfCredit = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_LACK_OF_CREDIT,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionHighInputs.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.highInputCost != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.highInputCost,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.highInputCost,
                                    false
                                )
                            )

                        }

                        consumptionHighInputs.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.highInputCost = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_HIGH_INPUT_COSTS,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionLowFertility.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.lowLandFertility != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.lowLandFertility,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.lowLandFertility,
                                    false
                                )
                            )

                        }

                        consumptionLowFertility.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.lowLandFertility = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_LOW_LAND_FERTILITY,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionUnreliableWater.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.lackOfReliableWater != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.lackOfReliableWater,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.lackOfReliableWater,
                                    false
                                )
                            )

                        }

                        consumptionUnreliableWater.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.lackOfReliableWater = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_UNRELIABLE_WATER,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionLowTechnicalSkills.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.lowTechnicalSkills != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.lowTechnicalSkills,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.lowTechnicalSkills,
                                    false
                                )
                            )

                        }

                        consumptionLowTechnicalSkills.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.lowTechnicalSkills = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_LOW_TECHNICAL_SKILLS,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionLowSeedQuality.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.lowQualitySeed != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.lowQualitySeed,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.lowQualitySeed,
                                    false
                                )
                            )

                        }

                        consumptionLowSeedQuality.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.lowQualitySeed = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_LOW_QUALITY_SEED,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionMarketAccess.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.lackOfMarketAccess != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.lackOfMarketAccess,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.lackOfMarketAccess,
                                    false
                                )
                            )

                        }

                        consumptionMarketAccess.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.lackOfMarketAccess = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_MARKET_ACCESS,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }
                consumptionCropPests.setOnClickListener {
                    if (cropProductionIncomeConstraintsResponses.endemicCropPests != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                cropProductionIncomeConstraintsResponses.endemicCropPests,
                                incomeConsumptionRanks
                            )
                        ) {

                            incomeConsumptionRanks.add(
                                RankResponseItem(
                                    cropProductionIncomeConstraintsResponses.endemicCropPests,
                                    false
                                )
                            )

                        }

                        consumptionCropPests.text = "Select rank..."
                        cropProductionIncomeConstraintsResponses.endemicCropPests = -1

                    } else {
                        inflateConstraintsRankModal(
                            incomeConsumptionRanks,
                            ConstraintsTypeEnum.IC_CROP_PESTS_DISEASES,
                            ConstraintCategoryEnum.INCOME_CONSUMPTION
                        )
                    }
                }


                livestockProductionPasture.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.lackOfPasture != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.lackOfPasture,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.lackOfPasture,
                                    false
                                )
                            )

                        }

                        livestockProductionPasture.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.lackOfPasture = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_LACK_OF_PASTURE,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }
                livestockProductionDrinkingWater.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater,
                                    false
                                )
                            )

                        }

                        livestockProductionDrinkingWater.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_LACK_ANIMAL_DRINKING_WATER,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }
                livestockProductionLowYieldingAnimal.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.lowYieldingAnimal != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.lowYieldingAnimal,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.lowYieldingAnimal,
                                    false
                                )
                            )

                        }

                        livestockProductionLowYieldingAnimal.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.lowYieldingAnimal = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_LOW_YIELDING_ANIMALS,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }
                livestockProductionVeterinaryDrugs.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs,
                                    false
                                )
                            )

                        }

                        livestockProductionVeterinaryDrugs.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_COSTLY_VETERINARY_DRUGS,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }
                livestockProductionPests.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases,
                                    false
                                )
                            )

                        }

                        livestockProductionPests.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_LIVESTOCK_PESTS_DISEASES,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }
                livestockProductionMarket.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.lackofMarket != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.lackofMarket,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.lackofMarket,
                                    false
                                )
                            )

                        }

                        livestockProductionMarket.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.lackofMarket = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_LACK_OF_MARKET,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }
                livestockProductionInsecurity.setOnClickListener {
                    if (livestockProductionIncomeConstraintsResponses.insecurity != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                livestockProductionIncomeConstraintsResponses.insecurity,
                                livestockProductionRanks
                            )
                        ) {

                            livestockProductionRanks.add(
                                RankResponseItem(
                                    livestockProductionIncomeConstraintsResponses.insecurity,
                                    false
                                )
                            )

                        }

                        livestockProductionInsecurity.text = "Select rank..."
                        livestockProductionIncomeConstraintsResponses.insecurity = -1

                    } else {
                        inflateConstraintsRankModal(
                            livestockProductionRanks,
                            ConstraintsTypeEnum.LP_INSECURITY,
                            ConstraintCategoryEnum.LIVESTOCK_PRODUCTION
                        )
                    }
                }


                fishingLowStocks.setOnClickListener {
                    if (fishingIncomeConstraintsResponses.lowFishStocks != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                fishingIncomeConstraintsResponses.lowFishStocks,
                                fishingRanks
                            )
                        ) {

                            fishingRanks.add(
                                RankResponseItem(
                                    fishingIncomeConstraintsResponses.lowFishStocks,
                                    false
                                )
                            )

                        }

                        fishingLowStocks.text = "Select rank..."
                        fishingIncomeConstraintsResponses.lowFishStocks = -1

                    } else {
                        inflateConstraintsRankModal(
                            fishingRanks,
                            ConstraintsTypeEnum.F_LOW_FISH_STOCKS,
                            ConstraintCategoryEnum.FISHING
                        )
                    }
                }
                fishingPoorMarket.setOnClickListener {
                    if (fishingIncomeConstraintsResponses.poorMarket != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                fishingIncomeConstraintsResponses.poorMarket,
                                fishingRanks
                            )
                        ) {

                            fishingRanks.add(
                                RankResponseItem(
                                    fishingIncomeConstraintsResponses.poorMarket,
                                    false
                                )
                            )

                        }

                        fishingPoorMarket.text = "Select rank..."
                        fishingIncomeConstraintsResponses.poorMarket = -1

                    } else {
                        inflateConstraintsRankModal(
                            fishingRanks,
                            ConstraintsTypeEnum.F_POOR_MARKET,
                            ConstraintCategoryEnum.FISHING
                        )
                    }
                }
                fishingLackOfEquipment.setOnClickListener {
                    if (fishingIncomeConstraintsResponses.lackOfEquipment != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                fishingIncomeConstraintsResponses.lackOfEquipment,
                                fishingRanks
                            )
                        ) {

                            fishingRanks.add(
                                RankResponseItem(
                                    fishingIncomeConstraintsResponses.lackOfEquipment,
                                    false
                                )
                            )

                        }

                        fishingLackOfEquipment.text = "Select rank..."
                        fishingIncomeConstraintsResponses.lackOfEquipment = -1

                    } else {
                        inflateConstraintsRankModal(
                            fishingRanks,
                            ConstraintsTypeEnum.F_EQUIPMENT,
                            ConstraintCategoryEnum.FISHING
                        )
                    }
                }
                fishingCompetition.setOnClickListener {
                    if (fishingIncomeConstraintsResponses.extremeCompetition != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                fishingIncomeConstraintsResponses.extremeCompetition,
                                fishingRanks
                            )
                        ) {

                            fishingRanks.add(
                                RankResponseItem(
                                    fishingIncomeConstraintsResponses.extremeCompetition,
                                    false
                                )
                            )

                        }

                        fishingCompetition.text = "Select rank..."
                        fishingIncomeConstraintsResponses.extremeCompetition = -1

                    } else {
                        inflateConstraintsRankModal(
                            fishingRanks,
                            ConstraintsTypeEnum.F_COMPETITION,
                            ConstraintCategoryEnum.FISHING
                        )
                    }
                }
                fishingLackOfExpertise.setOnClickListener {
                    if (fishingIncomeConstraintsResponses.lackOfExpertise != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                fishingIncomeConstraintsResponses.lackOfExpertise,
                                fishingRanks
                            )
                        ) {

                            fishingRanks.add(
                                RankResponseItem(
                                    fishingIncomeConstraintsResponses.lackOfExpertise,
                                    false
                                )
                            )

                        }

                        fishingLackOfExpertise.text = "Select rank..."
                        fishingIncomeConstraintsResponses.lackOfExpertise = -1

                    } else {
                        inflateConstraintsRankModal(
                            fishingRanks,
                            ConstraintsTypeEnum.F_LACK_OF_EXPERTISE,
                            ConstraintCategoryEnum.FISHING
                        )
                    }
                }
                fishingFishingRights.setOnClickListener {
                    if (fishingIncomeConstraintsResponses.fishingRightsRestrictions != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                fishingIncomeConstraintsResponses.fishingRightsRestrictions,
                                fishingRanks
                            )
                        ) {

                            fishingRanks.add(
                                RankResponseItem(
                                    fishingIncomeConstraintsResponses.fishingRightsRestrictions,
                                    false
                                )
                            )

                        }

                        fishingFishingRights.text = "Select rank..."
                        fishingIncomeConstraintsResponses.fishingRightsRestrictions = -1

                    } else {
                        inflateConstraintsRankModal(
                            fishingRanks,
                            ConstraintsTypeEnum.F_FISHING_RIGHTS,
                            ConstraintCategoryEnum.FISHING
                        )
                    }
                }


                resourceDecline.setOnClickListener {
                    if (naturalResourceIncomeConstraintsResponses.decliningNaturalResources != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                naturalResourceIncomeConstraintsResponses.decliningNaturalResources,
                                naturalResourceRanks
                            )
                        ) {

                            naturalResourceRanks.add(
                                RankResponseItem(
                                    naturalResourceIncomeConstraintsResponses.decliningNaturalResources,
                                    false
                                )
                            )

                        }

                        resourceDecline.text = "Select rank..."
                        naturalResourceIncomeConstraintsResponses.decliningNaturalResources = -1

                    } else {
                        inflateConstraintsRankModal(
                            naturalResourceRanks,
                            ConstraintsTypeEnum.NR_DECLINING_RESOURCE,
                            ConstraintCategoryEnum.NATURAL_RESOURCE
                        )
                    }
                }
                resourcePopulationPressure.setOnClickListener {
                    if (naturalResourceIncomeConstraintsResponses.populationPressure != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                naturalResourceIncomeConstraintsResponses.populationPressure,
                                naturalResourceRanks
                            )
                        ) {

                            naturalResourceRanks.add(
                                RankResponseItem(
                                    naturalResourceIncomeConstraintsResponses.populationPressure,
                                    false
                                )
                            )

                        }

                        resourcePopulationPressure.text = "Select rank..."
                        naturalResourceIncomeConstraintsResponses.populationPressure = -1

                    } else {
                        inflateConstraintsRankModal(
                            naturalResourceRanks,
                            ConstraintsTypeEnum.NR_POPULATION_PRESSURE,
                            ConstraintCategoryEnum.NATURAL_RESOURCE
                        )
                    }
                }
                resourceRights.setOnClickListener {
                    if (naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights,
                                naturalResourceRanks
                            )
                        ) {

                            naturalResourceRanks.add(
                                RankResponseItem(
                                    naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights,
                                    false
                                )
                            )

                        }

                        resourceRights.text = "Select rank..."
                        naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights =
                            -1

                    } else {
                        inflateConstraintsRankModal(
                            naturalResourceRanks,
                            ConstraintsTypeEnum.NR_RIGHTS_RESTRICTIONS,
                            ConstraintCategoryEnum.NATURAL_RESOURCE
                        )
                    }
                }
                resourceLowValue.setOnClickListener {
                    if (naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts,
                                naturalResourceRanks
                            )
                        ) {

                            naturalResourceRanks.add(
                                RankResponseItem(
                                    naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts,
                                    false
                                )
                            )

                        }

                        resourceLowValue.text = "Select rank..."
                        naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts = -1

                    } else {
                        inflateConstraintsRankModal(
                            naturalResourceRanks,
                            ConstraintsTypeEnum.NR_LOW_VALUE,
                            ConstraintCategoryEnum.NATURAL_RESOURCE
                        )
                    }
                }


                enterpriseLackOfCapital.setOnClickListener {
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfCapital != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                smallEnterpriseIncomeConstraintsResponses.lackOfCapital,
                                smallEnterpriesRanks
                            )
                        ) {

                            smallEnterpriesRanks.add(
                                RankResponseItem(
                                    smallEnterpriseIncomeConstraintsResponses.lackOfCapital,
                                    false
                                )
                            )

                        }

                        enterpriseLackOfCapital.text = "Select rank..."
                        smallEnterpriseIncomeConstraintsResponses.lackOfCapital = -1

                    } else {
                        inflateConstraintsRankModal(
                            smallEnterpriesRanks,
                            ConstraintsTypeEnum.SE_LACK_OF_CAPITAL,
                            ConstraintCategoryEnum.SMALL_ENTERPRISE
                        )
                    }
                }
                enterpriseRedTape.setOnClickListener {
                    if (smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape,
                                smallEnterpriesRanks
                            )
                        ) {

                            smallEnterpriesRanks.add(
                                RankResponseItem(
                                    smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape,
                                    false
                                )
                            )

                        }

                        enterpriseRedTape.text = "Select rank..."
                        smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape = -1

                    } else {
                        inflateConstraintsRankModal(
                            smallEnterpriesRanks,
                            ConstraintsTypeEnum.SE_RED_TAPE,
                            ConstraintCategoryEnum.SMALL_ENTERPRISE
                        )
                    }
                }
                enterpriseTaxes.setOnClickListener {
                    if (smallEnterpriseIncomeConstraintsResponses.tooManyTaxes != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                smallEnterpriseIncomeConstraintsResponses.tooManyTaxes,
                                smallEnterpriesRanks
                            )
                        ) {

                            smallEnterpriesRanks.add(
                                RankResponseItem(
                                    smallEnterpriseIncomeConstraintsResponses.tooManyTaxes,
                                    false
                                )
                            )

                        }

                        enterpriseTaxes.text = "Select rank..."
                        smallEnterpriseIncomeConstraintsResponses.tooManyTaxes = -1

                    } else {
                        inflateConstraintsRankModal(
                            smallEnterpriesRanks,
                            ConstraintsTypeEnum.SE_TAXES,
                            ConstraintCategoryEnum.SMALL_ENTERPRISE
                        )
                    }
                }
                enterpriseMarketAccess.setOnClickListener {
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket,
                                smallEnterpriesRanks
                            )
                        ) {

                            smallEnterpriesRanks.add(
                                RankResponseItem(
                                    smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket,
                                    false
                                )
                            )

                        }

                        enterpriseMarketAccess.text = "Select rank..."
                        smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket = -1

                    } else {
                        inflateConstraintsRankModal(
                            smallEnterpriesRanks,
                            ConstraintsTypeEnum.SE_MARKET_ACCESS,
                            ConstraintCategoryEnum.SMALL_ENTERPRISE
                        )
                    }
                }
                enterpriseExpertise.setOnClickListener {
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfExpertise != -1) {

                        if (!doesRankItemAlreadyExistInTheRankList(
                                smallEnterpriseIncomeConstraintsResponses.lackOfExpertise,
                                smallEnterpriesRanks
                            )
                        ) {

                            smallEnterpriesRanks.add(
                                RankResponseItem(
                                    smallEnterpriseIncomeConstraintsResponses.lackOfExpertise,
                                    false
                                )
                            )

                        }

                        enterpriseExpertise.text = "Select rank..."
                        smallEnterpriseIncomeConstraintsResponses.lackOfExpertise = -1

                    } else {
                        inflateConstraintsRankModal(
                            smallEnterpriesRanks,
                            ConstraintsTypeEnum.SE_LACK_OF_EXPERTISE,
                            ConstraintCategoryEnum.SMALL_ENTERPRISE
                        )
                    }
                }

                constraintsNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (wagedLabourIncomeConstraintsResponses.lowEducation == -1) {
                        hasNoValidationError = false
                        labourLowEducationCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.poorHealth == -1) {
                        hasNoValidationError = false
                        labourPoorHealthCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.fewJobs == -1) {
                        hasNoValidationError = false
                        labourFewJobsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.tooMuchFarmTime == -1) {
                        hasNoValidationError = false
                        labourFarmTimeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (wagedLabourIncomeConstraintsResponses.lowAverageWageRates == -1) {
                        hasNoValidationError = false
                        labourLowWageRatesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (cropProductionIncomeConstraintsResponses.smallLandHoldings == -1) {
                        hasNoValidationError = false
                        consumptionHoldingsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lackOfCredit == -1) {
                        hasNoValidationError = false
                        consumptionLackOfCreditCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (cropProductionIncomeConstraintsResponses.highInputCost == -1) {
                        hasNoValidationError = false
                        consumptionHighInputsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lowLandFertility == -1) {
                        hasNoValidationError = false
                        consumptionLowFertilityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lackOfReliableWater == -1) {
                        hasNoValidationError = false
                        consumptionUnreliableWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lowTechnicalSkills == -1) {
                        hasNoValidationError = false
                        consumptionLowTechnicalSkillsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lowQualitySeed == -1) {
                        hasNoValidationError = false
                        consumptionLowSeedQualityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.lackOfMarketAccess == -1) {
                        hasNoValidationError = false
                        consumptionMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (cropProductionIncomeConstraintsResponses.endemicCropPests == -1) {
                        hasNoValidationError = false
                        consumptionCropPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lackOfPasture == -1) {
                        hasNoValidationError = false
                        livestockProductionPastureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater == -1) {
                        hasNoValidationError = false
                        livestockProductionDrinkingWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lowYieldingAnimal == -1) {
                        hasNoValidationError = false
                        livestockProductionLowYieldingAnimalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs == -1) {
                        hasNoValidationError = false
                        livestockProductionVeterinaryDrugsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases == -1) {
                        hasNoValidationError = false
                        livestockProductionPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.lackofMarket == -1) {
                        hasNoValidationError = false
                        livestockProductionMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (livestockProductionIncomeConstraintsResponses.insecurity == -1) {
                        hasNoValidationError = false
                        livestockProductionInsecurityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.lowFishStocks == -1) {
                        hasNoValidationError = false
                        fishingLowStocksCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.poorMarket == -1) {
                        hasNoValidationError = false
                        fishingPoorMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.lackOfEquipment == -1) {
                        hasNoValidationError = false
                        fishingLackOfEquipmentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.extremeCompetition == -1) {
                        hasNoValidationError = false
                        fishingCompetitionCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.lackOfExpertise == -1) {
                        hasNoValidationError = false
                        fishingLackOfExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (fishingIncomeConstraintsResponses.fishingRightsRestrictions == -1) {
                        hasNoValidationError = false
                        fishingFishingRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.decliningNaturalResources == -1) {
                        hasNoValidationError = false
                        resourceDeclineCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.populationPressure == -1) {
                        hasNoValidationError = false
                        resourcePopulationPressureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights == -1) {
                        hasNoValidationError = false
                        resourceRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts == -1) {
                        hasNoValidationError = false
                        resourceLowValueCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfCapital == -1) {
                        hasNoValidationError = false
                        enterpriseLackOfCapitalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape == -1) {
                        hasNoValidationError = false
                        enterpriseRedTapeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.tooManyTaxes == -1) {
                        hasNoValidationError = false
                        enterpriseTaxesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket == -1) {
                        hasNoValidationError = false
                        enterpriseMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (smallEnterpriseIncomeConstraintsResponses.lackOfExpertise == -1) {
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

                        wealthGroupQuestionnaire.lastQuestionnaireStep =
                            Constants.COPING_STRATEGIES_STEP

                        if (!doesStepExist(
                                Constants.COPING_STRATEGIES_STEP,
                                wealthGroupQuestionnaire.questionnaireCoveredSteps
                            )
                        ) {
                            wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.COPING_STRATEGIES_STEP)
                        }

                        updateCurrentQuestionnaireToStore()

                        wgCopingStrategies.root.visibility = View.VISIBLE
                        wgConstraints.root.visibility = View.GONE

                    }
                }
            }

            /* I) Coping strategies */
            wgCopingStrategies.apply {

                copingBackButton.setOnClickListener {
                    populateConstraints()
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

                            wealthGroupQuestionnaire.lastQuestionnaireStep =
                                Constants.FGD_PARTICIPANTS_STEP

                            if (!doesStepExist(
                                    Constants.FGD_PARTICIPANTS_STEP,
                                    wealthGroupQuestionnaire.questionnaireCoveredSteps
                                )
                            ) {
                                wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.FGD_PARTICIPANTS_STEP)
                            }


                            updateCurrentQuestionnaireToStore()
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
                                it1,
                                false
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
                    populateCopingStrategies()
                    fdgParticipants.root.visibility = View.GONE
                    wgCopingStrategies.root.visibility = View.VISIBLE
                }

                fdgParticipantsNextButton.setOnClickListener {

                    wealthGroupQuestionnaire.fdgParticipants = fdgParticipantsModelList

                    wealthGroupQuestionnaire.lastQuestionnaireStep = Constants.WG_COMPLETION_PAGE

                    if (!doesStepExist(
                            Constants.WG_COMPLETION_PAGE,
                            wealthGroupQuestionnaire.questionnaireCoveredSteps
                        )
                    ) {
                        wealthGroupQuestionnaire.questionnaireCoveredSteps.add(Constants.WG_COMPLETION_PAGE)
                    }

                    updateCurrentQuestionnaireToStore()

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

                    updateCurrentQuestionnaireToStore()
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
                        questionnairesListObject.updateQuestionnaire(
                            questionnairesListObject.questionnaireList.indexOf(
                                existingQuestionnaires.get(0)
                            ), wealthGroupQuestionnaire
                        )
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


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun inflateFoodConsumptionNotApplicableModal(foodConsumptionEnum: FoodConsumptionEnum) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(
            R.layout.consumption_not_applicable_modal_layout,
            null
        )
        val close = v.findViewById<TextView>(R.id.close)
        val confirmNotApplicable = v.findViewById<TextView>(R.id.confirmNotApplicable)
        close.setOnClickListener {
            (foodConsumptionNotApplicableDialog as android.app.AlertDialog).cancel()
        }

        confirmNotApplicable.setOnClickListener {
            binding.apply {
                wgPercentFoodConsumptionIncome.apply {
                    if (foodConsumptionEnum == FoodConsumptionEnum.MAIZE_AND_POSHO) {

                        maizeOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        maizeMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        maizeGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true


                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.WHEAT_BARLEY) {
                        wheatOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        wheatMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        wheatGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.SORGHUM) {
                        sorghumOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        sorghumMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        sorghumGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.RICE_AND_PRODUCTS) {
                        riceOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        riceMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        riceGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.BEANS) {
                        beansOwnfarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        beansMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        beansGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.OTHER_PULSES) {
                        pulsesOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        pulsesMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        pulsesGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.VEGETABLES) {
                        vegetablesOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        vegetablesMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        vegetablesGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.FRUITS_AND_BERRIES) {
                        fruitsOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        fruitsMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        fruitsGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.WHITE_ROOTS_AND_TUBERS) {
                        whiteRootsOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        whiteRootsMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        whiteRootsGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.MEAT) {
                        meatOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        meatMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        meatGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.MILK_AND_DAIRY) {
                        milkOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        milkMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        milkGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.FISH_AND_SEA_FOOD) {
                        fishOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        fishOwnMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        fishGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.EGGS) {
                        eggsOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        eggsMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        eggsGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.FATS_AND_OILS) {
                        cookingFatOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        cookingFatMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        cookingFatGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }

                    if (foodConsumptionEnum == FoodConsumptionEnum.SPICES) {
                        spicesOwnFarmCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        spicesMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        spicesGiftCell.background =
                            context?.resources?.getDrawable(R.drawable.cell_shape, null)
                        foodConsumptionHasNoPercentageError = true
                    }
                }
            }
            (foodConsumptionNotApplicableDialog as android.app.AlertDialog).cancel()
        }

        openFoodConsumptionNotApplicableModal(v)
    }

    private fun openFoodConsumptionNotApplicableModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setView(v)
        builder.setCancelable(true)
        foodConsumptionNotApplicableDialog = builder.create()
        (foodConsumptionNotApplicableDialog as android.app.AlertDialog).apply {
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
                            this@WealthGroupDialogFragment,
                            false
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

    fun doesRankItemAlreadyExistInTheRankList(
        rankPosition: Int,
        rankList: MutableList<RankResponseItem>
    ): Boolean {
        for (currentRankItem in rankList) {
            if (currentRankItem.rankPosition == rankPosition) {
                return true
            }
        }
        return false
    }

    override fun onARankItemDiscarded(
        currentResponseItem: WgCropContributionResponseItem,
        position: Int,
        cropContributionEditTypeEnum: CropContributionEditTypeEnum,
        rankNumberValue: Int
    ) {
        if (cropContributionEditTypeEnum == CropContributionEditTypeEnum.CROP_CASH_INCOME_CONTRIBUTION_RANK) {
            if (!doesRankItemAlreadyExistInTheRankList(
                    rankNumberValue,
                    cropCashIncomeContributionRanks
                )
            ) {
                cropCashIncomeContributionRanks.add(
                    RankResponseItem(
                        rankNumberValue,
                        false
                    )
                )
            }
            currentResponseItem.cashIncomeRank.actualValue = 0.0
            currentResponseItem.cashIncomeRank.hasBeenSubmitted = false
        }

        if (cropContributionEditTypeEnum == CropContributionEditTypeEnum.CROP_FOOD_CONSUMPTION_CONTRIBUTION_RANK) {
            if (!doesRankItemAlreadyExistInTheRankList(
                    rankNumberValue,
                    cropCashIncomeContributionRanks
                )
            ) {
                cropFoodConsumptionContributionRanks.add(
                    RankResponseItem(
                        rankNumberValue,
                        false
                    )
                )
            }

            currentResponseItem.foodConsumptionRank.actualValue = 0.0
            currentResponseItem.foodConsumptionRank.hasBeenSubmitted = false
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

    override fun onAnyFieldEdited(
        currentResponseItem: WgCropContributionResponseItem,
        position: Int,
        cropContributionEditTypeEnum: CropContributionEditTypeEnum,
        selectedCashIncomeContributionRank: RankResponseItem?,
        selectedFoodConsumptionContributionRank: RankResponseItem?,
        isAnEditTextField: Boolean
    ) {

        if (cropContributionEditTypeEnum == CropContributionEditTypeEnum.CROP_CASH_INCOME_CONTRIBUTION_RANK) {
            if (selectedCashIncomeContributionRank?.rankPosition != 0) {
                cropCashIncomeContributionRanks.remove(selectedCashIncomeContributionRank)
            }
        }

        if (cropContributionEditTypeEnum == CropContributionEditTypeEnum.CROP_FOOD_CONSUMPTION_CONTRIBUTION_RANK) {
            if (selectedFoodConsumptionContributionRank?.rankPosition != 0) {
                cropFoodConsumptionContributionRanks.remove(selectedFoodConsumptionContributionRank)
            }
        }

        cropContributionResponseItems.set(position, currentResponseItem)

        binding.apply {
            cropProductionLayout.apply {

                if (!isAnEditTextField) {
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
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.cattleIncomeRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.cattle.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.cattle.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.cattleFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.DAIRY_CATTLE) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.dairyCattle.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.dairyCattle.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.dairyCattleIncomeRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.dairyCattle.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.dairyCattle.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.dairyCattleFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }


        if (animalType == WgLivestockTypesEnum.GOATS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.goats.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.goats.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.goatsIncomeRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.goats.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.goats.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.goatsFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.SHEEP) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.sheep.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.sheep.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.sheepCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.sheep.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.sheep.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.sheepFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.DONKEYS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.donkeys.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.donkeys.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.donkeysCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.donkeys.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.donkeys.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.donkeysFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.CAMELS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.camels.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.camels.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.camelsCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.camels.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.camels.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.camelsFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.PIGS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.pigs.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.pigs.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }

                binding.wgLivestockPoultryContribution.pigscashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.pigs.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.pigs.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.pigsFoodrank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.CHICKEN) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.chicken.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.chicken.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.chickenCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.chicken.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.chicken.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.chickenFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.IMPROVED_CHICKEN) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.improvedChicken.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.improvedChicken.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.improvedChickenCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.improvedChicken.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.improvedChicken.consumptionRank.hasBeenSubmitted =
                    true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.improvedChickenFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.DUCKS) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.ducks.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.ducks.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.ducksCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.ducks.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.ducks.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.ducksFoodRank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.BEE_HIVES) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.beeHives.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.beeHives.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.beeHivesCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.beeHives.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.beeHives.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }

                binding.wgLivestockPoultryContribution.beeHivesFoodrank.text =
                    selectedRankItem.rankPosition.toString()
            }
        }

        if (animalType == WgLivestockTypesEnum.FISH_POND) {
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.CASH_CONTRIBUTION) {
                livestockContributionResponses.fishPonds.incomeRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.fishPonds.incomeRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockCashIncomeContributionRanks.remove(selectedRankItem)
                }
                binding.wgLivestockPoultryContribution.fishPondsCashRank.text =
                    selectedRankItem.rankPosition.toString()
            }
            if (livestockContributionRankTypeEnum == LivestockContributionRankTypeEnum.FOOD_CONSUMPTION_CONTRIBUTION) {
                livestockContributionResponses.fishPonds.consumptionRank.actualValue =
                    selectedRankItem.rankPosition.toDouble()
                livestockContributionResponses.fishPonds.consumptionRank.hasBeenSubmitted = true
                if (selectedRankItem.rankPosition != 0) {
                    livestockFoodConsumptionContributionRanks.remove(selectedRankItem)
                }
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

                    if (selectedRankItem.rankPosition != 0) {
                        incomeSourceRanks.remove(selectedRankItem)
                    }

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

                    if (selectedRankItem.rankPosition != 0) {
                        incomeConsumptionRanks.remove(selectedRankItem)
                    }

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

                    if (selectedRankItem.rankPosition != 0) {
                        livestockProductionRanks.remove(selectedRankItem)
                    }

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

                    if (selectedRankItem.rankPosition != 0) {
                        fishingRanks.remove(selectedRankItem)
                    }

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

                    if (selectedRankItem.rankPosition != 0) {
                        naturalResourceRanks.remove(selectedRankItem)
                    }

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

                    if (selectedRankItem.rankPosition != 0) {
                        smallEnterpriesRanks.remove(selectedRankItem)
                    }

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
        if (wealthGroupQuestionnaire.questionnaireStatus != QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION) {
            saveQuestionnaireAsDraft()
        }
        if (wealthGroupQuestionnaire.questionnaireStatus == QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION) {
            updateCompletedQuestionnaire()
        }
    }

    override fun onStop() {
        super.onStop()
        if (wealthGroupQuestionnaire.questionnaireStatus != QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION) {
            saveQuestionnaireAsDraft()
        }
        if (wealthGroupQuestionnaire.questionnaireStatus == QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION) {
            updateCompletedQuestionnaire()
        }
    }

    fun saveQuestionnaireAsDraft() {
        wealthGroupQuestionnaire.questionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE
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
            questionnairesListObject.updateQuestionnaire(
                questionnairesListObject.questionnaireList.indexOf(
                    existingQuestionnaires.get(0)
                ), wealthGroupQuestionnaire
            )
        }
        editor?.remove(Constants.WEALTH_GROUP_LIST_OBJECT)

        val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
        editor?.putString(
            Constants.WEALTH_GROUP_LIST_OBJECT,
            newQuestionnaireObjectString
        )
        editor?.commit()

        confirmDraftIsSaved()

        val intent = Intent()
        intent.action = Constants.QUESTIONNAIRE_COMPLETED
        activity?.applicationContext?.sendBroadcast(intent)
    }


    fun confirmDraftIsSaved() {
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
            AppStore.getInstance().currentWealthGroupQuestionnaire?.let {
                saveQuestionnaireAsDraftFromStore(
                    it
                )
            }
        } else {
            AppStore.getInstance().currentWealthGroupQuestionnaire = null
            return
        }
    }


    fun saveQuestionnaireAsDraftFromStore(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        wealthGroupQuestionnaire.questionnaireStatus = QuestionnaireStatus.DRAFT_QUESTIONNAIRE
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
            questionnairesListObject.updateQuestionnaire(
                questionnairesListObject.questionnaireList.indexOf(
                    existingQuestionnaires.get(0)
                ), wealthGroupQuestionnaire
            )
        }
        editor?.remove(Constants.WEALTH_GROUP_LIST_OBJECT)

        val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
        editor?.putString(
            Constants.WEALTH_GROUP_LIST_OBJECT,
            newQuestionnaireObjectString
        )
        editor?.commit()

        confirmDraftIsSaved()

        val intent = Intent()
        intent.action = Constants.QUESTIONNAIRE_COMPLETED
        activity?.applicationContext?.sendBroadcast(intent)
    }


    fun updateCompletedQuestionnaire() {
        wealthGroupQuestionnaire.questionnaireStatus =
            QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION
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
            questionnairesListObject.updateQuestionnaire(
                questionnairesListObject.questionnaireList.indexOf(
                    existingQuestionnaires.get(0)
                ), wealthGroupQuestionnaire
            )
        }
        editor?.remove(Constants.WEALTH_GROUP_LIST_OBJECT)

        val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
        editor?.putString(
            Constants.WEALTH_GROUP_LIST_OBJECT,
            newQuestionnaireObjectString
        )
        editor?.commit()

        confirmCompletedQuestionnaireIsUpdated()

        val intent = Intent()
        intent.action = Constants.QUESTIONNAIRE_COMPLETED
        activity?.applicationContext?.sendBroadcast(intent)
    }


    fun confirmCompletedQuestionnaireIsUpdated() {
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
            AppStore.getInstance().currentWealthGroupQuestionnaire?.let {
                updateCompletedQuestionnaireFromStore(
                    it
                )
            }
        } else {
            AppStore.getInstance().currentWealthGroupQuestionnaire = null
            return
        }
    }


    fun updateCompletedQuestionnaireFromStore(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        wealthGroupQuestionnaire.questionnaireStatus =
            QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION
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
            questionnairesListObject.updateQuestionnaire(
                questionnairesListObject.questionnaireList.indexOf(
                    existingQuestionnaires.get(0)
                ), wealthGroupQuestionnaire
            )
        }
        editor?.remove(Constants.WEALTH_GROUP_LIST_OBJECT)

        val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
        editor?.putString(
            Constants.WEALTH_GROUP_LIST_OBJECT,
            newQuestionnaireObjectString
        )
        editor?.commit()

        confirmCompletedQuestionnaireIsUpdated()

        val intent = Intent()
        intent.action = Constants.QUESTIONNAIRE_COMPLETED
        activity?.applicationContext?.sendBroadcast(intent)
    }

    fun prepareCropSelectionListView() {
        binding.apply {
            cropSelectionLayout.apply {
                activity?.let { context ->
                    val adapter =
                        CropSelectionListAdapter(
                            context,
                            R.layout.lz_selection_item,
                            crops,
                            this@WealthGroupDialogFragment,
                            false
                        )
                    cropsList.adapter = adapter
                }
            }
        }
    }

    fun populateMainSourcesIncomeAndfood() {
        binding.apply {
            wgIncomeAndFoodSources.apply {
                incomeAndFoodSourceResponses =
                    wealthGroupQuestionnaire.incomeAndFoodSourceResponses
                livestockProduction.setText(incomeAndFoodSourceResponses.livestockProduction.toString())
                pastureFodderProduction.setText(incomeAndFoodSourceResponses.livestockProduction.toString())
                poultryProduction.setText(incomeAndFoodSourceResponses.poultryProduction.toString())
                cashCropProduction.setText(incomeAndFoodSourceResponses.cashCropProduction.toString())
                foodCropProduction.setText(incomeAndFoodSourceResponses.foodCropProduction.toString())
                casualOrWagedLabour.setText(incomeAndFoodSourceResponses.casualOrWagedLabour.toString())
                formalWagedLabour.setText(incomeAndFoodSourceResponses.formalWagedLabour.toString())
                fishing.setText(incomeAndFoodSourceResponses.fishing.toString())
                huntingAndGathering.setText(incomeAndFoodSourceResponses.huntingAndGathering.toString())
                smallBusiness.setText(incomeAndFoodSourceResponses.smallBusiness.toString())
                firewoodOrCharcoal.setText(incomeAndFoodSourceResponses.firewoodOrCharcoal.toString())
                pettyTrading.setText(incomeAndFoodSourceResponses.pettyTrading.toString())
                remittance.setText(incomeAndFoodSourceResponses.remittance.toString())
                bodaboda.setText(incomeAndFoodSourceResponses.bodaboda.toString())
                beeKeeping.setText(incomeAndFoodSourceResponses.beeKeeping.toString())
                sandHarvesting.setText(incomeAndFoodSourceResponses.sandHarvesting.toString())
                other.setText(incomeAndFoodSourceResponses.other.value.toString())
            }
        }
    }

    fun populateFgdParticipants() {
        binding.apply {
            fdgParticipants.apply {
                fdgParticipantsModelList = wealthGroupQuestionnaire.fdgParticipants
                val fgdParticipantAdapter = activity?.let { it1 ->
                    FgdParticipantsAdapter(
                        wealthGroupQuestionnaire.fdgParticipants, this@WealthGroupDialogFragment,
                        it1,
                        true
                    )
                }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                participantsList.layoutManager = gridLayoutManager
                participantsList.hasFixedSize()
                participantsList.adapter = fgdParticipantAdapter

                numberFgdParticipantsConfiguration.isGone = true
                participantsListWrapper.isVisible = true
            }
        }
    }


    fun populateCopingStrategies() {
        binding.apply {
            wgCopingStrategies.apply {
                val copingStrategiesResponses = wealthGroupQuestionnaire.copingStrategiesResponses
                val consumptionBasedStrategies =
                    copingStrategiesResponses.consumptionBasedStrategies
                lessExpensiveFood.setText(consumptionBasedStrategies.lessExpensiveFood.toString())
                reducedFoodQuantity.setText(consumptionBasedStrategies.reducedAdultFoodQuantity.toString())
                borrowedFood.setText(consumptionBasedStrategies.borrowedFood.toString())
                reducedNoMealsPerDay.setText(consumptionBasedStrategies.reducedMealsPerDay.toString())
                reducedMealPortionSize.setText(consumptionBasedStrategies.reducedMealPortionSize.toString())

                val livelihoodBasedStrategies = copingStrategiesResponses.livelihoodBasedStrategies
                soldHouseHoldAssets.setText(livelihoodBasedStrategies.soldHouseHoldAssets.toString())
                reducedNonFoodExpenses.setText(livelihoodBasedStrategies.reducedNonFoodExpense.toString())
                soldProductiveAssets.setText(livelihoodBasedStrategies.soldProductiveAssets.toString())
                spentSavings.setText(livelihoodBasedStrategies.spentSavings.toString())
                borrowedMoneyFromLender.setText(livelihoodBasedStrategies.borrowedMoneyFromLender.toString())
                soldHouseOrLand.setText(livelihoodBasedStrategies.soldHouseOrLand.toString())
                withdrewSchoolChildren.setText(livelihoodBasedStrategies.withdrewSchoolChildren.toString())
                soldFemaleAnimals.setText(livelihoodBasedStrategies.soldFemaleAnimals.toString())
                begging.setText(livelihoodBasedStrategies.begging.toString())
                soldMoreAnimals.setText(livelihoodBasedStrategies.soldMoreAnimals.toString())
                soldMoreAnimals.setText(livelihoodBasedStrategies.soldMoreAnimals.toString())
            }
        }
    }


    fun populateConstraints() {
        binding.apply {
            wgConstraints.apply {
                constraintResponses = wealthGroupQuestionnaire.constraintsResponses
                wagedLabourIncomeConstraintsResponses =
                    constraintResponses.wagedLabourIncomeConstraintsResponses
                cropProductionIncomeConstraintsResponses =
                    constraintResponses.cropProductionIncomeConstraintsResponses
                livestockProductionIncomeConstraintsResponses =
                    constraintResponses.livestockProductionIncomeConstraintsResponses
                fishingIncomeConstraintsResponses =
                    constraintResponses.fishingIncomeConstraintsResponses
                naturalResourceIncomeConstraintsResponses =
                    constraintResponses.naturalResourceIncomeConstraintsResponses
                smallEnterpriseIncomeConstraintsResponses =
                    constraintResponses.smallEnterpriseIncomeConstraintsResponses

                labourLowEducation.text =
                    wagedLabourIncomeConstraintsResponses.lowEducation.toString()
                labourPoorHealth.text = wagedLabourIncomeConstraintsResponses.poorHealth.toString()
                labourFewJobs.text = wagedLabourIncomeConstraintsResponses.fewJobs.toString()
                labourFarmTime.text =
                    wagedLabourIncomeConstraintsResponses.tooMuchFarmTime.toString()
                labourLowWageRates.text =
                    wagedLabourIncomeConstraintsResponses.lowAverageWageRates.toString()

                consumptionHoldings.text =
                    cropProductionIncomeConstraintsResponses.smallLandHoldings.toString()
                consumptionLackOfCredit.text =
                    cropProductionIncomeConstraintsResponses.lackOfCredit.toString()
                consumptionHighInputs.text =
                    cropProductionIncomeConstraintsResponses.highInputCost.toString()
                consumptionLowFertility.text =
                    cropProductionIncomeConstraintsResponses.lowLandFertility.toString()
                consumptionUnreliableWater.text =
                    cropProductionIncomeConstraintsResponses.lackOfReliableWater.toString()
                consumptionLowTechnicalSkills.text =
                    cropProductionIncomeConstraintsResponses.lowTechnicalSkills.toString()
                consumptionLowSeedQuality.text =
                    cropProductionIncomeConstraintsResponses.lowQualitySeed.toString()
                consumptionMarketAccess.text =
                    cropProductionIncomeConstraintsResponses.lackOfMarketAccess.toString()
                consumptionCropPests.text =
                    cropProductionIncomeConstraintsResponses.endemicCropPests.toString()

                livestockProductionPasture.text =
                    livestockProductionIncomeConstraintsResponses.lackOfPasture.toString()
                livestockProductionDrinkingWater.text =
                    livestockProductionIncomeConstraintsResponses.lackOfAnimalDrinkingWater.toString()
                livestockProductionLowYieldingAnimal.text =
                    livestockProductionIncomeConstraintsResponses.lowYieldingAnimal.toString()
                livestockProductionVeterinaryDrugs.text =
                    livestockProductionIncomeConstraintsResponses.costlyVeterinaryDrugs.toString()
                livestockProductionPests.text =
                    livestockProductionIncomeConstraintsResponses.livestockPestsAndDiseases.toString()
                livestockProductionMarket.text =
                    livestockProductionIncomeConstraintsResponses.lackofMarket.toString()
                livestockProductionInsecurity.text =
                    livestockProductionIncomeConstraintsResponses.insecurity.toString()

                fishingLowStocks.text = fishingIncomeConstraintsResponses.lowFishStocks.toString()
                fishingPoorMarket.text = fishingIncomeConstraintsResponses.poorMarket.toString()
                fishingLackOfEquipment.text =
                    fishingIncomeConstraintsResponses.lackOfEquipment.toString()
                fishingCompetition.text =
                    fishingIncomeConstraintsResponses.extremeCompetition.toString()
                fishingLackOfExpertise.text =
                    fishingIncomeConstraintsResponses.lackOfExpertise.toString()
                fishingFishingRights.text =
                    fishingIncomeConstraintsResponses.fishingRightsRestrictions.toString()

                resourceDecline.text =
                    naturalResourceIncomeConstraintsResponses.decliningNaturalResources.toString()
                resourcePopulationPressure.text =
                    naturalResourceIncomeConstraintsResponses.populationPressure.toString()
                resourceRights.text =
                    naturalResourceIncomeConstraintsResponses.naturalresourceExploitationRights.toString()
                resourceLowValue.text =
                    naturalResourceIncomeConstraintsResponses.lowValueNrBasedProducts.toString()

                enterpriseLackOfCapital.text =
                    smallEnterpriseIncomeConstraintsResponses.lackOfCapital.toString()
                enterpriseRedTape.text =
                    smallEnterpriseIncomeConstraintsResponses.tooMuchRedTape.toString()
                enterpriseTaxes.text =
                    smallEnterpriseIncomeConstraintsResponses.tooManyTaxes.toString()
                enterpriseMarketAccess.text =
                    smallEnterpriseIncomeConstraintsResponses.lackOfAccessToMarket.toString()
                enterpriseExpertise.text =
                    smallEnterpriseIncomeConstraintsResponses.lackOfExpertise.toString()
            }
        }
    }

    fun populateSettlementAndMigration() {
        binding.apply {
            wgMigrationPatterns.apply {
                val migrationPatternResponses = wealthGroupQuestionnaire.migrationPatternResponses

                fullyNomadic.setText(migrationPatternResponses.fullyNomadic.toString())
                semiNomadic.setText(migrationPatternResponses.semiNomadic.toString())
                occasionalNomadic.setText(migrationPatternResponses.occasionalNomadic.toString())
                outMigrantLabour.setText(migrationPatternResponses.outMigrantLabour.toString())
                inMigrantLabour.setText(migrationPatternResponses.inMigrantLabour.toString())
                fullySettled.setText(migrationPatternResponses.fullysettled.toString())
                internallyDisplaced.setText(migrationPatternResponses.internallyDisplaced.toString())
            }
        }
    }

    fun populateExpenditurePatterns() {
        binding.apply {
            wgExpenditurePatterns.apply {
                val expenditurePatternsResponses =
                    wealthGroupQuestionnaire.expenditurePatternsResponses
                maizeAndMaizeFlour.setText(expenditurePatternsResponses.maizeAndMaizeFlour.toString())
                otherCereals.setText(expenditurePatternsResponses.otherCereals.toString())
                pulses.setText(expenditurePatternsResponses.pulses.toString())
                rootsAndTubers.setText(expenditurePatternsResponses.rootsAndTubers.toString())
                vegetablesAndFruits.setText(expenditurePatternsResponses.vegetablesAndFruits.toString())
                fishandseaFood.setText(expenditurePatternsResponses.fishandSeaFood.toString())
                meat.setText(expenditurePatternsResponses.meat.toString())
                milk.setText(expenditurePatternsResponses.milk.toString())
                eggs.setText(expenditurePatternsResponses.eggs.toString())
                oilAndFats.setText(expenditurePatternsResponses.oilsAndFats.toString())
                otherFoods.setText(expenditurePatternsResponses.otherFoods.toString())
                schoolFees.setText(expenditurePatternsResponses.schoolFees.toString())
                drugsAndMedicalCare.setText(expenditurePatternsResponses.drugsAndMedicalCare.toString())
                clothingAndBeautyProducts.setText(expenditurePatternsResponses.clothingAndBeautyProducts.toString())
                houseRent.setText(expenditurePatternsResponses.houseRent.toString())
                communicationExpense.setText(expenditurePatternsResponses.communicationExpenses.toString())
                farmInputs.setText(expenditurePatternsResponses.farmInputs.toString())
                livestockDrugs.setText(expenditurePatternsResponses.livestockDrugs.toString())
                waterPurchase.setText(expenditurePatternsResponses.waterPurchase.toString())
                soaps.setText(expenditurePatternsResponses.soaps.toString())
                farrmLabour.setText(expenditurePatternsResponses.farmLabour.toString())
                travelRelatedExpense.setText(expenditurePatternsResponses.travelRelatedExpenses.toString())
                entertainment.setText(expenditurePatternsResponses.leisureAndEntertainment.toString())
                electricityBill.setText(expenditurePatternsResponses.electricityBills.toString())
                socialObligation.setText(expenditurePatternsResponses.socialObligation.toString())
                millingCost.setText(expenditurePatternsResponses.millingCosts.toString())
                cookingFuel.setText(expenditurePatternsResponses.cookingFuel.toString())
                savingsAndInvestment.setText(expenditurePatternsResponses.savingsAndInvestments.toString())
                loanRepayments.setText(expenditurePatternsResponses.loanRepayments.toString())
            }
        }
    }

    fun populateLabourPatterns() {
        binding.apply {
            wgLabourPatterns.apply {
                val labourPatternResponse = wealthGroupQuestionnaire.labourPatternResponses

                ownFarmWomen.setText(labourPatternResponse.ownFarmCropProduction.women.toString())
                ownFarmmen.setText(labourPatternResponse.ownFarmCropProduction.men.toString())

                livestockHusbandryWomen.setText(labourPatternResponse.livestockHusbandry.women.toString())
                livestockHusbandrymen.setText(labourPatternResponse.livestockHusbandry.men.toString())

                transportServicesWomen.setText(labourPatternResponse.transportServices.women.toString())
                transportServicesMen.setText(labourPatternResponse.transportServices.men.toString())

                wagedLabourWomen.setText(labourPatternResponse.wagedLabourOnFarms.women.toString())
                wagedLabourmen.setText(labourPatternResponse.wagedLabourOnFarms.men.toString())

                lowSkilledNonFarmWomen.setText(labourPatternResponse.lowSkilledNonFarmLabour.women.toString())
                lowSkilledNonFarmmen.setText(labourPatternResponse.lowSkilledNonFarmLabour.men.toString())

                skilledLabourWomen.setText(labourPatternResponse.skilledLabour.women.toString())
                skilledLabourmen.setText(labourPatternResponse.skilledLabour.men.toString())

                formalEmploymentWomen.setText(labourPatternResponse.formalEmployment.women.toString())
                formalEmploymentmen.setText(labourPatternResponse.formalEmployment.men.toString())

                huntingAndGatheringWomen.setText(labourPatternResponse.huntingAndGathering.women.toString())
                huntingAndGatheringmen.setText(labourPatternResponse.huntingAndGathering.men.toString())

                fishingWomen.setText(labourPatternResponse.fishing.women.toString())
                fishingmen.setText(labourPatternResponse.fishing.men.toString())

                tradingWomen.setText(labourPatternResponse.trading.women.toString())
                tradingmen.setText(labourPatternResponse.trading.men.toString())

                domesticUnpaidWorkWomen.setText(labourPatternResponse.domesticUnpaidWork.women.toString())
                domesticUnpaidWorkmen.setText(labourPatternResponse.domesticUnpaidWork.men.toString())

                leisureWomen.setText(labourPatternResponse.leisure.women.toString())
                leisuremen.setText(labourPatternResponse.leisure.men.toString())

                sexWorkWomen.setText(labourPatternResponse.commercialSexWork.women.toString())
                sexWorkmen.setText(labourPatternResponse.commercialSexWork.men.toString())

                beggingWomen.setText(labourPatternResponse.begging.women.toString())
                beggingmen.setText(labourPatternResponse.begging.men.toString())

                inactivityWomen.setText(labourPatternResponse.inactivity.women.toString())
                inactivitymen.setText(labourPatternResponse.inactivity.men.toString())
            }
        }
    }

    fun populateLivestockAndPoultryContribution() {
        binding.apply {
            wgLivestockPoultryContribution.apply {
                livestockContributionResponses =
                    wealthGroupQuestionnaire.livestockContributionResponses

                cattleCashPercentage.setText(livestockContributionResponses.cattle.incomePercentage.actualValue.toString())
                cattleFoodPercentage.setText(livestockContributionResponses.cattle.consumptionPercentage.actualValue.toString())

                dairyCattleCashPercentage.setText(livestockContributionResponses.dairyCattle.incomePercentage.actualValue.toString())
                dairyCattleFoodPercentage.setText(livestockContributionResponses.dairyCattle.consumptionPercentage.actualValue.toString())

                goatsIncomePercentage.setText(livestockContributionResponses.goats.incomePercentage.actualValue.toString())
                goatsFoodPercentage.setText(livestockContributionResponses.goats.consumptionPercentage.actualValue.toString())

                sheepCashPercentage.setText(livestockContributionResponses.sheep.incomePercentage.actualValue.toString())
                sheepFoodPercentage.setText(livestockContributionResponses.sheep.consumptionPercentage.actualValue.toString())

                donkeysCashPercentage.setText(livestockContributionResponses.donkeys.incomePercentage.actualValue.toString())
                donkeysFoodPercentage.setText(livestockContributionResponses.donkeys.consumptionPercentage.actualValue.toString())

                pigsCashPercentage.setText(livestockContributionResponses.pigs.incomePercentage.actualValue.toString())
                pigsFoodPercentage.setText(livestockContributionResponses.pigs.consumptionPercentage.actualValue.toString())

                chickenCashPaercentage.setText(livestockContributionResponses.chicken.incomePercentage.actualValue.toString())
                chickenFoodPercentage.setText(livestockContributionResponses.chicken.consumptionPercentage.actualValue.toString())

                improvedChickenCashPaercentage.setText(livestockContributionResponses.improvedChicken.incomePercentage.actualValue.toString())
                improvedChickenFoodPercentage.setText(livestockContributionResponses.improvedChicken.consumptionPercentage.actualValue.toString())

                camelsCashPercentage.setText(livestockContributionResponses.camels.incomePercentage.actualValue.toString())
                camelsFoodPercentage.setText(livestockContributionResponses.camels.consumptionPercentage.actualValue.toString())

                duckscashPercentage.setText(livestockContributionResponses.ducks.incomePercentage.actualValue.toString())
                ducksFoodPercentage.setText(livestockContributionResponses.ducks.consumptionPercentage.actualValue.toString())

                beeHivesCashPercentage.setText(livestockContributionResponses.beeHives.incomePercentage.actualValue.toString())
                beeHivesFoodPercentage.setText(livestockContributionResponses.beeHives.consumptionPercentage.actualValue.toString())

                fishPondscashPercentage.setText(livestockContributionResponses.fishPonds.incomePercentage.actualValue.toString())
                fishPondsFoodPercentage.setText(livestockContributionResponses.fishPonds.consumptionPercentage.actualValue.toString())


                /* Ranking */
                cattleIncomeRank.text =
                    livestockContributionResponses.cattle.incomeRank.actualValue.toInt().toString()
                cattleFoodRank.text =
                    livestockContributionResponses.cattle.consumptionRank.actualValue.toInt()
                        .toString()

                dairyCattleIncomeRank.text =
                    livestockContributionResponses.dairyCattle.incomeRank.actualValue.toInt()
                        .toString()
                dairyCattleFoodRank.text =
                    livestockContributionResponses.dairyCattle.consumptionRank.actualValue.toInt()
                        .toString()

                goatsIncomeRank.text =
                    livestockContributionResponses.goats.incomeRank.actualValue.toInt().toString()
                goatsFoodRank.text =
                    livestockContributionResponses.goats.consumptionRank.actualValue.toInt()
                        .toString()

                sheepCashRank.text =
                    livestockContributionResponses.sheep.incomeRank.actualValue.toInt().toString()
                sheepFoodRank.text =
                    livestockContributionResponses.sheep.consumptionRank.actualValue.toInt()
                        .toString()

                donkeysCashRank.text =
                    livestockContributionResponses.donkeys.incomeRank.actualValue.toInt().toString()
                donkeysFoodRank.text =
                    livestockContributionResponses.donkeys.consumptionRank.actualValue.toInt()
                        .toString()

                camelsCashRank.text =
                    livestockContributionResponses.camels.incomeRank.actualValue.toInt().toString()
                camelsFoodRank.text =
                    livestockContributionResponses.camels.consumptionRank.actualValue.toInt()
                        .toString()

                pigscashRank.text =
                    livestockContributionResponses.pigs.incomeRank.actualValue.toInt().toString()
                pigsFoodrank.text =
                    livestockContributionResponses.pigs.consumptionRank.actualValue.toInt()
                        .toString()

                chickenCashRank.text =
                    livestockContributionResponses.chicken.incomeRank.actualValue.toInt().toString()
                chickenFoodRank.text =
                    livestockContributionResponses.chicken.consumptionRank.actualValue.toInt()
                        .toString()

                improvedChickenCashRank.text =
                    livestockContributionResponses.improvedChicken.incomeRank.actualValue.toInt()
                        .toString()
                improvedChickenFoodRank.text =
                    livestockContributionResponses.improvedChicken.consumptionRank.actualValue.toInt()
                        .toString()

                ducksCashRank.text =
                    livestockContributionResponses.ducks.incomeRank.actualValue.toInt().toString()
                ducksFoodRank.text =
                    livestockContributionResponses.ducks.consumptionRank.actualValue.toInt()
                        .toString()

                beeHivesCashRank.text =
                    livestockContributionResponses.beeHives.incomeRank.actualValue.toInt()
                        .toString()
                beeHivesFoodrank.text =
                    livestockContributionResponses.beeHives.consumptionRank.actualValue.toInt()
                        .toString()

                fishPondsCashRank.text =
                    livestockContributionResponses.fishPonds.incomeRank.actualValue.toInt()
                        .toString()
                fishPondsFoodRank.text =
                    livestockContributionResponses.fishPonds.consumptionRank.actualValue.toInt()
                        .toString()
            }
        }
    }

    fun populateLivestockAndPoultryOwnership() {
        binding.apply {
            wgLivestockPoultryNumbers.apply {
                val livestockPoultryOwnershipResponses =
                    wealthGroupQuestionnaire.livestockPoultryOwnershipResponses

                cattleNumbers.setText(livestockPoultryOwnershipResponses.cattle.toString())
                dairyCattleNumbers.setText(livestockPoultryOwnershipResponses.dairyCattle.toString())
                goatNumbers.setText(livestockPoultryOwnershipResponses.goats.toString())
                sheepNumbers.setText(livestockPoultryOwnershipResponses.sheep.toString())
                donkeyNumbers.setText(livestockPoultryOwnershipResponses.donkeys.toString())
                camelNumbers.setText(livestockPoultryOwnershipResponses.camels.toString())
                pigNumbers.setText(livestockPoultryOwnershipResponses.pigs.toString())
                chickenNumbers.setText(livestockPoultryOwnershipResponses.chicken.toString())
                improvedChickenNumbers.setText(livestockPoultryOwnershipResponses.improvedChicken.toString())
                duckNumbers.setText(livestockPoultryOwnershipResponses.ducks.toString())
                beeHiveNumbers.setText(livestockPoultryOwnershipResponses.beeHives.toString())
                fishPondNumbers.setText(livestockPoultryOwnershipResponses.fishPonds.toString())
            }
        }
    }

    fun populateCropProduction() {
        binding.apply {
            cropProductionLayout.apply {
                cropContributionResponseItems =
                    wealthGroupQuestionnaire.cropContributionResponseItems
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

    fun prepareCropProduction() {
        binding.apply {
            cropProductionLayout.apply {
                cropContributionResponseItems.clear()
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

                if (!doesRankItemAlreadyExistInTheRankList(0, cropCashIncomeContributionRanks)) {
                    cropCashIncomeContributionRanks.add(
                        RankResponseItem(
                            0,
                            false
                        )
                    )
                }

                if (!doesRankItemAlreadyExistInTheRankList(
                        0,
                        cropFoodConsumptionContributionRanks
                    )
                ) {
                    cropFoodConsumptionContributionRanks.add(
                        RankResponseItem(
                            0,
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
            }
        }
    }

    fun populateCropSelectionSection() {
        binding.apply {
            cropSelectionLayout.apply {
                for (currentCrop in wealthGroupQuestionnaire.selectedCrops) {
                    val existingCrop = crops.first {
                        it.cropId == currentCrop.cropId
                    }
                    crops.set(crops.indexOf(existingCrop), currentCrop)
                }
                activity?.let { context ->
                    val adapter =
                        CropSelectionListAdapter(
                            context,
                            R.layout.lz_selection_item,
                            crops,
                            this@WealthGroupDialogFragment,
                            true
                        )
                    cropsList.adapter = adapter
                }
            }
        }
    }

    fun populateFoodConsunptionPercentages() {
        binding.apply {
            wgPercentFoodConsumptionIncome.apply {
                val foodConsumptionResponses = wealthGroupQuestionnaire.foodConsumptionResponses
                maizeOwnFarm.setText(foodConsumptionResponses.maizeAndPosho.ownFarm.toString())
                maizeMarket.setText(foodConsumptionResponses.maizeAndPosho.marketFoodPurchase.toString())
                maizeGift.setText(foodConsumptionResponses.maizeAndPosho.gifts.toString())

                wheatOwnFarm.setText(foodConsumptionResponses.wheatOrBarley.ownFarm.toString())
                wheatMarket.setText(foodConsumptionResponses.wheatOrBarley.marketFoodPurchase.toString())
                wheatGift.setText(foodConsumptionResponses.wheatOrBarley.gifts.toString())

                sorghumOwnFarm.setText(foodConsumptionResponses.sorghumOrMillet.ownFarm.toString())
                sorghumMarket.setText(foodConsumptionResponses.sorghumOrMillet.marketFoodPurchase.toString())
                sorghumGift.setText(foodConsumptionResponses.sorghumOrMillet.gifts.toString())

                riceOwnFarm.setText(foodConsumptionResponses.rice.ownFarm.toString())
                riceMarket.setText(foodConsumptionResponses.rice.marketFoodPurchase.toString())
                riceGift.setText(foodConsumptionResponses.rice.gifts.toString())

                beansOwnfarm.setText(foodConsumptionResponses.beans.ownFarm.toString())
                beansMarket.setText(foodConsumptionResponses.beans.marketFoodPurchase.toString())
                beansGift.setText(foodConsumptionResponses.beans.gifts.toString())

                pulsesOwnFarm.setText(foodConsumptionResponses.pulses.ownFarm.toString())
                pulsesMarket.setText(foodConsumptionResponses.pulses.marketFoodPurchase.toString())
                pulsesGift.setText(foodConsumptionResponses.pulses.gifts.toString())

                vegetablesOwnFarm.setText(foodConsumptionResponses.vegetables.ownFarm.toString())
                vegetablesMarket.setText(foodConsumptionResponses.vegetables.marketFoodPurchase.toString())
                vegetablesGift.setText(foodConsumptionResponses.vegetables.gifts.toString())

                fruitsOwnFarm.setText(foodConsumptionResponses.fruits.ownFarm.toString())
                fruitsMarket.setText(foodConsumptionResponses.fruits.marketFoodPurchase.toString())
                fruitsGift.setText(foodConsumptionResponses.fruits.gifts.toString())

                whiteRootsOwnFarm.setText(foodConsumptionResponses.whiteRoots.ownFarm.toString())
                whiteRootsMarket.setText(foodConsumptionResponses.whiteRoots.marketFoodPurchase.toString())
                whiteRootsGift.setText(foodConsumptionResponses.whiteRoots.gifts.toString())

                meatOwnFarm.setText(foodConsumptionResponses.meat.ownFarm.toString())
                meatMarket.setText(foodConsumptionResponses.meat.marketFoodPurchase.toString())
                meatGift.setText(foodConsumptionResponses.meat.gifts.toString())

                milkOwnFarm.setText(foodConsumptionResponses.milk.ownFarm.toString())
                milkMarket.setText(foodConsumptionResponses.milk.marketFoodPurchase.toString())
                milkGift.setText(foodConsumptionResponses.milk.gifts.toString())

                fishOwnFarm.setText(foodConsumptionResponses.fish.ownFarm.toString())
                fishOwnMarket.setText(foodConsumptionResponses.fish.marketFoodPurchase.toString())
                fishGift.setText(foodConsumptionResponses.fish.gifts.toString())

                eggsOwnFarm.setText(foodConsumptionResponses.eggs.ownFarm.toString())
                eggsMarket.setText(foodConsumptionResponses.eggs.marketFoodPurchase.toString())
                eggsGift.setText(foodConsumptionResponses.eggs.gifts.toString())

                cookingFatOwnFarm.setText(foodConsumptionResponses.cookingFats.ownFarm.toString())
                cookingFatMarket.setText(foodConsumptionResponses.cookingFats.marketFoodPurchase.toString())
                cookingFatGift.setText(foodConsumptionResponses.cookingFats.gifts.toString())

                spicesOwnFarm.setText(foodConsumptionResponses.spices.ownFarm.toString())
                spicesMarket.setText(foodConsumptionResponses.spices.marketFoodPurchase.toString())
                spicesGift.setText(foodConsumptionResponses.spices.gifts.toString())
            }
        }
    }


    fun processUpdatedCropProductionresponses(
        selectedCrops: MutableList<CropModel>,
        currentCropProductionResponses: MutableList<WgCropContributionResponseItem>
    ): MutableList<WgCropContributionResponseItem> {
        binding.apply {
            cropProductionLayout.apply {
                val updatedCropProductionResponses: MutableList<WgCropContributionResponseItem> =
                    ArrayList()
                val newlyAddedResponses: MutableList<WgCropContributionResponseItem> = ArrayList()
                for (crop in selectedCrops) {
                    for (response in currentCropProductionResponses) {
                        if (crop.cropId == response.cropModel.cropId) {
                            updatedCropProductionResponses.add(response)
                        }
                    }
                }


                for (newCrop in AppStore.getInstance().newlySelectedCrops) {
                    newlyAddedResponses.add(
                        WgCropContributionResponseItem(
                            newCrop,
                            CropContributionResponseValue(0.0, false),
                            CropContributionResponseValue(0.0, false),
                            CropContributionResponseValue(0.0, false),
                            CropContributionResponseValue(0.0, false)
                        )
                    )
                }

                AppStore.getInstance().newlySelectedCrops = ArrayList()

                updatedCropProductionResponses.addAll(newlyAddedResponses)
                wealthGroupQuestionnaire.cropContributionResponseItems =
                    updatedCropProductionResponses
                cropContributionResponseItems = updatedCropProductionResponses
                return updatedCropProductionResponses
            }
        }
    }

    fun updateCropProductionPage(responses: MutableList<WgCropContributionResponseItem>) {
        binding.apply {
            cropProductionLayout.apply {

                cropCashIncomeContributionRanks.clear()
                cropFoodConsumptionContributionRanks.clear()

                val extractedCashRankItems: MutableList<RankResponseItem> = ArrayList()
                val extractedFoodRankItems: MutableList<RankResponseItem> = ArrayList()
                for (item in cropContributionResponseItems) {
                    extractedCashRankItems.add(
                        RankResponseItem(
                            item.cashIncomeRank.actualValue.toInt(),
                            false
                        )
                    )
                }
                for (item in cropContributionResponseItems) {
                    extractedFoodRankItems.add(
                        RankResponseItem(
                            item.foodConsumptionRank.actualValue.toInt(),
                            false
                        )
                    )
                }

                for (i in 0..cropContributionResponseItems.size - 1) {
                    if (!doesRankItemAlreadyExistInTheRankList(i + 1, extractedCashRankItems)) {
                        cropCashIncomeContributionRanks.add(
                            RankResponseItem(
                                i + 1,
                                false
                            )
                        )
                    }

                    if (!doesRankItemAlreadyExistInTheRankList(i + 1, extractedFoodRankItems)) {
                        cropFoodConsumptionContributionRanks.add(
                            RankResponseItem(
                                i + 1,
                                false
                            )
                        )
                    }
                }

                if (!doesRankItemAlreadyExistInTheRankList(0, cropCashIncomeContributionRanks)) {
                    cropCashIncomeContributionRanks.add(
                        RankResponseItem(
                            0,
                            false
                        )
                    )
                }

                if (!doesRankItemAlreadyExistInTheRankList(
                        0,
                        cropFoodConsumptionContributionRanks
                    )
                ) {
                    cropFoodConsumptionContributionRanks.add(
                        RankResponseItem(
                            0,
                            false
                        )
                    )
                }

                activity?.let { context ->
                    val adapter =
                        WgCropContributionAdapter(
                            responses,
                            this@WealthGroupDialogFragment,
                            context,
                            cropCashIncomeContributionRanks,
                            cropFoodConsumptionContributionRanks
                        )
                    cropResponseList.adapter = adapter
                }
            }
        }
    }

    fun updateCurrentQuestionnaireToStore() {
        AppStore.getInstance().currentWealthGroupQuestionnaire = wealthGroupQuestionnaire
    }

    private fun inflateIncomeFoodSourcesOthersSpecifyModal() {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.water_sources_others_specify, null)

        val modalTitle = v.findViewById<TextView>(R.id.title)

        modalTitle.text = "Briefly describe the other sources of food and income in this category"

        val submitButton = v.findViewById<TextView>(R.id.submitButton)
        val othersSpecifyDescription = v.findViewById<EditText>(R.id.othersSpecifyDescription)

        othersSpecifyDescription.setText(incomeAndFoodSourceResponses.other.description)

        submitButton.setOnClickListener {
            incomeAndFoodSourceResponses.other.description = othersSpecifyDescription.text.toString()
            (incomeFoodSourcesOthersSpecifyDialog as android.app.AlertDialog).cancel()
        }

        openIncomeFoodSourcesOthersSpecifyModal(v)
    }

    private fun openIncomeFoodSourcesOthersSpecifyModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setView(v)
        builder.setCancelable(true)
        incomeFoodSourcesOthersSpecifyDialog = builder.create()
        (incomeFoodSourcesOthersSpecifyDialog as android.app.AlertDialog).apply {
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