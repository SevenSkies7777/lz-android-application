package com.silasonyango.ndma.ui.county.destinations

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.ui.county.adapters.LzCropProductionRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.adapters.LzMarketTradeRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.adapters.SubCountiesSpinnerAdapter
import com.silasonyango.ndma.ui.county.adapters.SubLocationLzAssignmentRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.model.*
import com.silasonyango.ndma.ui.county.responses.*
import com.silasonyango.ndma.ui.county.viewmodel.CountyLevelViewModel
import com.silasonyango.ndma.util.Util

class CountyLevelFragment : DialogFragment(),
    SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback,
    LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack,
    LzMarketTradeRecyclerViewAdapter.LzMarketTradeRecyclerViewAdapterCallBack {

    private lateinit var countyLevelViewModel: CountyLevelViewModel

    private lateinit var binding: CountyLevelQuestionnaireLayoutBinding

    private lateinit var countyLevelQuestionnaire: CountyLevelQuestionnaire

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    companion object {

        private const val QUESTIONNAIRE_ID = "questionnaireId"

        private const val QUESTIONNAIRE_NAME = "questionnaireName"

        @JvmStatic
        fun newInstance(questionnaireId: String, questionnaireName: String) =
            CountyLevelFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(QUESTIONNAIRE_ID, questionnaireId)
                        putString(QUESTIONNAIRE_NAME, questionnaireName)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            questionnaireId = it.getString(QUESTIONNAIRE_ID)

            questionnaireName = it.getString(QUESTIONNAIRE_NAME)
        }

        countyLevelQuestionnaire =
            questionnaireId?.let {
                questionnaireName?.let { it1 ->
                    CountyLevelQuestionnaire(
                        it,
                        it1
                    )
                }
            }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        countyLevelViewModel =
            ViewModelProvider(this).get(CountyLevelViewModel::class.java)
        binding = CountyLevelQuestionnaireLayoutBinding.inflate(inflater, container, false)
        defineViews()
        return binding.root
    }

    private fun defineViews() {
        defineLzMarketsLayouts()
        defineNavigation()
        binding.apply {


            val sublocationList: MutableList<SubLocationModel> = ArrayList()
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            populateLocationAndPopulationRV(sublocationList)


            val cropModelList: MutableList<CropModel> = ArrayList()
            cropModelList.add(CropModel("Maize", 1))
            cropModelList.add(CropModel("Beans", 2))
            cropModelList.add(CropModel("Potatoes", 3))
            cropModelList.add(CropModel("Tomatoes", 4))
            cropModelList.add(CropModel("Cassava", 5))
            cropModelList.add(CropModel("Rice", 6))
            cropModelList.add(CropModel("Mango", 7))
            populateCropProductionRecyclerView(cropModelList)
        }
    }

    private fun populateLocationAndPopulationRV(subLocationsList: MutableList<SubLocationModel>) {
        binding.apply {
            locationAndPopulationLayout.apply {
                val subLocationsRecyclerViewAdapter =
                    activity?.let {
                        SubLocationLzAssignmentRecyclerViewAdapter(
                            it,
                            subLocationsList,
                            this@CountyLevelFragment
                        )
                    }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                sublocationLzAssignmentRV.layoutManager = gridLayoutManager
                sublocationLzAssignmentRV.hasFixedSize()
                sublocationLzAssignmentRV.adapter =
                    subLocationsRecyclerViewAdapter
            }
        }
    }

    private fun populateCropProductionRecyclerView(cropModelList: MutableList<CropModel>) {
        binding.apply {
            cropProductionLayout.apply {
                val lzCropProductionRecyclerViewAdapter =
                    activity?.let {
                        LzCropProductionRecyclerViewAdapter(
                            it,
                            cropModelList,
                            this@CountyLevelFragment
                        )
                    }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                lzCropProductionRV.layoutManager = gridLayoutManager
                lzCropProductionRV.hasFixedSize()
                lzCropProductionRV.adapter =
                    lzCropProductionRecyclerViewAdapter
            }
        }
    }

    private fun defineLzMarketsLayouts() {
        var currentlySelectedSubCounty: SubCountyModel? = null
        binding.apply {
            lzMarkets.apply {
                val subCounties: MutableList<SubCountyModel> = ArrayList()
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))

                val spinnerAdapter = context?.let {
                    SubCountiesSpinnerAdapter(
                        it,
                        R.layout.spinner_item_layout,
                        R.id.tvSpinnerItemText,
                        subCounties
                    )
                }


                subCountySpinner.adapter = spinnerAdapter
                subCountySpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            currentlySelectedSubCounty =
                                parent.getItemAtPosition(position) as SubCountyModel

                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }

                addEditText.setOnClickListener {
                    val etMarketName = EditText(context)
                    etMarketName.hint = "Market Name"
                    villageEditTextContainer.addView(etMarketName)
                }

                submitMarkets.setOnClickListener {
                    val countyLevelQuestionnaire =
                        AppStore.getInstance().countyLevelQuestionnairesList.filter {
                            it.uniqueId == questionnaireId
                        }

                    val nearestVillageOrTown = NearestVillageOrTown(
                        Util.generateUniqueId(),
                        etNearestVillageOrTown.text.toString()
                    )

                    val marketList: MutableList<MarketModel> = ArrayList()

                    for (i in 1..villageEditTextContainer.size) {
                        var text = (villageEditTextContainer.get(i) as EditText).text.toString()
                        marketList.add(
                            MarketModel(
                                text,
                                nearestVillageOrTown.townUniqueId,
                                currentlySelectedSubCounty!!.subCountyCode
                            )
                        )
                    }

//                    countyLevelQuestionnaire.get(0).subCountyMarkets?.marketModelList?.addAll(marketList)
//                    System.out.println()
                }



                marketNextButton.setOnClickListener {

                }

            }


        }
    }

    private fun populateLzMarketTradeRecyclerViewAdapter(marketList: MutableList<MarketModel>) {
        binding.apply {
            lzMarketTransactions.apply {
                val lzMarketTradeRecyclerViewAdapter =
                    activity?.let {
                        LzMarketTradeRecyclerViewAdapter(
                            it,
                            marketList,
                            this@CountyLevelFragment
                        )
                    }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                lzMarketTradeRV.layoutManager = gridLayoutManager
                lzMarketTradeRV.hasFixedSize()
                lzMarketTradeRV.adapter =
                    lzMarketTradeRecyclerViewAdapter
            }
        }
    }

    private fun defineNavigation() {
        binding.apply {

            lzDefinitionsLayout.apply {
                lzDefinitionsNextButton.setOnClickListener {
                    lzDefinitionsLayout.root.visibility = View.GONE
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                }
            }

            /*Location and population navigation buttons*/
            locationAndPopulationLayout.apply {
                locationNextButton.setOnClickListener {
                    val wealthGroupResponse = WealthGroupResponse(
                        etVerPoorResponse.text.toString().toDouble(),
                        etPoorResponse.text.toString().toDouble(),
                        etMediumResponse.text.toString().toDouble(),
                        etBetterOffResponse.text.toString().toDouble()
                    )
                    countyLevelQuestionnaire.wealthGroupResponse = wealthGroupResponse
                    locationAndPopulationLayout.root.visibility = View.GONE
                    cropProductionLayout.root.visibility = View.VISIBLE
                }
                locationBackButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.GONE
                    lzDefinitionsLayout.root.visibility = View.VISIBLE
                }
            }

            /*Crop Production navigation buttons*/
            cropProductionLayout.apply {
                cropProductionNextButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    cropProductionLayout.root.visibility = View.GONE
                }
                cropProductionBackButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                    cropProductionLayout.root.visibility = View.GONE
                }
            }

            /*Water source navigation buttons*/
            mainWaterSource.apply {
                waterSourceBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.GONE
                    cropProductionLayout.root.visibility = View.VISIBLE
                }
                waterSourceNextButton.setOnClickListener {

                    val waterSourceResponses = WaterSourcesResponses()
                    waterSourceResponses.rivers = WaterDependenceResponseItem(
                        riversWetSeason.text.toString().toDouble(),
                        riversDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.traditionalRiversWells = WaterDependenceResponseItem(
                        traditionalRiversWellsWetSeason.text.toString().toDouble(),
                        traditionalRiversWellsDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.naturalPonds = WaterDependenceResponseItem(
                        naturalPondsWetSeason.text.toString().toDouble(),
                        naturalPondsDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.pansAndDams = WaterDependenceResponseItem(
                        pansAndDamsWetSeason.text.toString().toDouble(),
                        pansAndDamsDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.shallowWells = WaterDependenceResponseItem(
                        shallowWellsWetSeason.text.toString().toDouble(),
                        shallowWellsDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.boreholes = WaterDependenceResponseItem(
                        boreHolesWetSeason.text.toString().toDouble(),
                        boreHolesDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.springs = WaterDependenceResponseItem(
                        springsWetSeason.text.toString().toDouble(),
                        springsDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.lakes = WaterDependenceResponseItem(
                        lakesWetSeason.text.toString().toDouble(),
                        lakesDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.rockCatchments = WaterDependenceResponseItem(
                        rockCatchmentWetSeason.text.toString().toDouble(),
                        rockCatchmentDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.pipedWater = WaterDependenceResponseItem(
                        pipedWaterWetSeason.text.toString().toDouble(),
                        pipedWaterDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.waterTrucking = WaterDependenceResponseItem(
                        waterTruckingWetSeason.text.toString().toDouble(),
                        waterTruckingDrySeason.text.toString().toDouble()
                    )

                    waterSourceResponses.roofCatchments = WaterDependenceResponseItem(
                        roofCatchmentWetSeason.text.toString().toDouble(),
                        roofCatchmentDrySeason.text.toString().toDouble()
                    )

                    countyLevelQuestionnaire.waterSourceResponses = waterSourceResponses

                    mainWaterSource.root.visibility = View.GONE
                    lzHungerPatterns.root.visibility = View.VISIBLE
                }
            }


            /*Crop Production navigation buttons*/
            lzHungerPatterns.apply {
                hungerPatternsBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    lzHungerPatterns.root.visibility = View.GONE
                }
                hungerPatternsNextButton.setOnClickListener {
                    countyLevelQuestionnaire.hungerPatternsResponses = HungerPatternsResponses(
                        etLongRainsHungerPeriod.text.toString().toDouble(),
                        etEndLongBeginShortRainsHungerPeriod.text.toString().toDouble(),
                        etShortRainsHungerPeriod.text.toString().toDouble(),
                        etEndShortBeginLongRainsHungerPeriod.text.toString().toDouble()
                    )
                    lzHazards.root.visibility = View.VISIBLE
                    lzHungerPatterns.root.visibility = View.GONE
                }
            }


            /*Hazards navigation*/
            lzHazards.apply {
                hazardBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    lzHazards.root.visibility = View.GONE
                }
                hazardNextButton.setOnClickListener {

                    val hazardResponses = HazardResponses()

                    hazardResponses.animalRustling = HazardResponseItem(
                        animalRustlingRank.text.toString().toInt(),
                        animalRustlingNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.banditry = HazardResponseItem(
                        banditryRank.text.toString().toInt(),
                        banditryNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.terrorism = HazardResponseItem(
                        terrorismRank.text.toString().toInt(),
                        terrorismNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.ethnicConflict = HazardResponseItem(
                        ethicConflictRank.text.toString().toInt(),
                        ethicConflictNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.politicalViolence = HazardResponseItem(
                        politicalViolenceRank.text.toString().toInt(),
                        politicalViolenceNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.livestockPestsAndDiseases = HazardResponseItem(
                        pestAndDiseaseRank.text.toString().toInt(),
                        pestAndDiseaseNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.hailstormsOrFrost = HazardResponseItem(
                        hailstormsOrFrostRank.text.toString().toInt(),
                        hailstormsOrFrostNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.flooding = HazardResponseItem(
                        floodingRank.text.toString().toInt(),
                        floodingNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.landslides = HazardResponseItem(
                        landslidesRank.text.toString().toInt(),
                        landslidesNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.highWindsOrCyclones = HazardResponseItem(
                        windsOrCycloneRank.text.toString().toInt(),
                        windsOrCycloneNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.bushFires = HazardResponseItem(
                        bushFiresRank.text.toString().toInt(),
                        bushFiresRank.text.toString().toDouble()
                    )

                    hazardResponses.cropPests = HazardResponseItem(
                        cropPestsRank.text.toString().toInt(),
                        cropPestsNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.locustInvasion = HazardResponseItem(
                        locustInvasionRank.text.toString().toInt(),
                        locustInvasionNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.cropDiseases = HazardResponseItem(
                        cropDiseasesRank.text.toString().toInt(),
                        cropDiseasesNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.terminalIllnesses = HazardResponseItem(
                        terminalIllnessRank.text.toString().toInt(),
                        terminalIllnessNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.malariaPowerOutBreak = HazardResponseItem(
                        malariaOutbreakRank.text.toString().toInt(),
                        malariaOutbreakNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.malariaPowerOutBreak = HazardResponseItem(
                        waterBorneDiseaseRank.text.toString().toInt(),
                        waterBorneDiseaseNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.waterBornDiseases = HazardResponseItem(
                        humanWildlifeConflictRank.text.toString().toInt(),
                        humanWildlifeConflictNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.humanWildlifeConflict = HazardResponseItem(
                        humanWildlifeConflictRank.text.toString().toInt(),
                        humanWildlifeConflictNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.highFoodPrices = HazardResponseItem(
                        highFoodPriceRank.text.toString().toInt(),
                        highFoodPriceNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.marketFoodShortages = HazardResponseItem(
                        foodShortageRank.text.toString().toInt(),
                        foodShortageNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.drinkingWaterShortages = HazardResponseItem(
                        drinkingWaterShortageRank.text.toString().toInt(),
                        drinkingWaterShortageNoOfYears.text.toString().toDouble()
                    )

                    hazardResponses.others = HazardResponseItem(
                        othersRank.text.toString().toInt(),
                        othersNoOfYears.text.toString().toDouble()
                    )

                    countyLevelQuestionnaire.hazardResponses = hazardResponses

                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                    lzHazards.root.visibility = View.GONE
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(matchParent, matchParent)
            window?.setBackgroundDrawable(null)
        }
    }
}