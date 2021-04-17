package com.silasonyango.ndma.ui.wealthgroup


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.databinding.WealthGroupQuestionnaireLayoutBinding
import com.silasonyango.ndma.ui.county.model.QuestionnaireSessionLocation
import com.silasonyango.ndma.ui.wealthgroup.responses.*
import com.silasonyango.ndma.util.Util

class WealthGroupDialogFragment : DialogFragment() {

    private lateinit var wealthGroupViewModel: WealthGroupViewModel

    private lateinit var binding: WealthGroupQuestionnaireLayoutBinding

    private lateinit var wealthGroupQuestionnaire: WealthGroupQuestionnaire

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    var questionnaireSessionLocation: QuestionnaireSessionLocation? = null

    private var subContyDialog: AlertDialog? = null

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
            wealthGroupQuestionnaire.questionnaireName = AppStore.getInstance().sessionDetails?.geography?.county?.countyName + "county "+
                wealthGroupQuestionnaire.questionnaireGeography.selectedLivelihoodZone.livelihoodZoneName + "Livelihood Zone " + wealthGroupQuestionnaire.questionnaireGeography.selectedWealthGroup.wealthGroupName + "questionnaire"
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
        binding = WealthGroupQuestionnaireLayoutBinding.inflate(inflater, container, false)
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
                foodSourcesPercentNextButton.setOnClickListener {

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

                    wgPercentFoodConsumptionIncome.root.visibility = View.GONE
                    wgLivestockPoultryNumbers.root.visibility = View.VISIBLE
                }

                foodSourcesPercentBackButton.setOnClickListener {
                    wgPercentFoodConsumptionIncome.root.visibility = View.GONE
                    wgIncomeAndFoodSources.root.visibility = View.VISIBLE
                }
            }


            /*Livestock and poultry navigation*/
            wgLivestockPoultryNumbers.apply {
                livestockPoultryNumbertsBackButton.setOnClickListener {
                    wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE
                    wgLivestockPoultryNumbers.root.visibility = View.GONE
                }

                livestockPoultryNumbertsNextButton.setOnClickListener {

                    val livestockPoultryOwnershipResponses = LivestockPoultryOwnershipResponses()
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
                    livestockPoultryOwnershipResponses.pigs = pigNumbers.text.toString().toDouble()
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
                labourPatternsBackButton.setOnClickListener {
                    wgLivestockPoultryContribution.root.visibility = View.VISIBLE
                    wgLabourPatterns.root.visibility = View.GONE
                }

                labourPatternsNextButton.setOnClickListener {

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


            /*Expenditure patterns navigation */
            wgExpenditurePatterns.apply {
                expenditurePatternsBackButton.setOnClickListener {
                    wgLabourPatterns.root.visibility = View.VISIBLE
                    wgExpenditurePatterns.root.visibility = View.GONE
                }

                expenditurePatternsNextButton.setOnClickListener {

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
                    expenditurePatternsResponses.oilsAndFats = oilAndFats.text.toString().toDouble()
                    expenditurePatternsResponses.otherFoods = otherFoods.text.toString().toDouble()
                    expenditurePatternsResponses.schoolFees = schoolFees.text.toString().toDouble()
                    expenditurePatternsResponses.drugsAndMedicalCare =
                        drugsAndMedicalCare.text.toString().toDouble()
                    expenditurePatternsResponses.clothingAndBeautyProducts =
                        clothingAndBeautyProducts.text.toString().toDouble()
                    expenditurePatternsResponses.houseRent = houseRent.text.toString().toDouble()
                    expenditurePatternsResponses.communicationExpenses =
                        communicationExpense.text.toString().toDouble()
                    expenditurePatternsResponses.farmInputs = farmInputs.text.toString().toDouble()
                    expenditurePatternsResponses.livestockDrugs =
                        livestockDrugs.text.toString().toDouble()
                    expenditurePatternsResponses.waterPurchase =
                        waterPurchase.text.toString().toDouble()
                    expenditurePatternsResponses.soaps = soaps.text.toString().toDouble()
                    expenditurePatternsResponses.farmLabour = farrmLabour.text.toString().toDouble()
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


            /*Migration patterns */
            wgMigrationPatterns.apply {
                migrationPatternsBackButton.setOnClickListener {
                    wgExpenditurePatterns.root.visibility = View.VISIBLE
                    wgMigrationPatterns.root.visibility = View.GONE
                }

                migrationPatternsNextButton.setOnClickListener {

                    val migrationPatternResponses = MigrationPatternResponses()

                    migrationPatternResponses.fullyNomadic = fullyNomadic.text.toString().toDouble()
                    migrationPatternResponses.semiNomadic = semiNomadic.text.toString().toDouble()
                    migrationPatternResponses.occasionalNomadic =
                        occasionalNomadic.text.toString().toDouble()
                    migrationPatternResponses.outMigrantLabour =
                        outMigrantLabour.text.toString().toDouble()
                    migrationPatternResponses.inMigrantLabour =
                        inMigrantLabour.text.toString().toDouble()
                    migrationPatternResponses.fullysettled = fullySettled.text.toString().toDouble()
                    migrationPatternResponses.internallyDisplaced =
                        internallyDisplaced.text.toString().toDouble()

                    wealthGroupQuestionnaire.migrationPatternResponses = migrationPatternResponses

                    wgConstraints.root.visibility = View.VISIBLE
                    wgMigrationPatterns.root.visibility = View.GONE
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
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (labourPoorHealth.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourPoorHealthCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (labourFewJobs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourFewJobsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (labourFarmTime.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourFarmTimeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (labourLowWageRates.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        labourLowWageRatesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }

                    if (consumptionHoldings.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionHoldingsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionLackOfCredit.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLackOfCreditCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }

                    if (consumptionHighInputs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionHighInputsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionLowFertility.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLowFertilityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionUnreliableWater.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionUnreliableWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionLowTechnicalSkills.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLowTechnicalSkillsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionLowSeedQuality.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionLowSeedQualityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionMarketAccess.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (consumptionCropPests.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        consumptionCropPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionPasture.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionPastureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionDrinkingWater.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionDrinkingWaterCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionLowYieldingAnimal.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionLowYieldingAnimalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionVeterinaryDrugs.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionVeterinaryDrugsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionPests.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionPestsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (livestockProductionInsecurity.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        livestockProductionInsecurityCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (fishingLowStocks.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingLowStocksCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (fishingPoorMarket.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingPoorMarketCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (fishingLackOfEquipment.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingLackOfEquipmentCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (fishingCompetition.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingCompetitionCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (fishingLackOfExpertise.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingLackOfExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (fishingFishingRights.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        fishingFishingRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (resourceDecline.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourceDeclineCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (resourcePopulationPressure.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourcePopulationPressureCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (resourceRights.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourceRightsCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (resourceLowValue.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        resourceLowValueCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (enterpriseLackOfCapital.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseLackOfCapitalCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (enterpriseRedTape.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseRedTapeCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (enterpriseTaxes.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseTaxesCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (enterpriseMarketAccess.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseMarketAccessCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }
                    if (enterpriseExpertise.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        enterpriseExpertiseCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell,null)
                    }


                    if (hasNoValidationError) {

                        val constraintResponses = ConstraintsResponses()

                        val wagedLabourIncomeConstraintsResponses =
                            WagedLabourIncomeConstraintsResponses()

                        wagedLabourIncomeConstraintsResponses.lowEducation =
                            labourLowEducation.text.toString().toInt()
                        wagedLabourIncomeConstraintsResponses.poorHealth =
                            labourPoorHealth.text.toString().toInt()
                        wagedLabourIncomeConstraintsResponses.fewJobs = labourFewJobs.text.toString().toInt()
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
                    intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                    activity?.sendBroadcast(intent)

                    this@WealthGroupDialogFragment.dismiss()

                }
            }
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
}