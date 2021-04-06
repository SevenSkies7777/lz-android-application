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
import com.silasonyango.ndma.ui.home.adapters.LivelihoodZonesAdapter
import com.silasonyango.ndma.ui.home.adapters.LzSelectionAdapter
import com.silasonyango.ndma.ui.home.adapters.SubLocationZoneAssignmentAdapter
import com.silasonyango.ndma.ui.home.adapters.WgQuestionnaireTypeAdapter
import com.silasonyango.ndma.ui.wealthgroup.WealthGroupDialogFragment
import com.silasonyango.ndma.util.GpsTracker
import com.silasonyango.ndma.util.Util


class CountyLevelFragment : DialogFragment(),
    SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback,
    LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack,
    LzMarketTradeRecyclerViewAdapter.LzMarketTradeRecyclerViewAdapterCallBack,
    LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack, LzSelectionAdapter.LzSelectionAdapterCallBack, SubLocationZoneAssignmentAdapter.SubLocationZoneAssignmentAdapterCallBack {

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
            cropModelList.add(CropModel("Maize", 1))
            cropModelList.add(CropModel("Beans", 2))
            cropModelList.add(CropModel("Potatoes", 3))
            cropModelList.add(CropModel("Tomatoes", 4))
            cropModelList.add(CropModel("Cassava", 5))
            cropModelList.add(CropModel("Rice", 6))
            cropModelList.add(CropModel("Mango", 7))
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

                    val subLocationZoneAssignmentModelList: MutableList<SubLocationZoneAssignmentModel> = ArrayList()

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
                    countyConfiguration.root.visibility = View.VISIBLE
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
                    lzHungerPatterns.root.visibility = View.VISIBLE
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

                }
            }
        }

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
            countyLevelQuestionnaire.countyLivelihoodZones = removeItemFromLzList(selectedLivelihoodZone) as MutableList<LivelihoodZoneModel>
        } else {
            countyLevelQuestionnaire.countyLivelihoodZones.add(selectedLivelihoodZone)
        }
    }

    fun removeItemFromLzList(itemToBeRemoved: LivelihoodZoneModel): List<LivelihoodZoneModel> {
        return countyLevelQuestionnaire.countyLivelihoodZones.filter { s -> s.livelihoodZoneId != itemToBeRemoved.livelihoodZoneId}
    }

    fun isLzAlreadySelected(selectedItem: LivelihoodZoneModel): Boolean {
        return countyLevelQuestionnaire.countyLivelihoodZones.filter { s -> s.livelihoodZoneId == selectedItem.livelihoodZoneId}.size > 0
    }

    override fun onLivelihoodZoneSelected(selectedSubLocationZoneAssignment: SubLocationZoneAssignmentModel) {
        countyLevelQuestionnaire.subLocationZoneAllocationList.add(selectedSubLocationZoneAssignment)
    }
}