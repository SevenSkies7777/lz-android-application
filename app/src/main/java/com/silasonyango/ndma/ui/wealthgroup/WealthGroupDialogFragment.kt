package com.silasonyango.ndma.ui.wealthgroup


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
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
import com.silasonyango.ndma.ui.wealthgroup.responses.FoodConsumptionResponseItem
import com.silasonyango.ndma.ui.wealthgroup.responses.FoodConsumptionResponses
import com.silasonyango.ndma.ui.wealthgroup.responses.IncomeAndFoodSourceResponses

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
        fun newInstance(questionnaireId: String, questionnaireName: String, questionnaireSessionLocation: QuestionnaireSessionLocation) =
            WealthGroupDialogFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(QUESTIONNAIRE_ID, questionnaireId)
                        putString(QUESTIONNAIRE_NAME, questionnaireName)
                        putParcelable(QUESTIONNAIRE_SESSION_LOCATION, questionnaireSessionLocation)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            questionnaireId = it.getString(QUESTIONNAIRE_ID)

            questionnaireName = it.getString(QUESTIONNAIRE_NAME)

            questionnaireSessionLocation = it.getParcelable<QuestionnaireSessionLocation>(QUESTIONNAIRE_SESSION_LOCATION)

            wealthGroupQuestionnaire =
                questionnaireId?.let { it1 ->
                    questionnaireName?.let { it2 ->
                        WealthGroupQuestionnaire(
                            it1,
                            it2
                        )
                    }
                }!!

            wealthGroupQuestionnaire.questionnaireGeography = this!!.questionnaireSessionLocation!!
        }

        inflateSubCountyModal()
    }

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

                    wealthGroupQuestionnaire.incomeAndFoodSourceResponses = incomeAndFoodSourceResponses

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
                    wgLabourPatterns.root.visibility = View.VISIBLE
                    wgLivestockPoultryNumbers.root.visibility = View.GONE
                }
            }

            /*Labour patterns navigation */
            wgLabourPatterns.apply {
                labourPatternsBackButton.setOnClickListener {
                    wgLivestockPoultryContribution.root.visibility = View.VISIBLE
                    wgLabourPatterns.root.visibility = View.GONE
                }

                labourPatternsNextButton.setOnClickListener {
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
                    editor?.putString(Constants.WEALTH_GROUP_LIST_OBJECT, newQuestionnaireObjectString)
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