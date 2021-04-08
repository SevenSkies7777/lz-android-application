package com.silasonyango.ndma.ui.county.destinations

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.login.model.GeographyObject
import com.silasonyango.ndma.ui.county.adapters.LzCropProductionRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.adapters.LzMarketTradeRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.adapters.SubCountiesSpinnerAdapter
import com.silasonyango.ndma.ui.county.adapters.SubLocationLzAssignmentRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.model.*
import com.silasonyango.ndma.ui.county.responses.*
import com.silasonyango.ndma.ui.county.viewmodel.CountyLevelViewModel
import com.silasonyango.ndma.ui.home.HomeViewModel
import com.silasonyango.ndma.ui.home.adapters.*
import com.silasonyango.ndma.ui.wealthgroup.WealthGroupDialogFragment
import com.silasonyango.ndma.util.GpsTracker
import com.silasonyango.ndma.util.Util


class CountyLevelFragment : DialogFragment(),
    SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback,
    LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack,
    LzMarketTradeRecyclerViewAdapter.LzMarketTradeRecyclerViewAdapterCallBack,
    LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack,
    LzSelectionAdapter.LzSelectionAdapterCallBack,
    SubLocationZoneAssignmentAdapter.SubLocationZoneAssignmentAdapterCallBack,
    CropSelectionAdapter.CropSelectionAdapterCallBack,
    TribeSelectionAdapter.TribeSelectionAdapterCallBack, EthnicityAdapter.EthnicityAdapterCallBack {

    private lateinit var countyLevelViewModel: CountyLevelViewModel

    private lateinit var binding: CountyLevelQuestionnaireLayoutBinding

    private lateinit var countyLevelQuestionnaire: CountyLevelQuestionnaire

    lateinit var geographyObject: GeographyObject

    private var livelihoodZoneAlertDialog: android.app.AlertDialog? = null

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    val WRITE_STORAGE_PERMISSION_CODE: Int = 100


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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineViews() {
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
        defineLzMarketsLayouts()
        defineNavigation()
        binding.apply {


            val sublocationList: MutableList<SubLocationModel> = ArrayList()

            populateLocationAndPopulationRV(sublocationList)


            val cropModelList: MutableList<CropModel> = ArrayList()
            cropModelList.add(CropModel(0, "Maize", 1))
            cropModelList.add(CropModel(0, "Beans", 2))
            cropModelList.add(CropModel(0, "Potatoes", 3))
            cropModelList.add(CropModel(0, "Tomatoes", 4))
            cropModelList.add(CropModel(0, "Cassava", 5))
            cropModelList.add(CropModel(0, "Rice", 6))
            cropModelList.add(CropModel(0, "Mango", 7))
            populateCropProductionRecyclerView(cropModelList)

            countyConfiguration.apply {

                livelihoodZoneDropDown.setOnClickListener {
                    inflateLivelihoodZoneModal(geographyObject.livelihoodZones)
                }

                configurationSubmitButton.setOnClickListener {
                    var latitude: Double = 0.0
                    var longitude: Double = 0.0
                    val gpsTracker: GpsTracker = GpsTracker(context)
                    if (isStoragePermissionGranted()) {
                        latitude = gpsTracker.latitude
                        longitude = gpsTracker.longitude
                        countyLevelQuestionnaire.latitude = latitude
                        countyLevelQuestionnaire.longitude = longitude

                        countyLevelQuestionnaire.questionnaireStartDate = Util.getNow()

                        prepareLivelihoodSelectionLayout()
                        countyConfiguration.root.visibility = View.GONE
                        livelihoodZoneSelection.root.visibility = View.VISIBLE
                    }
                }
            }

        }
    }

    private fun prepareLivelihoodSelectionLayout() {
        binding.apply {

            livelihoodZoneSelection.apply {

                val lzSelectionAdapter = LzSelectionAdapter(
                    geographyObject.livelihoodZones,
                    this@CountyLevelFragment
                )
                val gridLayoutManager = GridLayoutManager(context, 1)
                lzList.layoutManager = gridLayoutManager
                lzList.hasFixedSize()
                lzList.adapter = lzSelectionAdapter

            }

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
//                subCounties.add(SubCountyModel("Laikipia", 0))
//                subCounties.add(SubCountyModel("Laikipia", 0))
//                subCounties.add(SubCountyModel("Laikipia", 0))
//                subCounties.add(SubCountyModel("Laikipia", 0))
//                subCounties.add(SubCountyModel("Laikipia", 0))
//                subCounties.add(SubCountyModel("Laikipia", 0))

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineNavigation() {
        binding.apply {

            /* Livelihood Zone Selection navigation */
            livelihoodZoneSelection.apply {
                lzSelectionBackButton.setOnClickListener {
                    countyConfiguration.root.visibility = View.VISIBLE
                    livelihoodZoneSelection.root.visibility = View.GONE
                }

                lzSelectionNextButton.setOnClickListener {

                    val subLocationZoneAssignmentModelList: MutableList<SubLocationZoneAssignmentModel> =
                        ArrayList()

                    for (currentSubLocation in geographyObject.subLocations) {
                        subLocationZoneAssignmentModelList.add(
                            SubLocationZoneAssignmentModel(
                                currentSubLocation,
                                0
                            )
                        )
                    }

                    lzSubLocationAssignment.apply {
                        val subLocationassignmentAdapter = activity?.let { it1 ->
                            SubLocationZoneAssignmentAdapter(
                                subLocationZoneAssignmentModelList,
                                this@CountyLevelFragment,
                                geographyObject.livelihoodZones,
                                it1
                            )
                        }

                        val gridLayoutManager = GridLayoutManager(activity, 1)
                        listRv.layoutManager = gridLayoutManager
                        listRv.hasFixedSize()
                        listRv.adapter =
                            subLocationassignmentAdapter
                    }
                    lzSubLocationAssignment.root.visibility = View.VISIBLE
                    livelihoodZoneSelection.root.visibility = View.GONE
                }
            }


            /* Lz Sublocation assignment navigation */
            lzSubLocationAssignment.apply {

                lzAllocationBackButton.setOnClickListener {
                    lzSubLocationAssignment.root.visibility = View.GONE
                    livelihoodZoneSelection.root.visibility = View.VISIBLE
                }

                lzAllocationNextButton.setOnClickListener {
                    lzSubLocationAssignment.root.visibility = View.GONE
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                }

            }

            /*Location and population navigation buttons*/
            locationAndPopulationLayout.apply {
                locationNextButton.setOnClickListener {

                    val wealthGroupResponse = WealthGroupResponse(
                        returnZeroStringIfEmpty(etVerPoorResponse.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etPoorResponse.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etMediumResponse.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etBetterOffResponse.text.toString()).toDouble()
                    )
                    countyLevelQuestionnaire.wealthGroupResponse = wealthGroupResponse

                    val cropModelList: MutableList<CropModel> = ArrayList()
                    cropModelList.add(CropModel(0, "Maize", 1))
                    cropModelList.add(CropModel(0, "Beans", 2))
                    cropModelList.add(CropModel(0, "Potatoes", 3))
                    cropModelList.add(CropModel(0, "Tomatoes", 4))
                    cropModelList.add(CropModel(0, "Cassava", 5))
                    cropModelList.add(CropModel(0, "Rice", 6))
                    cropModelList.add(CropModel(0, "Mango", 7))

                    val cropSelectionAdapter =
                        CropSelectionAdapter(cropModelList, this@CountyLevelFragment)
                    val gridLayoutManager = GridLayoutManager(activity, 1)

                    cropSelectionLayout.apply {
                        cropsList.layoutManager = gridLayoutManager
                        cropsList.hasFixedSize()
                        cropsList.adapter =
                            cropSelectionAdapter
                    }

                    locationAndPopulationLayout.root.visibility = View.GONE
                    cropSelectionLayout.root.visibility = View.VISIBLE
                }
                locationBackButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.GONE
                    lzSubLocationAssignment.root.visibility = View.VISIBLE
                }
            }


            /* Crop selection navigation button */
            cropSelectionLayout.apply {

                cropSelectionBackButton.setOnClickListener {
                    cropSelectionLayout.root.visibility = View.GONE
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                }


                cropSelectionNextButton.setOnClickListener {
                    cropSelectionLayout.root.visibility = View.GONE
                    cropProductionLayout.root.visibility = View.VISIBLE
                }

            }

            /*Crop Production navigation buttons*/
            cropProductionLayout.apply {
                cropProductionNextButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    cropProductionLayout.root.visibility = View.GONE
                }
                cropProductionBackButton.setOnClickListener {
                    cropSelectionLayout.root.visibility = View.VISIBLE
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
                        returnZeroStringIfEmpty(riversWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(riversDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.traditionalRiversWells = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(traditionalRiversWellsWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(traditionalRiversWellsDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.naturalPonds = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(naturalPondsWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(naturalPondsDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.pansAndDams = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(pansAndDamsWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(pansAndDamsDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.shallowWells = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(shallowWellsWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(shallowWellsDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.boreholes = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(boreHolesWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(boreHolesDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.springs = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(springsWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(springsDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.lakes = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(lakesWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(lakesDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.rockCatchments = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(rockCatchmentWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(rockCatchmentDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.pipedWater = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(pipedWaterWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(pipedWaterDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.waterTrucking = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(waterTruckingWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(waterTruckingDrySeason.text.toString()).toDouble()
                    )

                    waterSourceResponses.roofCatchments = WaterDependenceResponseItem(
                        returnZeroStringIfEmpty(roofCatchmentWetSeason.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(roofCatchmentDrySeason.text.toString()).toDouble()
                    )

                    countyLevelQuestionnaire.waterSourceResponses = waterSourceResponses


                    val ethnicGroupList: MutableList<EthnicGroupModel> = ArrayList()
                    ethnicGroupList.add(EthnicGroupModel(0, "Luo", 0))
                    ethnicGroupList.add(EthnicGroupModel(0, "Kikuyu", 0))
                    ethnicGroupList.add(EthnicGroupModel(0, "Kamba", 0))
                    ethnicGroupList.add(EthnicGroupModel(0, "Lughya", 0))
                    ethnicGroupList.add(EthnicGroupModel(0, "Swahili", 0))

                    val tribeSelectionAdapter =
                        TribeSelectionAdapter(ethnicGroupList, this@CountyLevelFragment)
                    val gridLayoutManager = GridLayoutManager(activity, 1)

                    ethnicGroupSelection.apply {
                        tribeList.layoutManager = gridLayoutManager
                        tribeList.hasFixedSize()
                        tribeList.adapter =
                            tribeSelectionAdapter
                    }

                    mainWaterSource.root.visibility = View.GONE
                    ethnicGroupSelection.root.visibility = View.VISIBLE
                }
            }


            /* Ethnic Group Selection navigation */
            ethnicGroupSelection.apply {

                tribeSelectionBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    ethnicGroupSelection.root.visibility = View.GONE
                }

                tribeSelectionNextButton.setOnClickListener {

                    val ethnicGroupResponseList: MutableList<EthnicityResponseItem> = ArrayList()
                    for (currentEthnicGroup: EthnicGroupModel in countyLevelQuestionnaire.livelihoodZoneEthnicGroups) {
                        ethnicGroupResponseList.add(
                            EthnicityResponseItem(
                                currentEthnicGroup,
                                0.0
                            )
                        )
                    }

                    val ethnicPopulationAdapter =
                        EthnicityAdapter(ethnicGroupResponseList, this@CountyLevelFragment)
                    val gridLayoutManager = GridLayoutManager(activity, 1)

                    ethnicGroupPopulation.apply {
                        ethnicityTable.layoutManager = gridLayoutManager
                        ethnicityTable.hasFixedSize()
                        ethnicityTable.adapter =
                            ethnicPopulationAdapter
                    }

                    ethnicGroupSelection.root.visibility = View.GONE
                    ethnicGroupPopulation.root.visibility = View.VISIBLE
                }

            }


            /* Ethnic group percentage */
            ethnicGroupPopulation.apply {

                ethnicBackButton.setOnClickListener {
                    ethnicGroupPopulation.root.visibility = View.GONE
                    ethnicGroupSelection.root.visibility = View.VISIBLE
                }

                ethnicNextButton.setOnClickListener {
                    ethnicGroupPopulation.root.visibility = View.GONE
                    lzHungerPatterns.root.visibility = View.VISIBLE
                }

            }


            /*Hunger patterns navigation buttons*/
            lzHungerPatterns.apply {
                hungerPatternsBackButton.setOnClickListener {
                    ethnicGroupPopulation.root.visibility = View.VISIBLE
                    lzHungerPatterns.root.visibility = View.GONE
                }
                hungerPatternsNextButton.setOnClickListener {
                    countyLevelQuestionnaire.hungerPatternsResponses = HungerPatternsResponses(
                        returnZeroStringIfEmpty(etLongRainsHungerPeriod.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etEndLongBeginShortRainsHungerPeriod.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etShortRainsHungerPeriod.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etEndShortBeginLongRainsHungerPeriod.text.toString()).toDouble()
                    )
                    lzHazards.root.visibility = View.VISIBLE
                    lzHungerPatterns.root.visibility = View.GONE
                }
            }


            /*Hazards navigation*/
            lzHazards.apply {
                hazardBackButton.setOnClickListener {
                    lzHungerPatterns.root.visibility = View.VISIBLE
                    lzHazards.root.visibility = View.GONE
                }
                hazardNextButton.setOnClickListener {

                    val hazardResponses = HazardResponses()

                    hazardResponses.animalRustling = HazardResponseItem(
                        returnZeroStringIfEmpty(animalRustlingRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(animalRustlingNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.banditry = HazardResponseItem(
                        returnZeroStringIfEmpty(banditryRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(banditryNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.terrorism = HazardResponseItem(
                        returnZeroStringIfEmpty(terrorismRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(terrorismNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.ethnicConflict = HazardResponseItem(
                        returnZeroStringIfEmpty(ethicConflictRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(ethicConflictNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.politicalViolence = HazardResponseItem(
                        returnZeroStringIfEmpty(politicalViolenceRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(politicalViolenceNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.livestockPestsAndDiseases = HazardResponseItem(
                        returnZeroStringIfEmpty(pestAndDiseaseRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(pestAndDiseaseNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.hailstormsOrFrost = HazardResponseItem(
                        returnZeroStringIfEmpty(hailstormsOrFrostRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(hailstormsOrFrostNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.flooding = HazardResponseItem(
                        returnZeroStringIfEmpty(floodingRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(floodingNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.landslides = HazardResponseItem(
                        returnZeroStringIfEmpty(landslidesRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(landslidesNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.highWindsOrCyclones = HazardResponseItem(
                        returnZeroStringIfEmpty(windsOrCycloneRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(windsOrCycloneNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.bushFires = HazardResponseItem(
                        returnZeroStringIfEmpty(bushFiresRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(bushFiresRank.text.toString()).toDouble()
                    )

                    hazardResponses.cropPests = HazardResponseItem(
                        returnZeroStringIfEmpty(cropPestsRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(cropPestsNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.locustInvasion = HazardResponseItem(
                        returnZeroStringIfEmpty(locustInvasionRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(locustInvasionNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.cropDiseases = HazardResponseItem(
                        returnZeroStringIfEmpty(cropDiseasesRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(cropDiseasesNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.terminalIllnesses = HazardResponseItem(
                        returnZeroStringIfEmpty(terminalIllnessRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(terminalIllnessNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.malariaPowerOutBreak = HazardResponseItem(
                        returnZeroStringIfEmpty(malariaOutbreakRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(malariaOutbreakNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.malariaPowerOutBreak = HazardResponseItem(
                        returnZeroStringIfEmpty(waterBorneDiseaseRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(waterBorneDiseaseNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.waterBornDiseases = HazardResponseItem(
                        returnZeroStringIfEmpty(humanWildlifeConflictRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(humanWildlifeConflictNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.humanWildlifeConflict = HazardResponseItem(
                        returnZeroStringIfEmpty(humanWildlifeConflictRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(humanWildlifeConflictNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.highFoodPrices = HazardResponseItem(
                        returnZeroStringIfEmpty(highFoodPriceRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(highFoodPriceNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.marketFoodShortages = HazardResponseItem(
                        returnZeroStringIfEmpty(foodShortageRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(foodShortageNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.drinkingWaterShortages = HazardResponseItem(
                        returnZeroStringIfEmpty(drinkingWaterShortageRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(drinkingWaterShortageNoOfYears.text.toString()).toDouble()
                    )

                    hazardResponses.others = HazardResponseItem(
                        returnZeroStringIfEmpty(othersRank.text.toString()).toInt(),
                        returnZeroStringIfEmpty(othersNoOfYears.text.toString()).toDouble()
                    )

                    countyLevelQuestionnaire.hazardResponses = hazardResponses

                    lzCompletionPage.root.visibility = View.VISIBLE
                    lzHazards.root.visibility = View.GONE
                }
            }

            /*LzCompletion page navigation*/
            lzCompletionPage.apply {
                closeButton.setOnClickListener {
                    countyLevelQuestionnaire.questionnaireEndDate = Util.getNow()
                    val gson = Gson()
                    val sharedPreferences: SharedPreferences? =
                        context?.applicationContext?.getSharedPreferences(
                            "MyPref",
                            Context.MODE_PRIVATE
                        )
                    val editor: SharedPreferences.Editor? = sharedPreferences?.edit()


                    val questionnairesListString =
                        sharedPreferences?.getString(Constants.QUESTIONNAIRES_LIST_OBJECT, null)
                    val questionnairesListObject: CountyLevelQuestionnaireListObject =
                        gson.fromJson(
                            questionnairesListString,
                            CountyLevelQuestionnaireListObject::class.java
                        )
                    questionnairesListObject.addQuestionnaire(countyLevelQuestionnaire)
                    editor?.remove(Constants.QUESTIONNAIRES_LIST_OBJECT)

                    val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
                    editor?.putString(
                        Constants.QUESTIONNAIRES_LIST_OBJECT,
                        newQuestionnaireObjectString
                    )
                    editor?.commit()

                    this@CountyLevelFragment.dismiss()

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


    private fun inflateLivelihoodZoneModal(livelihoodZoneModelList: MutableList<LivelihoodZoneModel>) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val lzAdapter = LivelihoodZonesAdapter(
            livelihoodZoneModelList,
            this
        )
        val gridLayoutManager = GridLayoutManager(activity, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = lzAdapter

        openLivelihoodZoneModal(v)
    }

    private fun openLivelihoodZoneModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setView(v)
        builder.setCancelable(true)
        livelihoodZoneAlertDialog = builder.create()
        (livelihoodZoneAlertDialog as android.app.AlertDialog).apply {
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

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(matchParent, matchParent)
            window?.setBackgroundDrawable(null)
        }
    }

    override fun onLivelihoodZoneItemClicked(selectedLivelihoodZone: LivelihoodZoneModel) {
        countyLevelQuestionnaire.selectedLivelihoodZone = selectedLivelihoodZone
        (livelihoodZoneAlertDialog as android.app.AlertDialog).dismiss()
        binding.apply {
            countyConfiguration.apply {
                livelihoodZoneText.text = selectedLivelihoodZone.livelihoodZoneName
            }
        }

    }


    private fun isStoragePermissionGranted(): Boolean {
        val scopedActivity = context

        val isPermissionGranted = scopedActivity?.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED

        return if (isPermissionGranted) {
            true
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                WRITE_STORAGE_PERMISSION_CODE
            )
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_STORAGE_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            var latitude: Double = 0.0
            var longitude: Double = 0.0
            val gpsTracker: GpsTracker = GpsTracker(context)

            latitude = gpsTracker.latitude
            longitude = gpsTracker.longitude
            countyLevelQuestionnaire.latitude = latitude
            countyLevelQuestionnaire.longitude = longitude

            prepareLivelihoodSelectionLayout()
            binding.apply {
                countyConfiguration.root.visibility = View.GONE
                livelihoodZoneSelection.root.visibility = View.VISIBLE
            }

        }
    }

    override fun onLivelihoodZoneItemSelectedFromSelectionList(selectedLivelihoodZone: LivelihoodZoneModel) {

        if (isLzAlreadySelected(selectedLivelihoodZone)) {
            countyLevelQuestionnaire.countyLivelihoodZones =
                removeItemFromLzList(selectedLivelihoodZone) as MutableList<LivelihoodZoneModel>
        } else {
            countyLevelQuestionnaire.countyLivelihoodZones.add(selectedLivelihoodZone)
        }
    }

    fun removeItemFromLzList(itemToBeRemoved: LivelihoodZoneModel): List<LivelihoodZoneModel> {
        return countyLevelQuestionnaire.countyLivelihoodZones.filter { s -> s.livelihoodZoneId != itemToBeRemoved.livelihoodZoneId }
    }

    fun isLzAlreadySelected(selectedItem: LivelihoodZoneModel): Boolean {
        return countyLevelQuestionnaire.countyLivelihoodZones.filter { s -> s.livelihoodZoneId == selectedItem.livelihoodZoneId }.size > 0
    }

    override fun onLivelihoodZoneSelected(selectedSubLocationZoneAssignment: SubLocationZoneAssignmentModel) {
        countyLevelQuestionnaire.subLocationZoneAllocationList.add(selectedSubLocationZoneAssignment)
    }

    override fun onCropItemSelectedFromSelectionList(selectedCrop: CropModel) {
        countyLevelQuestionnaire.livelihoodZoneCrops.add(selectedCrop)
    }

    override fun onTribeItemSelectedFromSelectionList(selectedTribe: EthnicGroupModel) {
        countyLevelQuestionnaire.livelihoodZoneEthnicGroups.add(selectedTribe)
    }
}