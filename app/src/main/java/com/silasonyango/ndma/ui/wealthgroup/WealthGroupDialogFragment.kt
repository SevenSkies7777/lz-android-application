package com.silasonyango.ndma.ui.wealthgroup


import android.content.Context
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.silasonyango.ndma.R
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

                    wealthGroupQuestionnaire.expenditurePatternsResponses = expenditurePatternsResponses

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
                    migrationPatternResponses.occasionalNomadic = occasionalNomadic.text.toString().toDouble()
                    migrationPatternResponses.outMigrantLabour = outMigrantLabour.text.toString().toDouble()
                    migrationPatternResponses.inMigrantLabour = inMigrantLabour.text.toString().toDouble()
                    migrationPatternResponses.fullysettled = fullySettled.text.toString().toDouble()
                    migrationPatternResponses.internallyDisplaced = internallyDisplaced.text.toString().toDouble()

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


                    wgCompletionPage.root.visibility = View.VISIBLE
                    wgConstraints.root.visibility = View.GONE
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