package com.silasonyango.ndma.ui.wealthgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.silasonyango.ndma.R
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.databinding.WealthGroupQuestionnaireLayoutBinding

class WealthGroupDialogFragment : DialogFragment() {

    private lateinit var wealthGroupViewModel: WealthGroupViewModel

    private lateinit var binding: WealthGroupQuestionnaireLayoutBinding

    var questionnaireId: String? = null

    companion object {

        private const val QUESTIONNAIRE_ID = "questionnaireId"

        @JvmStatic
        fun newInstance(questionnaireId: String) =
                WealthGroupDialogFragment()
                        .apply {
                            arguments = Bundle().apply {
                                putString(QUESTIONNAIRE_ID,questionnaireId)
                            }
                        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            questionnaireId = it.getString(QUESTIONNAIRE_ID)
        }
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
                    wgIncomeAndFoodSources.root.visibility = View.GONE
                    wgPercentFoodConsumptionIncome.root.visibility = View.VISIBLE
                }
            }

            /*Income and food sources percentages navigation
            * todo: Change this navigation to crop production question
            * */

            wgPercentFoodConsumptionIncome.apply {
                foodSourcesPercentNextButton.setOnClickListener {
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
            }
        }
    }
}