package com.silasonyango.ndma.ui.county.destinations

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
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
import com.silasonyango.ndma.config.Constants.QUESTIONNAIRE_COMPLETED
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
import kotlinx.android.synthetic.main.market_geograpghy_configuration.*


class CountyLevelFragment : DialogFragment(),
    SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback,
    LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack,
    LzMarketTradeRecyclerViewAdapter.LzMarketTradeRecyclerViewAdapterCallBack,
    LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack,
    LzSelectionAdapter.LzSelectionAdapterCallBack,
    SubLocationZoneAssignmentAdapter.SubLocationZoneAssignmentAdapterCallBack,
    CropSelectionAdapter.CropSelectionAdapterCallBack,
    TribeSelectionAdapter.TribeSelectionAdapterCallBack, EthnicityAdapter.EthnicityAdapterCallBack,
    MonthsAdapter.MonthsAdapterCallBack,
    MarketSubCountySelectionAdapter.MarketSubCountySelectionAdapterCallBack,
    MarketTransactionsAdapter.MarketTransactionsAdapterCallBack {

    private lateinit var countyLevelViewModel: CountyLevelViewModel

    private lateinit var binding: CountyLevelQuestionnaireLayoutBinding

    private lateinit var countyLevelQuestionnaire: CountyLevelQuestionnaire

    lateinit var geographyObject: GeographyObject

    private var livelihoodZoneAlertDialog: android.app.AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    private var seasonCalendarDialog: android.app.AlertDialog? = null

    private var marketSubCountyDialog: android.app.AlertDialog? = null

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    val WRITE_STORAGE_PERMISSION_CODE: Int = 100

    val lzSeasonsResponses = LzSeasonsResponses()


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

    @RequiresApi(Build.VERSION_CODES.O)
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
        countyLevelQuestionnaire.questionnaireStartDate = Util.getNow()
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
        defineNavigation()
        binding.apply {


            val sublocationList: MutableList<SubLocationModel> = ArrayList()

            populateLocationAndPopulationRV(sublocationList)

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
                        countyLevelQuestionnaire.questionnaireName = AppStore.getInstance().sessionDetails?.geography?.county?.countyName + "county "+
                            countyLevelQuestionnaire.selectedLivelihoodZone.livelihoodZoneName + "Livelihood Zone questionnaire"


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
                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(etVerPoorResponse.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    etPoorResponse.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(etMediumResponse.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    etBetterOffResponse.text.toString()
                                ).toDouble()

                            if (totalEntry > 100) {
                                if (errorDialog != null) {
                                    errorDialog?.isShowing?.let { isDialogShowing->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
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
                etVerPoorResponse.addTextChangedListener(textWatcher)
                etPoorResponse.addTextChangedListener(textWatcher)
                etMediumResponse.addTextChangedListener(textWatcher)
                etBetterOffResponse.addTextChangedListener(textWatcher)
                locationNextButton.setOnClickListener {


                    val wealthGroupResponse = WealthGroupResponse(
                        returnZeroStringIfEmpty(etVerPoorResponse.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etPoorResponse.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etMediumResponse.text.toString()).toDouble(),
                        returnZeroStringIfEmpty(etBetterOffResponse.text.toString()).toDouble()
                    )
                    countyLevelQuestionnaire.wealthGroupResponse = wealthGroupResponse


                    val cropSelectionAdapter =
                        CropSelectionAdapter(geographyObject.crops, this@CountyLevelFragment)
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
                    populateCropProductionRecyclerView(countyLevelQuestionnaire.livelihoodZoneCrops)
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


                    val tribeSelectionAdapter =
                        TribeSelectionAdapter(
                            geographyObject.ethnicGroups,
                            this@CountyLevelFragment
                        )
                    val gridLayoutManager = GridLayoutManager(activity, 1)

                    ethnicGroupSelection.apply {
                        tribeList.layoutManager = gridLayoutManager
                        tribeList.hasFixedSize()
                        tribeList.adapter =
                            tribeSelectionAdapter
                    }

                    mainWaterSource.root.visibility = View.GONE
                    marketGeographyConfiguration.root.visibility = View.VISIBLE
                }
            }


            /* Market serving the livelihoodzone */
            marketGeographyConfiguration.apply {

                marketGeographyBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    marketGeographyConfiguration.root.visibility = View.GONE
                }

                marketGeographyNextButton.setOnClickListener {
                    lzMarketTransactions.root.visibility = View.VISIBLE
                    marketGeographyConfiguration.root.visibility = View.GONE

                    val marketTransactionItems: MutableList<MarketTransactionsItem> = ArrayList()


                    for (currentDefinedMarket in countyLevelQuestionnaire.definedMarkets) {
                        marketTransactionItems.add(
                            MarketTransactionsItem(
                                currentDefinedMarket.marketUniqueId,
                                currentDefinedMarket.marketName,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false
                            )
                        )
                    }

                    countyLevelQuestionnaire.marketTransactionItems = marketTransactionItems

                    val marketTransactionsAdapter =
                        MarketTransactionsAdapter(
                            marketTransactionItems,
                            this@CountyLevelFragment
                        )
                    val gridLayoutManager = GridLayoutManager(activity, 1)

                    lzMarketTransactions.apply {

                        marketTransactionsList.layoutManager = gridLayoutManager
                        marketTransactionsList.hasFixedSize()
                        marketTransactionsList.adapter =
                            marketTransactionsAdapter

                    }

                }


                oneSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_ONE
                    )
                }
                twoSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_TWO
                    )
                }
                threeSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_THREE
                    )
                }
                fourSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_FOUR
                    )
                }
                fiveSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_FIVE
                    )
                }
                sixSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_SIX
                    )
                }
                sevenSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_SEVEN
                    )
                }
                eightSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_EIGHT
                    )
                }
                nineSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_NINE
                    )
                }
                tenSubCounty.setOnClickListener {
                    inflateMarketCountySelectionModal(
                        geographyObject.subCounties,
                        MarketCountySelectionEnum.MARKET_TEN
                    )
                }


            }


            /* Market transactions navigation */
            lzMarketTransactions.apply {

                marketTransactionBackButton.setOnClickListener {
                    marketGeographyConfiguration.root.visibility = View.VISIBLE
                    lzMarketTransactions.root.visibility = View.GONE
                }

                marketTransactionNextButton.setOnClickListener {
                    ethnicGroupSelection.root.visibility = View.VISIBLE
                    lzMarketTransactions.root.visibility = View.GONE
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

                    lzSeasonsCalendar.root.visibility = View.VISIBLE
                    lzHazards.root.visibility = View.GONE
                }
            }


            lzSeasonsCalendar.apply {

                /* Seasons responses */
                dryMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.SEASONS_DRY
                    )
                }
                longRainMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.SEASONS_LONG_RAINS
                    )
                }
                shortRainMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.SEASONS_SHORT_RAINS
                    )
                }


                /* Crop production responses */
                landPrepMaize.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.MAIZE_LAND_PREPARATION
                    )
                }
                landPrepCassava.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.CASSAVA_LAND_PREPARATION
                    )
                }
                landPrepRice.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.RICE_LAND_PREPARATION
                    )
                }
                landPrepSorghum.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.SORGHUM_LAND_PREPARATION
                    )
                }
                landPrepLegumes.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LEGUMES_LAND_PREPARATION
                    )
                }

                plantingMaize.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.MAIZE_PLANTING
                    )
                }
                plantingCassava.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.CASSAVA_PLANTING
                    )
                }
                plantingRice.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.RICE_PLANTING
                    )
                }
                plantingSorghum.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.SORGHUM_PLANTING
                    )
                }
                plantingLegumes.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LEGUMES_PLANTING
                    )
                }

                harvestingMaize.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.MAIZE_HARVESTING
                    )
                }
                harvestingCassava.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.CASSAVA_HARVESTING
                    )
                }
                harvestingRice.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.RICE_HARVESTING
                    )
                }
                harvestingSorghum.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.SORGHUM_HARVESTING
                    )
                }
                harvestingLegumes.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LEGUMES_HARVESTING
                    )
                }


                /* Livestock production responses */
                livestockInMigration.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LIVESTOCK_IN_MIGRATION
                    )
                }
                livestockOutMigration.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LIVESTOCK_OUT_MIGRATION
                    )
                }
                milkHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_MILK_PRODUCTION
                    )
                }
                milkLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_MILK_PRODUCTION
                    )
                }
                calvingHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_CALVING
                    )
                }
                calvingLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_CALVING
                    )
                }
                kiddingHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_KIDDING
                    )
                }
                kiddingLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_KIDDING
                    )
                }
                foodPricesHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_FOOD_PRICES
                    )
                }
                foodPricesLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_FOOD_PRICES
                    )
                }
                livestockPricesHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_LIVESTOCK_PRICES
                    )
                }
                livestockPricesLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_LIVESTOCK_PRICES
                    )
                }
                casualLabourAvailabilityHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_CASUAL_LABOUR_AVAILABILITY
                    )
                }
                casualLabourAvailabilityLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_CASUAL_LABOUR_AVAILABILITY
                    )
                }
                casualLabourWagesHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_CASUAL_LABOUR_WAGES
                    )
                }
                casualLabourWagesLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_CASUAL_LABOUR_WAGES
                    )
                }
                remittancesHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_REMITTANCES
                    )
                }
                remittancesLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_REMITTANCES
                    )
                }
                fishingHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_FISHING
                    )
                }
                fishingLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_FISHING
                    )
                }
                marketAccessHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_MARKET_ACCESS
                    )
                }
                marketAccessLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_MARKET_ACCESS
                    )
                }
                diseaseOutbreakHigh.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.HIGH_DISEASE_OUTBREAK
                    )
                }
                diseaseOutbreakLow.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LOW_DISEASE_OUTBREAK
                    )
                }
                waterStressMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.WATER_STRESS
                    )
                }
                conflictRiskMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.CONFLICT_RISK
                    )
                }
                ceremoniesMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.CEREMONIES
                    )
                }
                leanSeasonsMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.LEAN_SEASONS
                    )
                }
                foodSecurityMonth.setOnClickListener {
                    inflateSeasonCalendarModal(
                        geographyObject.months,
                        SeasonsResponsesEnum.FOOD_SECURITY_ASSESSMENTS
                    )
                }


                seasonCalendarBackButton.setOnClickListener {
                    lzSeasonsCalendar.root.visibility = View.GONE
                    lzHazards.root.visibility = View.VISIBLE
                }

                seasonCalendarNextButton.setOnClickListener {
                    lzSeasonsCalendar.root.visibility = View.GONE
                    lzCompletionPage.root.visibility = View.VISIBLE
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


                    val intent = Intent()
                    intent.action = QUESTIONNAIRE_COMPLETED
                    intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                    activity?.sendBroadcast(intent)

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

    @RequiresApi(Build.VERSION_CODES.O)
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
            countyLevelQuestionnaire.questionnaireStartDate = Util.getNow()
            countyLevelQuestionnaire.questionnaireName = AppStore.getInstance().sessionDetails?.geography?.county?.countyName + "county "+
                    countyLevelQuestionnaire.selectedLivelihoodZone.livelihoodZoneName + "Livelihood Zone questionnaire"

            prepareLivelihoodSelectionLayout()
            binding.apply {
                countyConfiguration.root.visibility = View.GONE
                livelihoodZoneSelection.root.visibility = View.VISIBLE
            }

        }
    }


    private fun inflateSeasonCalendarModal(
        months: MutableList<MonthsModel>,
        seasonsResponsesEnum: SeasonsResponsesEnum
    ) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val monthsAdapter = MonthsAdapter(
            months,
            this,
            seasonsResponsesEnum
        )
        val gridLayoutManager = GridLayoutManager(activity, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = monthsAdapter

        openSeasonCalendarModal(v)
    }

    private fun openSeasonCalendarModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setView(v)
        builder.setCancelable(true)
        seasonCalendarDialog = builder.create()
        (seasonCalendarDialog as android.app.AlertDialog).apply {
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


    private fun inflateMarketCountySelectionModal(
        subCounties: MutableList<SubCountyModel>,
        marketSubCountySelectionEnum: MarketCountySelectionEnum
    ) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val marketSubCountySelectionAdapter = MarketSubCountySelectionAdapter(
            subCounties,
            this,
            marketSubCountySelectionEnum
        )
        val gridLayoutManager = GridLayoutManager(activity, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = marketSubCountySelectionAdapter

        openMarketCountySelectionModal(v)
    }

    private fun openMarketCountySelectionModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setView(v)
        builder.setCancelable(true)
        marketSubCountyDialog = builder.create()
        (marketSubCountyDialog as android.app.AlertDialog).apply {
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

    override fun onMonthSelected(
        selectedMonth: MonthsModel,
        seasonsResponsesEnum: SeasonsResponsesEnum
    ) {

        binding.apply {
            lzSeasonsCalendar.apply {

                /* Seasons responses */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SEASONS_DRY) {
                    lzSeasonsResponses.dry = selectedMonth
                    dryMonth.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SEASONS_LONG_RAINS) {
                    lzSeasonsResponses.longRains = selectedMonth
                    longRainMonth.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SEASONS_SHORT_RAINS) {
                    lzSeasonsResponses.shortRains = selectedMonth
                    shortRainMonth.text = selectedMonth.monthName
                }


                /* Crop production responses */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.MAIZE_LAND_PREPARATION) {
                    lzSeasonsResponses.maizeLandPreparation = selectedMonth
                    landPrepMaize.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CASSAVA_LAND_PREPARATION) {
                    lzSeasonsResponses.cassavaLandPreparation = selectedMonth
                    landPrepCassava.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.RICE_LAND_PREPARATION) {
                    lzSeasonsResponses.riceLandPreparation = selectedMonth
                    landPrepRice.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SORGHUM_LAND_PREPARATION) {
                    lzSeasonsResponses.sorghumLandPreparation = selectedMonth
                    landPrepSorghum.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEGUMES_LAND_PREPARATION) {
                    lzSeasonsResponses.legumesLandPreparation = selectedMonth
                    landPrepLegumes.text = selectedMonth.monthName
                }

                if (seasonsResponsesEnum == SeasonsResponsesEnum.MAIZE_PLANTING) {
                    lzSeasonsResponses.maizePlanting = selectedMonth
                    plantingMaize.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CASSAVA_PLANTING) {
                    lzSeasonsResponses.cassavaPlanting = selectedMonth
                    plantingCassava.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.RICE_PLANTING) {
                    lzSeasonsResponses.ricePlanting = selectedMonth
                    plantingRice.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SORGHUM_PLANTING) {
                    lzSeasonsResponses.sorghumPlanting = selectedMonth
                    plantingSorghum.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEGUMES_PLANTING) {
                    lzSeasonsResponses.legumesPlanting = selectedMonth
                    plantingLegumes.text = selectedMonth.monthName
                }

                if (seasonsResponsesEnum == SeasonsResponsesEnum.MAIZE_HARVESTING) {
                    lzSeasonsResponses.maizeHarvesting = selectedMonth
                    harvestingMaize.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CASSAVA_HARVESTING) {
                    lzSeasonsResponses.cassavaHarvesting = selectedMonth
                    plantingCassava.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.RICE_HARVESTING) {
                    lzSeasonsResponses.riceHarvesting = selectedMonth
                    harvestingRice.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SORGHUM_HARVESTING) {
                    lzSeasonsResponses.sorghumHarvesting = selectedMonth
                    harvestingSorghum.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEGUMES_HARVESTING) {
                    lzSeasonsResponses.legumesHarvesting = selectedMonth
                    harvestingLegumes.text = selectedMonth.monthName
                }


                /* Livestock production responses */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LIVESTOCK_IN_MIGRATION) {
                    lzSeasonsResponses.livestockInMigration = selectedMonth
                    livestockInMigration.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LIVESTOCK_OUT_MIGRATION) {
                    lzSeasonsResponses.livestockOutMigration = selectedMonth
                    livestockOutMigration.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_MILK_PRODUCTION) {
                    lzSeasonsResponses.highMilkProduction = selectedMonth
                    milkHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_MILK_PRODUCTION) {
                    lzSeasonsResponses.lowMilkProduction = selectedMonth
                    milkLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_CALVING) {
                    lzSeasonsResponses.highCalving = selectedMonth
                    calvingHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_CALVING) {
                    lzSeasonsResponses.lowCalving = selectedMonth
                    calvingLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_KIDDING) {
                    lzSeasonsResponses.highKidding = selectedMonth
                    kiddingHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_KIDDING) {
                    lzSeasonsResponses.lowKidding = selectedMonth
                    kiddingLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_FOOD_PRICES) {
                    lzSeasonsResponses.highFoodPrices = selectedMonth
                    foodPricesHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_FOOD_PRICES) {
                    lzSeasonsResponses.lowFoodPrices = selectedMonth
                    foodPricesLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_LIVESTOCK_PRICES) {
                    lzSeasonsResponses.highLivestockPrices = selectedMonth
                    livestockPricesHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_LIVESTOCK_PRICES) {
                    lzSeasonsResponses.lowLivestockPrices = selectedMonth
                    livestockPricesLow.text = selectedMonth.monthName
                }


                /* Others */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_CASUAL_LABOUR_AVAILABILITY) {
                    lzSeasonsResponses.highCasualLabourAvailability = selectedMonth
                    casualLabourAvailabilityHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_CASUAL_LABOUR_AVAILABILITY) {
                    lzSeasonsResponses.lowCasualLabourAvailability = selectedMonth
                    casualLabourAvailabilityLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_CASUAL_LABOUR_WAGES) {
                    lzSeasonsResponses.highCasualLabourWages = selectedMonth
                    casualLabourWagesHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_CASUAL_LABOUR_WAGES) {
                    lzSeasonsResponses.lowCasualLabourWages = selectedMonth
                    casualLabourWagesLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_REMITTANCES) {
                    lzSeasonsResponses.highRemittances = selectedMonth
                    remittancesHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_REMITTANCES) {
                    lzSeasonsResponses.lowRemittances = selectedMonth
                    remittancesLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_FISHING) {
                    lzSeasonsResponses.highFish = selectedMonth
                    fishingHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_FISHING) {
                    lzSeasonsResponses.lowFish = selectedMonth
                    fishingLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_MARKET_ACCESS) {
                    lzSeasonsResponses.highMarketAccess = selectedMonth
                    marketAccessHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_MARKET_ACCESS) {
                    lzSeasonsResponses.lowMarketAccess = selectedMonth
                    marketAccessLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_DISEASE_OUTBREAK) {
                    lzSeasonsResponses.highDiseaseOutbreak = selectedMonth
                    diseaseOutbreakHigh.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_DISEASE_OUTBREAK) {
                    lzSeasonsResponses.lowDiseaseOutbreak = selectedMonth
                    diseaseOutbreakLow.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.WATER_STRESS) {
                    lzSeasonsResponses.waterStress = selectedMonth
                    waterStressMonth.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CONFLICT_RISK) {
                    lzSeasonsResponses.conflictRisks = selectedMonth
                    conflictRiskMonth.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CEREMONIES) {
                    lzSeasonsResponses.ceremonies = selectedMonth
                    ceremoniesMonth.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEAN_SEASONS) {
                    lzSeasonsResponses.leanSeasons = selectedMonth
                    leanSeasonsMonth.text = selectedMonth.monthName
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.FOOD_SECURITY_ASSESSMENTS) {
                    lzSeasonsResponses.foodSecurityAssessments = selectedMonth
                    foodSecurityMonth.text = selectedMonth.monthName
                }
            }
        }



        (seasonCalendarDialog as android.app.AlertDialog).dismiss()
        countyLevelQuestionnaire.livelihoodZoneSeasonsResponses = lzSeasonsResponses
    }

    override fun onMarketSubCountyItemClicked(
        selectedSubCounty: SubCountyModel,
        marketCountySelectionEnum: MarketCountySelectionEnum
    ) {

        binding.apply {

            marketGeographyConfiguration.apply {

                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_ONE) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            oneMarketName.text.toString(),
                            selectedSubCounty,
                            oneNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    oneSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_TWO) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            twoMarketName.text.toString(),
                            selectedSubCounty,
                            twoNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    twoSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_THREE) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            threeMarketName.text.toString(),
                            selectedSubCounty,
                            threeNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    threeSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_FOUR) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            fourMarketName.text.toString(),
                            selectedSubCounty,
                            fourNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    fourSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_FIVE) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            fiveMarketName.text.toString(),
                            selectedSubCounty,
                            fiveNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    fiveSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_SIX) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            sixMarketName.text.toString(),
                            selectedSubCounty,
                            sixNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    sixSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_SEVEN) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            sevenMarketName.text.toString(),
                            selectedSubCounty,
                            sevenNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    sevenSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_EIGHT) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            eightMarketName.text.toString(),
                            selectedSubCounty,
                            eightNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    eightSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_NINE) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            nineMarketName.text.toString(),
                            selectedSubCounty,
                            nineNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    nineSubCounty.text = selectedSubCounty.subCountyName
                }
                if (marketCountySelectionEnum == MarketCountySelectionEnum.MARKET_TEN) {
                    countyLevelQuestionnaire.definedMarkets.add(
                        DefinedMarketModel(
                            tenMarketName.text.toString(),
                            selectedSubCounty,
                            tenNearestVillageOrTown.text.toString(),
                            Util.generateUniqueId()
                        )
                    )
                    tenSubCounty.text = selectedSubCounty.subCountyName
                }

            }
        }

        (marketSubCountyDialog as android.app.AlertDialog).dismiss()


    }

    override fun onLivestockMarketTradeClicked(marketUniqueId: String, isTradeHappening: Boolean) {
        val currentMarketingTransactionItem =
            countyLevelQuestionnaire.marketTransactionItems.first {
                it.marketUniqueId == marketUniqueId
            }
        currentMarketingTransactionItem.livestockTrade = isTradeHappening
        val tempList: List<MarketTransactionsItem> =
            countyLevelQuestionnaire.marketTransactionItems.filter {
                it.marketUniqueId != marketUniqueId
            }
        (tempList as MutableList<MarketTransactionsItem>).add(currentMarketingTransactionItem)
        countyLevelQuestionnaire.marketTransactionItems = tempList
    }

    override fun onPoultryMarketTradeClicked(marketUniqueId: String, isTradeHappening: Boolean) {
        val currentMarketingTransactionItem =
            countyLevelQuestionnaire.marketTransactionItems.first {
                it.marketUniqueId == marketUniqueId
            }
        currentMarketingTransactionItem.poultryTrade = isTradeHappening
        val tempList: List<MarketTransactionsItem> =
            countyLevelQuestionnaire.marketTransactionItems.filter {
                it.marketUniqueId != marketUniqueId
            }
        (tempList as MutableList<MarketTransactionsItem>).add(currentMarketingTransactionItem)
        countyLevelQuestionnaire.marketTransactionItems = tempList
    }

    override fun onFarmProduceTradeClicked(marketUniqueId: String, isTradeHappening: Boolean) {
        val currentMarketingTransactionItem =
            countyLevelQuestionnaire.marketTransactionItems.first {
                it.marketUniqueId == marketUniqueId
            }
        currentMarketingTransactionItem.farmProduceTrade = isTradeHappening
        val tempList: List<MarketTransactionsItem> =
            countyLevelQuestionnaire.marketTransactionItems.filter {
                it.marketUniqueId != marketUniqueId
            }
        (tempList as MutableList<MarketTransactionsItem>).add(currentMarketingTransactionItem)
        countyLevelQuestionnaire.marketTransactionItems = tempList
    }

    override fun onFoodProduceTradeClicked(marketUniqueId: String, isTradeHappening: Boolean) {
        val currentMarketingTransactionItem =
            countyLevelQuestionnaire.marketTransactionItems.first {
                it.marketUniqueId == marketUniqueId
            }
        currentMarketingTransactionItem.foodProduceRetail = isTradeHappening
        val tempList: List<MarketTransactionsItem> =
            countyLevelQuestionnaire.marketTransactionItems.filter {
                it.marketUniqueId != marketUniqueId
            }
        (tempList as MutableList<MarketTransactionsItem>).add(currentMarketingTransactionItem)
        countyLevelQuestionnaire.marketTransactionItems = tempList
    }

    override fun onFarmInputsTradeClicked(marketUniqueId: String, isTradeHappening: Boolean) {
        val currentMarketingTransactionItem =
            countyLevelQuestionnaire.marketTransactionItems.first {
                it.marketUniqueId == marketUniqueId
            }
        currentMarketingTransactionItem.retailFarmInput = isTradeHappening
        val tempList: List<MarketTransactionsItem> =
            countyLevelQuestionnaire.marketTransactionItems.filter {
                it.marketUniqueId != marketUniqueId
            }
        (tempList as MutableList<MarketTransactionsItem>).add(currentMarketingTransactionItem)
        countyLevelQuestionnaire.marketTransactionItems = tempList
    }

    override fun onLabourExchangeTradeClicked(marketUniqueId: String, isTradeHappening: Boolean) {
        val currentMarketingTransactionItem =
            countyLevelQuestionnaire.marketTransactionItems.first {
                it.marketUniqueId == marketUniqueId
            }
        currentMarketingTransactionItem.labourExchange = isTradeHappening
        val tempList: List<MarketTransactionsItem> =
            countyLevelQuestionnaire.marketTransactionItems.filter {
                it.marketUniqueId != marketUniqueId
            }
        (tempList as MutableList<MarketTransactionsItem>).add(currentMarketingTransactionItem)
        countyLevelQuestionnaire.marketTransactionItems = tempList
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
}