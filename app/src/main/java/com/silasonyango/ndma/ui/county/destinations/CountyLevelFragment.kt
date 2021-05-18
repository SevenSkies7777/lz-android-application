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
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.children
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
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.login.model.GeographyObject
import com.silasonyango.ndma.ui.county.adapters.*
import com.silasonyango.ndma.ui.county.model.*
import com.silasonyango.ndma.ui.county.responses.*
import com.silasonyango.ndma.ui.county.viewmodel.CountyLevelViewModel
import com.silasonyango.ndma.ui.home.HomeViewModel
import com.silasonyango.ndma.ui.home.adapters.*
import com.silasonyango.ndma.ui.model.QuestionnaireStatus
import com.silasonyango.ndma.ui.model.RankResponseItem
import com.silasonyango.ndma.ui.wealthgroup.adapters.CropProductionListAdapter
import com.silasonyango.ndma.ui.wealthgroup.adapters.CropSelectionListAdapter
import com.silasonyango.ndma.ui.wealthgroup.adapters.TribesListViewAdapter
import com.silasonyango.ndma.ui.wealthgroup.responses.CropProductionResponseValueModel
import com.silasonyango.ndma.ui.wealthgroup.responses.CropSeasonResponseItem
import com.silasonyango.ndma.ui.wealthgroup.responses.WgCropProductionResponseItem
import com.silasonyango.ndma.util.GpsTracker
import com.silasonyango.ndma.util.Util


class CountyLevelFragment : DialogFragment(),
    SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback,
    LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack,
    LzMarketTradeRecyclerViewAdapter.LzMarketTradeRecyclerViewAdapterCallBack,
    LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack,
    LzSelectionAdapter.LzSelectionAdapterCallBack,
    CropSelectionAdapter.CropSelectionAdapterCallBack,
    TribeSelectionAdapter.TribeSelectionAdapterCallBack, EthnicityAdapter.EthnicityAdapterCallBack,
    MonthsAdapter.MonthsAdapterCallBack,
    MarketSubCountySelectionAdapter.MarketSubCountySelectionAdapterCallBack,
    MarketTransactionsAdapter.MarketTransactionsAdapterCallBack,
    CropSelectionListAdapter.CropSelectionListAdapterCallBack,
    CropProductionListAdapter.CropProductionListAdapterCallBack,
    TribesListViewAdapter.TribesListViewAdapterCallBack,
    HazardsRankingAdapter.HazardsRankingAdapterCallBack, ZoneCharectaristicsAdapter.ZoneCharectaristicsAdapterCallBack {

    private lateinit var countyLevelViewModel: CountyLevelViewModel

    private lateinit var binding: CountyLevelQuestionnaireLayoutBinding

    private lateinit var countyLevelQuestionnaire: CountyLevelQuestionnaire

    lateinit var geographyObject: GeographyObject

    private var livelihoodZoneAlertDialog: android.app.AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    private var seasonCalendarDialog: android.app.AlertDialog? = null

    private var hazardsRankingDialog: androidx.appcompat.app.AlertDialog? = null

    private var marketSubCountyDialog: android.app.AlertDialog? = null

    private var ethnicGroups: MutableList<EthnicGroupModel> = ArrayList()

    private var hazardsRanks: MutableList<RankResponseItem> = ArrayList()

    private var zoneCharectaristicsItemsList: MutableList<ZoneCharectaristicsResponseItem> = ArrayList()

    private var cropProductionResponseItems: MutableList<WgCropProductionResponseItem> = ArrayList()

    private lateinit var homeViewModel: HomeViewModel

    var questionnaireId: String? = null

    var questionnaireName: String? = null

    val WRITE_STORAGE_PERMISSION_CODE: Int = 100

    val lzSeasonsResponses = LzSeasonsResponses()

    private var crops: MutableList<CropModel> = ArrayList()

    val hazardResponses = HazardResponses()

    val subLocationZoneAssignmentModelList: MutableList<SubLocationZoneAssignmentModel> =
        ArrayList()


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
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

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
        crops = geographyObject.crops
        ethnicGroups = geographyObject.ethnicGroups
        defineNavigation()
        binding.apply {


            val sublocationList: MutableList<SubLocationModel> = ArrayList()

            populateLocationAndPopulationRV(sublocationList)

            countyConfiguration.apply {

                livelihoodZoneDropDown.setOnClickListener {
                    inflateLivelihoodZoneModal(geographyObject.currentUserAssignedCountyLivelihoodZones)
                }

                configurationSubmitButton.setOnClickListener {

                    if (countyLevelQuestionnaire.selectedLivelihoodZone != null) {

                        var latitude: Double = 0.0
                        var longitude: Double = 0.0
                        val gpsTracker: GpsTracker = GpsTracker(context)
                        if (isStoragePermissionGranted()) {
                            latitude = gpsTracker.latitude
                            longitude = gpsTracker.longitude
                            countyLevelQuestionnaire.latitude = latitude
                            countyLevelQuestionnaire.longitude = longitude
                            countyLevelQuestionnaire.questionnaireStartDate = Util.getNow()

                            countyLevelQuestionnaire.selectedLivelihoodZone?.let {
                                countyLevelQuestionnaire.questionnaireName =
                                    AppStore.getInstance().sessionDetails?.geography?.county?.countyName + " " +
                                            it.livelihoodZoneName + " Livelihood Zone questionnaire"
                            }


                            countyLevelQuestionnaire.selectedLivelihoodZone?.let {
                                val subLocationLivelihoodZoneAssignment =
                                    geographyObject.sublocationsLivelihoodZoneAssignments.filter {
                                        it.livelihoodZoneId == it.livelihoodZoneId
                                    }

                                for (currentSubLocationLivelihoodZoneAssignment in subLocationLivelihoodZoneAssignment) {
                                    subLocationZoneAssignmentModelList.add(
                                        SubLocationZoneAssignmentModel(
                                            currentSubLocationLivelihoodZoneAssignment.subLocationName,
                                            currentSubLocationLivelihoodZoneAssignment.livelihoodZoneId,
                                            currentSubLocationLivelihoodZoneAssignment.livelihoodZoneName
                                        )
                                    )
                                }
                            }

                            countyLivelihoodZoneCharectaristics.apply {
                                for (currentLivelihoodZone in geographyObject.currentUserAssignedCountyLivelihoodZones) {
                                    zoneCharectaristicsItemsList.add(ZoneCharectaristicsResponseItem(
                                        currentLivelihoodZone, ArrayList<String>()
                                    ))
                                }
                                val zoneCharectaristicsAdapter =
                                    activity?.let { it1 ->
                                        ZoneCharectaristicsAdapter(zoneCharectaristicsItemsList,this@CountyLevelFragment,
                                            it1
                                        )
                                    }
                                val gridLayoutManager = GridLayoutManager(activity, 1)
                                zoneList.layoutManager = gridLayoutManager
                                zoneList.hasFixedSize()
                                zoneList.adapter =
                                    zoneCharectaristicsAdapter

                            }


                            countyLivelihoodZoneCharectaristics.root.visibility = View.VISIBLE
                            countyConfiguration.root.visibility = View.GONE
                        }

                    } else {
                        inflateErrorModal(
                            "Data Error",
                            "You have  not selected any livelihood zone"
                        )
                    }

                }
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun defineNavigation() {
        binding.apply {

            countyLivelihoodZoneCharectaristics.apply {

                lzXticsBackButton.setOnClickListener {
                    countyLivelihoodZoneCharectaristics.root.visibility = View.GONE
                    countyConfiguration.root.visibility = View.VISIBLE
                }

                lzXticsNextButton.setOnClickListener {

                    if (zoneCharectaristicsItemsList.filter { it.zoneCharectaristics.isEmpty() }.size > 0) {
                        inflateErrorModal("Missing data", "Kindly fill in all the charectaristics for all the zones")
                    } else {
                        lzSubLocationAssignment.apply {
                            val subLocationassignmentAdapter = activity?.let { it1 ->
                                SubLocationZoneAssignmentAdapter(
                                    subLocationZoneAssignmentModelList,
                                    it1
                                )
                            }

                            val gridLayoutManager = GridLayoutManager(activity, 1)
                            listRv.layoutManager = gridLayoutManager
                            listRv.hasFixedSize()
                            listRv.adapter =
                                subLocationassignmentAdapter
                        }

                        countyLivelihoodZoneCharectaristics.root.visibility = View.GONE
                        lzSubLocationAssignment.root.visibility = View.VISIBLE
                    }
                }

            }

            /* Lz Sublocation assignment navigation */
            lzSubLocationAssignment.apply {

                lzAllocationBackButton.setOnClickListener {
                    countyLivelihoodZoneCharectaristics.root.visibility = View.VISIBLE
                    lzSubLocationAssignment.root.visibility = View.GONE
                }

                lzAllocationNextButton.setOnClickListener {
                    lzSubLocationAssignment.root.visibility = View.GONE
                    wealthGroupCharectaristics.root.visibility = View.VISIBLE
                }

            }

            wealthGroupCharectaristics.apply {

                val wealthGroupCharectaristicsResponses = WealthGroupCharectaristicsResponses()

                xticsNumberSubmitButton.setOnClickListener {
                    if (noCharectaristics.text.toString().isNotEmpty()) {

                        val editTextsList: MutableList<EditText> = ArrayList()
                        for (i in 0..noCharectaristics.text.toString().toInt() - 1) {
                            editTextsList.add(EditText(requireContext()))
                        }

                        var ids = 1
                        for (currentEditText in editTextsList) {
                            currentEditText.setId(ids)
                            currentEditText.hint = "Charectaristic $ids"
                            currentEditText.setLayoutParams(
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            )
                            veryPoorList.addView(currentEditText)
                            ids++
                        }

                        numberVeryPoorCharectaristics.visibility = View.GONE
                        veryPoorCharectaristicsList.visibility = View.VISIBLE

                    }
                }


                veryPoorSubmitButton.setOnClickListener {

                    var allEditTextsAreEmpty = true
                    for (currentEditText in veryPoorList.children) {
                        val currentString = (currentEditText as EditText).text.toString()
                        if (currentString.trim().isNotEmpty()) {
                            allEditTextsAreEmpty = false
                            wealthGroupCharectaristicsResponses.veryPoorCharectaristics.add(currentString)
                        }
                    }

                    if (allEditTextsAreEmpty) {
                        inflateErrorModal("Data error", "You have not filled in any charectaristic")
                    } else {
                        veryPoorSection.visibility = View.GONE
                        veryPoorIcon.visibility = View.VISIBLE
                    }

                }


                poorXticsNumberSubmitButton.setOnClickListener {
                    if (poorNoCharectaristics.text.toString().isNotEmpty()) {

                        val editTextsList: MutableList<EditText> = ArrayList()
                        for (i in 0..poorNoCharectaristics.text.toString().toInt() - 1) {
                            editTextsList.add(EditText(requireContext()))
                        }

                        var ids = 1
                        for (currentEditText in editTextsList) {
                            currentEditText.hint = "Charectaristic $ids"
                            currentEditText.setLayoutParams(
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            )
                            poorList.addView(currentEditText)
                            ids++
                        }

                        numberPoorCharectaristics.visibility = View.GONE
                        poorCharectaristicsList.visibility = View.VISIBLE

                    }
                }


                poorSubmitButton.setOnClickListener {

                    var allEditTextsAreEmpty = true
                    for (currentEditText in poorList.children) {
                        val currentString = (currentEditText as EditText).text.toString()
                        if (currentString.trim().isNotEmpty()) {
                            allEditTextsAreEmpty = false
                            wealthGroupCharectaristicsResponses.poorCharectaristics.add(currentString)
                        }
                    }

                    if (allEditTextsAreEmpty) {
                        inflateErrorModal("Data error", "You have not filled in any charectaristic")
                    } else {
                        poorSection.visibility = View.GONE
                        poorIcon.visibility = View.VISIBLE
                    }

                }


                mediumXticsNumberSubmitButton.setOnClickListener {
                    if (mediumNoCharectaristics.text.toString().isNotEmpty()) {

                        val editTextsList: MutableList<EditText> = ArrayList()
                        for (i in 0..mediumNoCharectaristics.text.toString().toInt() - 1) {
                            editTextsList.add(EditText(requireContext()))
                        }

                        var ids = 1
                        for (currentEditText in editTextsList) {
                            currentEditText.hint = "Charectaristic $ids"
                            currentEditText.setLayoutParams(
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            )
                            mediumList.addView(currentEditText)
                            ids++
                        }

                        numberMediumCharectaristics.visibility = View.GONE
                        mediumCharectaristicsList.visibility = View.VISIBLE

                    }
                }


                mediumSubmitButton.setOnClickListener {

                    var allEditTextsAreEmpty = true
                    for (currentEditText in mediumList.children) {
                        val currentString = (currentEditText as EditText).text.toString()
                        if (currentString.trim().isNotEmpty()) {
                            allEditTextsAreEmpty = false
                            wealthGroupCharectaristicsResponses.mediumCharectaristics.add(currentString)
                        }
                    }

                    if (allEditTextsAreEmpty) {
                        inflateErrorModal("Data error", "You have not filled in any charectaristic")
                    } else {
                        mediumSection.visibility = View.GONE
                        mediumIcon.visibility = View.VISIBLE
                    }

                }


                betterOffXticsNumberSubmitButton.setOnClickListener {
                    if (betterOffNoCharectaristics.text.toString().isNotEmpty()) {

                        val editTextsList: MutableList<EditText> = ArrayList()
                        for (i in 0..betterOffNoCharectaristics.text.toString().toInt() - 1) {
                            editTextsList.add(EditText(requireContext()))
                        }

                        var ids = 1
                        for (currentEditText in editTextsList) {
                            currentEditText.hint = "Charectaristic $ids"
                            currentEditText.setLayoutParams(
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            )
                            betterOffList.addView(currentEditText)
                            ids++
                        }

                        numberBetterOffCharectaristics.visibility = View.GONE
                        betterOffCharectaristicsList.visibility = View.VISIBLE

                    }
                }

                betterOffSubmitButton.setOnClickListener {

                    var allEditTextsAreEmpty = true
                    for (currentEditText in betterOffList.children) {
                        val currentString = (currentEditText as EditText).text.toString()
                        if (currentString.trim().isNotEmpty()) {
                            allEditTextsAreEmpty = false
                            wealthGroupCharectaristicsResponses.betterOffCharectaristics.add(currentString)
                        }
                    }

                    if (allEditTextsAreEmpty) {
                        inflateErrorModal("Data error", "You have not filled in any charectaristic")
                    } else {
                        betterOffSection.visibility = View.GONE
                        betterOffIcon.visibility = View.VISIBLE
                    }

                }

                wgCharectaristicsBackButton.setOnClickListener {
                    lzSubLocationAssignment.root.visibility = View.VISIBLE
                    wealthGroupCharectaristics.root.visibility = View.GONE
                }

                wgCharectaristicsNextButton.setOnClickListener {

                    if (wealthGroupCharectaristicsResponses.poorCharectaristics.isNotEmpty() && wealthGroupCharectaristicsResponses.veryPoorCharectaristics.isNotEmpty()
                        && wealthGroupCharectaristicsResponses.mediumCharectaristics.isNotEmpty() && wealthGroupCharectaristicsResponses.betterOffCharectaristics.isNotEmpty()) {
                        countyLevelQuestionnaire.wealthGroupCharectariticsResponses =  wealthGroupCharectaristicsResponses

                        locationAndPopulationLayout.root.visibility = View.VISIBLE
                        wealthGroupCharectaristics.root.visibility = View.GONE
                    } else {
                        inflateErrorModal("Missing data", "Kindly fill in atleast a single charectaristic in all of the sections")
                    }
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
                etVerPoorResponse.addTextChangedListener(textWatcher)
                etPoorResponse.addTextChangedListener(textWatcher)
                etMediumResponse.addTextChangedListener(textWatcher)
                etBetterOffResponse.addTextChangedListener(textWatcher)


                locationNextButton.setOnClickListener {

                    if (etVerPoorResponse.text.toString()
                            .isNotEmpty() && etPoorResponse.text.toString().isNotEmpty()
                        && etMediumResponse.text.toString()
                            .isNotEmpty() && etBetterOffResponse.text.toString().isNotEmpty()
                    ) {

                        val totalEntry =
                            returnZeroStringIfEmpty(etVerPoorResponse.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                etPoorResponse.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(etMediumResponse.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                etBetterOffResponse.text.toString()
                            ).toDouble()

                        if (totalEntry < 100) {
                            inflateErrorModal(
                                "Percentage error",
                                "Total value is less than 100% by ${100 - totalEntry}"
                            )
                        } else {

                            val wealthGroupResponse = WealthGroupResponse(
                                returnZeroStringIfEmpty(etVerPoorResponse.text.toString()).toDouble(),
                                returnZeroStringIfEmpty(etPoorResponse.text.toString()).toDouble(),
                                returnZeroStringIfEmpty(etMediumResponse.text.toString()).toDouble(),
                                returnZeroStringIfEmpty(etBetterOffResponse.text.toString()).toDouble()
                            )
                            countyLevelQuestionnaire.wealthGroupResponse = wealthGroupResponse

                            cropSelectionLayout.apply {
                                activity?.let { context ->
                                    val adapter =
                                        CropSelectionListAdapter(
                                            context,
                                            R.layout.lz_selection_item,
                                            crops,
                                            this@CountyLevelFragment
                                        )
                                    cropsList.adapter = adapter
                                }
                            }

                            locationAndPopulationLayout.root.visibility = View.GONE
                            cropSelectionLayout.root.visibility = View.VISIBLE

                        }


                    } else {
                        inflateErrorModal("Data error", "Kindly fill out the missing fields")
                    }
                }
                locationBackButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.GONE
                    wealthGroupCharectaristics.root.visibility = View.VISIBLE
                }
            }


            /* Crop selection navigation button */
            cropSelectionLayout.apply {

                cropSelectionBackButton.setOnClickListener {
                    cropSelectionLayout.root.visibility = View.GONE
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                }


                cropSelectionNextButton.setOnClickListener {

                    if (countyLevelQuestionnaire.selectedCrops.isNotEmpty()) {

                        for (currentCrop in countyLevelQuestionnaire.selectedCrops) {
                            cropProductionResponseItems.add(
                                WgCropProductionResponseItem(
                                    currentCrop,
                                    CropSeasonResponseItem(
                                        CropProductionResponseValueModel(0.0, false),
                                        CropProductionResponseValueModel(0.0, false),
                                        CropProductionResponseValueModel(0.0, false),
                                        CropProductionResponseValueModel(0.0, false)
                                    ),
                                    CropSeasonResponseItem(
                                        CropProductionResponseValueModel(0.0, false),
                                        CropProductionResponseValueModel(0.0, false),
                                        CropProductionResponseValueModel(0.0, false),
                                        CropProductionResponseValueModel(0.0, false)
                                    )
                                )
                            )
                        }

                        cropProductionLayout.apply {
                            activity?.let { context ->
                                val adapter =
                                    CropProductionListAdapter(
                                        context,
                                        R.layout.lz_crop_production_item,
                                        cropProductionResponseItems,
                                        this@CountyLevelFragment
                                    )
                                cropsList.adapter = adapter
                            }
                        }

                        cropProductionLayout.root.visibility = View.VISIBLE
                        cropSelectionLayout.root.visibility = View.GONE

                    } else {
                        inflateErrorModal("Data error", "You have not selected any crop")
                    }

                }

            }

            /*Crop Production navigation buttons*/

            cropProductionLayout.apply {

                cropProductionBackButton.setOnClickListener {
                    cropProductionLayout.root.visibility = View.GONE
                    cropSelectionLayout.root.visibility = View.VISIBLE
                }

                cropProductionNextButton.setOnClickListener {

                    if (isAnyCropProductionFieldEmpty()) {
                        activity?.let { context ->
                            val adapter =
                                CropProductionListAdapter(
                                    context,
                                    R.layout.lz_crop_production_item,
                                    cropProductionResponseItems,
                                    this@CountyLevelFragment
                                )
                            cropsList.adapter = adapter
                        }
                        inflateErrorModal("Missing Data", "Kindly fill out all the fields")
                    } else if (doesCropProductionHavePercentageErrors()) {
                        inflateErrorModal(
                            "Percentage error",
                            returnAppropriateCropPercentagErrorMessage()
                        )
                    } else {
                        cropProductionLayout.root.visibility = View.GONE
                        mainWaterSource.root.visibility = View.VISIBLE
                    }
                }

            }

            /*Water source navigation buttons*/
            mainWaterSource.apply {
                waterSourceBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.GONE
                    cropProductionLayout.root.visibility = View.VISIBLE
                }


                val wetSeasonTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(riversWetSeason.text.toString()).toDouble() +
                                        returnZeroStringIfEmpty(traditionalRiversWellsWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    naturalPondsWetSeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(pansAndDamsWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    shallowWellsWetSeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(boreHolesWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    springsWetSeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(lakesWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    rockCatchmentWetSeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(pipedWaterWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    waterTruckingWetSeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(roofCatchmentWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    othersWetSeason.text.toString()
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


                riversWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                traditionalRiversWellsWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                naturalPondsWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                pansAndDamsWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                shallowWellsWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                boreHolesWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                springsWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                lakesWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                rockCatchmentWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                pipedWaterWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                waterTruckingWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                roofCatchmentWetSeason.addTextChangedListener(wetSeasonTextWatcher)
                othersWetSeason.addTextChangedListener(wetSeasonTextWatcher)


                val drySeasonTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            val totalEntry =
                                returnZeroStringIfEmpty(riversDrySeason.text.toString()).toDouble() +
                                        returnZeroStringIfEmpty(traditionalRiversWellsDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    naturalPondsDrySeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(pansAndDamsDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    shallowWellsDrySeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(boreHolesDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    springsDrySeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(lakesDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    rockCatchmentDrySeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(pipedWaterDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    waterTruckingDrySeason.text.toString()
                                ).toDouble() + returnZeroStringIfEmpty(roofCatchmentDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                    othersDrySeason.text.toString()
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

                riversDrySeason.addTextChangedListener(drySeasonTextWatcher)
                traditionalRiversWellsDrySeason.addTextChangedListener(drySeasonTextWatcher)
                naturalPondsDrySeason.addTextChangedListener(drySeasonTextWatcher)
                pansAndDamsDrySeason.addTextChangedListener(drySeasonTextWatcher)
                shallowWellsDrySeason.addTextChangedListener(drySeasonTextWatcher)
                boreHolesDrySeason.addTextChangedListener(drySeasonTextWatcher)
                springsDrySeason.addTextChangedListener(drySeasonTextWatcher)
                lakesDrySeason.addTextChangedListener(drySeasonTextWatcher)
                rockCatchmentDrySeason.addTextChangedListener(drySeasonTextWatcher)
                pipedWaterDrySeason.addTextChangedListener(drySeasonTextWatcher)
                waterTruckingDrySeason.addTextChangedListener(drySeasonTextWatcher)
                roofCatchmentDrySeason.addTextChangedListener(drySeasonTextWatcher)
                othersDrySeason.addTextChangedListener(drySeasonTextWatcher)

                waterSourceNextButton.setOnClickListener {

                    var hasNoValidationError: Boolean = true

                    if (riversWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        riversWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (riversDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        riversDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (traditionalRiversWellsWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        traditionalRiversWellsWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (traditionalRiversWellsDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        traditionalRiversWellsDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalPondsWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        naturalPondsWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (naturalPondsDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        naturalPondsDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pansAndDamsWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pansAndDamsWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pansAndDamsDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pansAndDamsDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (shallowWellsWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        shallowWellsWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (shallowWellsDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        shallowWellsDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (boreHolesWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        boreHolesWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (boreHolesDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        boreHolesDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (springsWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        springsWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (springsDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        springsDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (lakesWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        lakesWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (lakesDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        lakesDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (rockCatchmentWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        rockCatchmentWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (rockCatchmentDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        rockCatchmentDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pipedWaterWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pipedWaterWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (pipedWaterDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        pipedWaterDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (waterTruckingWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        waterTruckingWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (waterTruckingDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        waterTruckingDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (roofCatchmentWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        roofCatchmentWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (roofCatchmentDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        roofCatchmentDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (othersWetSeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        othersWetSeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (othersDrySeason.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        othersDrySeasonCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (hasNoValidationError) {

                        val wetSeasonTotalEntry =
                            returnZeroStringIfEmpty(riversWetSeason.text.toString()).toDouble() +
                                    returnZeroStringIfEmpty(traditionalRiversWellsWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                naturalPondsWetSeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(pansAndDamsWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                shallowWellsWetSeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(boreHolesWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                springsWetSeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(lakesWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                rockCatchmentWetSeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(pipedWaterWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                waterTruckingWetSeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(roofCatchmentWetSeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                othersWetSeason.text.toString()
                            ).toDouble()

                        val drySeasonTotalEntry =
                            returnZeroStringIfEmpty(riversDrySeason.text.toString()).toDouble() +
                                    returnZeroStringIfEmpty(traditionalRiversWellsDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                naturalPondsDrySeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(pansAndDamsDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                shallowWellsDrySeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(boreHolesDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                springsDrySeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(lakesDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                rockCatchmentDrySeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(pipedWaterDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                waterTruckingDrySeason.text.toString()
                            ).toDouble() + returnZeroStringIfEmpty(roofCatchmentDrySeason.text.toString()).toDouble() + returnZeroStringIfEmpty(
                                othersDrySeason.text.toString()
                            ).toDouble()

                        if (wetSeasonTotalEntry == 100.0 && drySeasonTotalEntry == 100.0) {

                            val waterSourceResponses = WaterSourcesResponses()
                            waterSourceResponses.rivers = WaterDependenceResponseItem(
                                returnZeroStringIfEmpty(riversWetSeason.text.toString()).toDouble(),
                                returnZeroStringIfEmpty(riversDrySeason.text.toString()).toDouble()
                            )

                            waterSourceResponses.traditionalRiversWells =
                                WaterDependenceResponseItem(
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

                            waterSourceResponses.others = WaterDependenceResponseItem(
                                returnZeroStringIfEmpty(othersWetSeason.text.toString()).toDouble(),
                                returnZeroStringIfEmpty(othersDrySeason.text.toString()).toDouble()
                            )

                            countyLevelQuestionnaire.waterSourceResponses = waterSourceResponses

                            mainWaterSource.root.visibility = View.GONE
                            marketGeographyConfiguration.root.visibility = View.VISIBLE

                        } else if (wetSeasonTotalEntry < 100) {
                            inflateErrorModal(
                                "Percentage Error",
                                "Wet season total entriesare less than 100% by ${100 - wetSeasonTotalEntry}"
                            )
                        } else if (drySeasonTotalEntry < 100) {
                            inflateErrorModal(
                                "Percentage Error",
                                "Dry season total entries are less than 100% by ${100 - drySeasonTotalEntry}"
                            )
                        }

                    }
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

                    ethnicGroupSelection.apply {
                        activity?.let { context ->
                            val adapter =
                                TribesListViewAdapter(
                                    context,
                                    R.layout.lz_selection_item,
                                    ethnicGroups,
                                    this@CountyLevelFragment
                                )
                            tribesList.adapter = adapter
                        }
                    }

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

                    if (countyLevelQuestionnaire.livelihoodZoneEthnicGroups.isNotEmpty()) {

                        val ethnicGroupResponseList: MutableList<EthnicityResponseItem> =
                            ArrayList()
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

                    } else {
                        inflateErrorModal("Data error", "You have not selected any ethnic group")
                    }
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

                var etLongRainsHungerPeriodHasError: Boolean = false
                var etEndLongBeginShortRainsHungerPeriodHasError: Boolean = false
                var etShortRainsHungerPeriodHasError: Boolean = false
                var etEndShortBeginLongRainsHungerPeriodHasError: Boolean = false


                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({

                            if (editable == etLongRainsHungerPeriod.editableText) {
                                if (editable.toString().toDouble() > 10.0) {
                                    errorDialog?.isShowing?.let { isDialogShowing ->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
                                    }
                                    inflateErrorModal(
                                        "Constraint error",
                                        "Number of years cannot exceed 10"
                                    )
                                    etLongRainsHungerPeriodHasError = true
                                } else {
                                    etLongRainsHungerPeriodHasError = false
                                }
                            }
                            if (editable == etEndLongBeginShortRainsHungerPeriod.editableText) {
                                if (editable.toString().toDouble() > 10.0) {
                                    errorDialog?.isShowing?.let { isDialogShowing ->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
                                    }
                                    inflateErrorModal(
                                        "Constraint error",
                                        "Number of years cannot exceed 10"
                                    )
                                    etEndLongBeginShortRainsHungerPeriodHasError = true
                                } else {
                                    etEndLongBeginShortRainsHungerPeriodHasError = false
                                }
                            }
                            if (editable == etShortRainsHungerPeriod.editableText) {
                                if (editable.toString().toDouble() > 10.0) {
                                    errorDialog?.isShowing?.let { isDialogShowing ->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
                                    }
                                    inflateErrorModal(
                                        "Constraint error",
                                        "Number of years cannot exceed 10"
                                    )
                                    etShortRainsHungerPeriodHasError = true
                                } else {
                                    etShortRainsHungerPeriodHasError = false
                                }
                            }
                            if (editable == etEndShortBeginLongRainsHungerPeriod.editableText) {
                                if (editable.toString().toDouble() > 10.0) {
                                    errorDialog?.isShowing?.let { isDialogShowing ->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
                                    }
                                    inflateErrorModal(
                                        "Constraint error",
                                        "Number of years cannot exceed 10"
                                    )
                                    etEndShortBeginLongRainsHungerPeriodHasError = true
                                } else {
                                    etEndShortBeginLongRainsHungerPeriodHasError = false
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

                etLongRainsHungerPeriod.addTextChangedListener(textWatcher)
                etEndLongBeginShortRainsHungerPeriod.addTextChangedListener(textWatcher)
                etShortRainsHungerPeriod.addTextChangedListener(textWatcher)
                etEndShortBeginLongRainsHungerPeriod.addTextChangedListener(textWatcher)

                hungerPatternsBackButton.setOnClickListener {
                    ethnicGroupPopulation.root.visibility = View.VISIBLE
                    lzHungerPatterns.root.visibility = View.GONE
                }
                hungerPatternsNextButton.setOnClickListener {
                    var hasNoValidationError: Boolean = true

                    if (etLongRainsHungerPeriod.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        etLongRainsHungerPeriodCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (etEndLongBeginShortRainsHungerPeriod.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        etEndLongBeginShortRainsHungerPeriodCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (etShortRainsHungerPeriod.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        etShortRainsHungerPeriodCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }
                    if (etEndShortBeginLongRainsHungerPeriod.text.toString().isEmpty()) {
                        hasNoValidationError = false
                        etEndShortBeginLongRainsHungerPeriodCell.background =
                            context?.resources?.getDrawable(R.drawable.error_cell, null)
                    }

                    if (hasNoValidationError &&
                        (!etLongRainsHungerPeriodHasError &&
                                !etEndLongBeginShortRainsHungerPeriodHasError &&
                                !etShortRainsHungerPeriodHasError &&
                                !etEndShortBeginLongRainsHungerPeriodHasError)
                    ) {

                        countyLevelQuestionnaire.hungerPatternsResponses = HungerPatternsResponses(
                            returnZeroStringIfEmpty(etLongRainsHungerPeriod.text.toString()).toDouble(),
                            returnZeroStringIfEmpty(etEndLongBeginShortRainsHungerPeriod.text.toString()).toDouble(),
                            returnZeroStringIfEmpty(etShortRainsHungerPeriod.text.toString()).toDouble(),
                            returnZeroStringIfEmpty(etEndShortBeginLongRainsHungerPeriod.text.toString()).toDouble()
                        )
                        lzHazards.root.visibility = View.VISIBLE
                        lzHungerPatterns.root.visibility = View.GONE

                    }

                    if (etLongRainsHungerPeriodHasError ||
                        etEndLongBeginShortRainsHungerPeriodHasError ||
                        etShortRainsHungerPeriodHasError ||
                        etEndShortBeginLongRainsHungerPeriodHasError
                    ) {

                        inflateErrorModal(
                            "Constraint error",
                            "Number of years cannot exceed 10"
                        )

                    }

                }
            }


            /*Hazards navigation*/
            lzHazards.apply {
                hazardBackButton.setOnClickListener {
                    lzHungerPatterns.root.visibility = View.VISIBLE
                    lzHazards.root.visibility = View.GONE
                }

                for (i in 0..23) {
                    hazardsRanks.add(RankResponseItem(i + 1, false))
                }


                animalRustlingRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.ANIMAL_RUSTLING)
                }

                banditryRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.BANDITRY)
                }

                terrorismRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.TERRORISM)
                }

                ethicConflictRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.ETHNIC_CONFLICT)
                }

                politicalViolenceRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.POLITICAL_CONFLICT)
                }

                droughtRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.DROUGHT)
                }

                pestAndDiseaseRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.LIVESTOCK_PESTS_DISEASES)
                }

                hailstormsOrFrostRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.HAILSTORMS)
                }

                floodingRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.FLOODING)
                }

                landslidesRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.LANDSLIDES)
                }

                windsOrCycloneRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.HIGH_WINDS)
                }

                bushFiresRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.BUSH_FIRES)
                }

                cropPestsRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.CROP_PESTS)
                }

                locustInvasionRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.LOCUST_INVASION)
                }

                cropDiseasesRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.CROP_DISEASES)
                }

                terminalIllnessRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.TERMINAL_ILLNESS)
                }

                malariaOutbreakRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.MALARIA)
                }

                waterBorneDiseaseRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.WATERBORNE_DISEASES)
                }

                humanWildlifeConflictRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.HUMAN_WILDLIFE_CONFLICT)
                }

                highFoodPriceRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.HIGH_FOOD_PRICES)
                }

                foodShortageRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.FOOD_SHORTAGE)
                }

                drinkingWaterShortageRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.DRINKING_WATER_SHORTAGE)
                }

                invasivePlantsRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.INVASIVE_PLANTS)
                }

                othersRank.setOnClickListener {
                    inflateHazardsRankModal(hazardsRanks, HazardTypeEnum.OTHERS)
                }


                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(editable: Editable?) {
                        Handler(Looper.getMainLooper()).postDelayed({


                            if (editable.toString().isNotEmpty()) {
                                if (editable.toString().toDouble() > 10.0) {
                                    errorDialog?.isShowing?.let { isDialogShowing ->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
                                    }
                                    inflateErrorModal("Data error", "Number of years cannot be greater than 10")
                                }
                                if (editable.toString().toDouble() < 0.0) {
                                    errorDialog?.isShowing?.let { isDialogShowing ->
                                        if (isDialogShowing) {
                                            return@postDelayed
                                        }
                                    }
                                    inflateErrorModal("Data error", "Number of years cannot be less than 0")
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


                animalRustlingNoOfYears.addTextChangedListener(textWatcher)
                banditryNoOfYears.addTextChangedListener(textWatcher)
                terrorismNoOfYears.addTextChangedListener(textWatcher)
                ethicConflictNoOfYears.addTextChangedListener(textWatcher)
                politicalViolenceNoOfYears.addTextChangedListener(textWatcher)
                droughtNoOfYears.addTextChangedListener(textWatcher)
                pestAndDiseaseNoOfYears.addTextChangedListener(textWatcher)
                hailstormsOrFrostNoOfYears.addTextChangedListener(textWatcher)
                floodingNoOfYears.addTextChangedListener(textWatcher)
                landslidesNoOfYears.addTextChangedListener(textWatcher)
                windsOrCycloneNoOfYears.addTextChangedListener(textWatcher)
                bushFiresNoOfYears.addTextChangedListener(textWatcher)
                cropPestsNoOfYears.addTextChangedListener(textWatcher)
                locustInvasionNoOfYears.addTextChangedListener(textWatcher)
                cropDiseasesNoOfYears.addTextChangedListener(textWatcher)
                terminalIllnessNoOfYears.addTextChangedListener(textWatcher)
                malariaOutbreakNoOfYears.addTextChangedListener(textWatcher)
                waterBorneDiseaseNoOfYears.addTextChangedListener(textWatcher)
                humanWildlifeConflictNoOfYears.addTextChangedListener(textWatcher)
                highFoodPriceNoOfYears.addTextChangedListener(textWatcher)
                foodShortageNoOfYears.addTextChangedListener(textWatcher)
                drinkingWaterShortageNoOfYears.addTextChangedListener(textWatcher)
                invasivePlantsNoOfYears.addTextChangedListener(textWatcher)
                othersNoOfYears.addTextChangedListener(textWatcher)

                hazardNextButton.setOnClickListener {

                    if (animalRustlingNoOfYears.text.toString()
                            .isNotEmpty() && banditryNoOfYears.text.toString()
                            .isNotEmpty() && terrorismNoOfYears.text.toString()
                            .isNotEmpty() && ethicConflictNoOfYears.text.toString()
                            .isNotEmpty() && politicalViolenceNoOfYears.text.toString()
                            .isNotEmpty() && droughtNoOfYears.text.toString()
                            .isNotEmpty() && pestAndDiseaseNoOfYears.text.toString()
                            .isNotEmpty() && hailstormsOrFrostNoOfYears.text.toString()
                            .isNotEmpty() && floodingNoOfYears.text.toString()
                            .isNotEmpty() && landslidesNoOfYears.text.toString()
                            .isNotEmpty() && windsOrCycloneNoOfYears.text.toString()
                            .isNotEmpty() && bushFiresNoOfYears.text.toString()
                            .isNotEmpty() && cropPestsNoOfYears.text.toString()
                            .isNotEmpty() && locustInvasionNoOfYears.text.toString()
                            .isNotEmpty() && cropDiseasesNoOfYears.text.toString()
                            .isNotEmpty() && terminalIllnessNoOfYears.text.toString()
                            .isNotEmpty() && malariaOutbreakNoOfYears.text.toString()
                            .isNotEmpty() && waterBorneDiseaseNoOfYears.text.toString()
                            .isNotEmpty() && humanWildlifeConflictNoOfYears.text.toString()
                            .isNotEmpty() && highFoodPriceNoOfYears.text.toString()
                            .isNotEmpty() && foodShortageNoOfYears.text.toString()
                            .isNotEmpty() && drinkingWaterShortageNoOfYears.text.toString()
                            .isNotEmpty() && invasivePlantsNoOfYears.text.toString()
                            .isNotEmpty() && othersNoOfYears.text.toString()
                            .isNotEmpty() && hazardResponses.animalRustling.importanceRank != 0
                        && hazardResponses.banditry.importanceRank != 0
                        && hazardResponses.terrorism.importanceRank != 0
                        && hazardResponses.ethnicConflict.importanceRank != 0
                        && hazardResponses.ethnicConflict.importanceRank != 0
                        && hazardResponses.politicalViolence.importanceRank != 0
                        && hazardResponses.drought.importanceRank != 0
                        && hazardResponses.livestockPestsAndDiseases.importanceRank != 0
                        && hazardResponses.hailstormsOrFrost.importanceRank != 0
                        && hazardResponses.flooding.importanceRank != 0
                        && hazardResponses.landslides.importanceRank != 0
                        && hazardResponses.highWindsOrCyclones.importanceRank != 0
                        && hazardResponses.bushFires.importanceRank != 0
                        && hazardResponses.cropPests.importanceRank != 0
                        && hazardResponses.locustInvasion.importanceRank != 0
                        && hazardResponses.cropDiseases.importanceRank != 0
                        && hazardResponses.terminalIllnesses.importanceRank != 0
                        && hazardResponses.malariaPowerOutBreak.importanceRank != 0
                        && hazardResponses.waterBornDiseases.importanceRank != 0
                        && hazardResponses.humanWildlifeConflict.importanceRank != 0
                        && hazardResponses.highFoodPrices.importanceRank != 0
                        && hazardResponses.marketFoodShortages.importanceRank != 0
                        && hazardResponses.drinkingWaterShortages.importanceRank != 0
                        && hazardResponses.invasivePlants.importanceRank != 0
                        && hazardResponses.others.importanceRank != 0
                    ) {
                        hazardResponses.animalRustling.noExperiencedYears = animalRustlingNoOfYears.text.toString().toDouble()
                        hazardResponses.banditry.noExperiencedYears = banditryNoOfYears.text.toString().toDouble()
                        hazardResponses.terrorism.noExperiencedYears = terrorismNoOfYears.text.toString().toDouble()
                        hazardResponses.ethnicConflict.noExperiencedYears = ethicConflictNoOfYears.text.toString().toDouble()
                        hazardResponses.politicalViolence.noExperiencedYears = politicalViolenceNoOfYears.text.toString().toDouble()
                        hazardResponses.drought.noExperiencedYears = droughtNoOfYears.text.toString().toDouble()
                        hazardResponses.livestockPestsAndDiseases.noExperiencedYears = pestAndDiseaseNoOfYears.text.toString().toDouble()
                        hazardResponses.hailstormsOrFrost.noExperiencedYears = hailstormsOrFrostNoOfYears.text.toString().toDouble()
                        hazardResponses.flooding.noExperiencedYears = floodingNoOfYears.text.toString().toDouble()
                        hazardResponses.landslides.noExperiencedYears = landslidesNoOfYears.text.toString().toDouble()
                        hazardResponses.highWindsOrCyclones.noExperiencedYears = windsOrCycloneNoOfYears.text.toString().toDouble()
                        hazardResponses.bushFires.noExperiencedYears = bushFiresNoOfYears.text.toString().toDouble()
                        hazardResponses.cropPests.noExperiencedYears = cropPestsNoOfYears.text.toString().toDouble()
                        hazardResponses.locustInvasion.noExperiencedYears = locustInvasionNoOfYears.text.toString().toDouble()
                        hazardResponses.cropDiseases.noExperiencedYears = cropDiseasesNoOfYears.text.toString().toDouble()
                        hazardResponses.terminalIllnesses.noExperiencedYears = terminalIllnessNoOfYears.text.toString().toDouble()
                        hazardResponses.malariaPowerOutBreak.noExperiencedYears = malariaOutbreakNoOfYears.text.toString().toDouble()
                        hazardResponses.waterBornDiseases.noExperiencedYears = waterBorneDiseaseNoOfYears.text.toString().toDouble()
                        hazardResponses.humanWildlifeConflict.noExperiencedYears = humanWildlifeConflictNoOfYears.text.toString().toDouble()
                        hazardResponses.highFoodPrices.noExperiencedYears = highFoodPriceNoOfYears.text.toString().toDouble()
                        hazardResponses.marketFoodShortages.noExperiencedYears = foodShortageNoOfYears.text.toString().toDouble()
                        hazardResponses.drinkingWaterShortages.noExperiencedYears = drinkingWaterShortageNoOfYears.text.toString().toDouble()
                        hazardResponses.invasivePlants.noExperiencedYears = invasivePlantsNoOfYears.text.toString().toDouble()
                        hazardResponses.others.noExperiencedYears = othersNoOfYears.text.toString().toDouble()

                        countyLevelQuestionnaire.hazardResponses = hazardResponses

                        lzSeasonsCalendar.root.visibility = View.VISIBLE
                        lzHazards.root.visibility = View.GONE
                    } else {
                        inflateErrorModal("Missing data", "Kindly fill out all the missing data")
                    }
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


                    if (lzSeasonsResponses.dry.isEmpty()) {
                        inflateErrorModal("Data error", "Dry season has no months selected")
                    } else if (lzSeasonsResponses.longRains.isEmpty()) {
                        inflateErrorModal("Data error", "Long rains season has no months selected")
                    } else if (lzSeasonsResponses.shortRains.isEmpty()) {
                        inflateErrorModal("Data error", "Short rains season has no months selected")
                    } else if (lzSeasonsResponses.maizeLandPreparation.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Maize land preparation season has no months selected"
                        )
                    } else if (lzSeasonsResponses.cassavaLandPreparation.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Cassava land preparation season has no months selected"
                        )
                    } else if (lzSeasonsResponses.riceLandPreparation.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Rice land preparation season has no months selected"
                        )
                    } else if (lzSeasonsResponses.sorghumLandPreparation.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Sorghum land preparation season has no months selected"
                        )
                    } else if (lzSeasonsResponses.legumesLandPreparation.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Legumes land preparation season has no months selected"
                        )
                    } else if (lzSeasonsResponses.maizePlanting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Maize planting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.cassavaPlanting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Cassava planting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.ricePlanting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Rice planting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.sorghumPlanting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Sorghum planting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.legumesPlanting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Legumes planting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.maizeHarvesting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Maize harvesting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.cassavaHarvesting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Cassava harvesting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.riceHarvesting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Rice harvesting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.sorghumHarvesting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Sorghum harvesting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.legumesHarvesting.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Legumes harvesting season has no months selected"
                        )
                    } else if (lzSeasonsResponses.livestockInMigration.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Livestock in-migration season has no months selected"
                        )
                    } else if (lzSeasonsResponses.livestockOutMigration.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Livestock out-migration season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highMilkProduction.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High milk production season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowMilkProduction.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low milk production season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highCalving.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High calving season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowCalving.isEmpty()) {
                        inflateErrorModal("Data error", "Low calving season has no months selected")
                    } else if (lzSeasonsResponses.highKidding.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High kidding season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowKidding.isEmpty()) {
                        inflateErrorModal("Data error", "Low kidding season has no months selected")
                    } else if (lzSeasonsResponses.highFoodPrices.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High food prices season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowFoodPrices.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low food prices season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highLivestockPrices.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High livestock prices season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowLivestockPrices.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low livestock prices season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highCasualLabourAvailability.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High casual labour availability season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowCasualLabourAvailability.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low casual labour availability season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highCasualLabourWages.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High casual labour wages season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowCasualLabourWages.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low casual labour wages season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highRemittances.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High remittances season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowRemittances.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low remittances season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highFish.isEmpty()) {
                        inflateErrorModal("Data error", "High fish season has no months selected")
                    } else if (lzSeasonsResponses.lowFish.isEmpty()) {
                        inflateErrorModal("Data error", "Low fish season has no months selected")
                    } else if (lzSeasonsResponses.highMarketAccess.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High market access season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowMarketAccess.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low market access season has no months selected"
                        )
                    } else if (lzSeasonsResponses.highDiseaseOutbreak.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "High disease outbreak season season has no months selected"
                        )
                    } else if (lzSeasonsResponses.lowDiseaseOutbreak.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Low disease outbreak season season has no months selected"
                        )
                    } else if (lzSeasonsResponses.waterStress.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Water stress season season has no months selected"
                        )
                    } else if (lzSeasonsResponses.conflictRisks.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Conflict risk season season has no months selected"
                        )
                    } else if (lzSeasonsResponses.ceremonies.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Ceremonies season season has no months selected"
                        )
                    } else if (lzSeasonsResponses.leanSeasons.isEmpty()) {
                        inflateErrorModal("Data error", "Lean season season has no months selected")
                    } else if (lzSeasonsResponses.foodSecurityAssessments.isEmpty()) {
                        inflateErrorModal(
                            "Data error",
                            "Food security assessment season season has no months selected"
                        )
                    } else {
                        lzSeasonsCalendar.root.visibility = View.GONE
                        lzCompletionPage.root.visibility = View.VISIBLE
                    }
                }

            }

            /*LzCompletion page navigation*/
            lzCompletionPage.apply {
                closeButton.setOnClickListener {
                    countyLevelQuestionnaire.questionnaireEndDate = Util.getNow()
                    countyLevelQuestionnaire.questionnaireStatus =
                        QuestionnaireStatus.COMPLETED_AWAITING_SUBMISSION
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
                    activity?.applicationContext?.sendBroadcast(intent)
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
            countyLevelQuestionnaire.questionnaireName =
                AppStore.getInstance().sessionDetails?.geography?.county?.countyName + " " +
                        countyLevelQuestionnaire.selectedLivelihoodZone?.livelihoodZoneName + " Livelihood Zone questionnaire"


            binding.apply {
                val subLocationLivelihoodZoneAssignment =
                    geographyObject.sublocationsLivelihoodZoneAssignments.filter {
                        it.livelihoodZoneId == countyLevelQuestionnaire.selectedLivelihoodZone?.livelihoodZoneId
                    }
                for (currentSubLocationLivelihoodZoneAssignment in subLocationLivelihoodZoneAssignment) {
                    subLocationZoneAssignmentModelList.add(
                        SubLocationZoneAssignmentModel(
                            currentSubLocationLivelihoodZoneAssignment.subLocationName,
                            currentSubLocationLivelihoodZoneAssignment.livelihoodZoneId,
                            currentSubLocationLivelihoodZoneAssignment.livelihoodZoneName
                        )
                    )
                }

                countyLivelihoodZoneCharectaristics.apply {
                    for (currentLivelihoodZone in geographyObject.currentUserAssignedCountyLivelihoodZones) {
                        zoneCharectaristicsItemsList.add(ZoneCharectaristicsResponseItem(
                            currentLivelihoodZone, ArrayList<String>()
                        ))
                    }
                    val zoneCharectaristicsAdapter =
                        activity?.let { it1 ->
                            ZoneCharectaristicsAdapter(zoneCharectaristicsItemsList,this@CountyLevelFragment,
                                it1
                            )
                        }
                    val gridLayoutManager = GridLayoutManager(activity, 1)
                    zoneList.layoutManager = gridLayoutManager
                    zoneList.hasFixedSize()
                    zoneList.adapter =
                        zoneCharectaristicsAdapter

                }


                countyLivelihoodZoneCharectaristics.root.visibility = View.VISIBLE
                countyConfiguration.root.visibility = View.GONE
            }

        }
    }


    private fun inflateSeasonCalendarModal(
        months: MutableList<MonthsModel>,
        seasonsResponsesEnum: SeasonsResponsesEnum
    ) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)
        val icClose = v.findViewById<View>(R.id.icClose)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val monthsAdapter = MonthsAdapter(
            months,
            this,
            seasonsResponsesEnum
        )

        icClose.setOnClickListener {
            (seasonCalendarDialog as android.app.AlertDialog).dismiss()
        }

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


    override fun onCropItemSelectedFromSelectionList(selectedCrop: CropModel) {
        countyLevelQuestionnaire.livelihoodZoneCrops.add(selectedCrop)
    }

    override fun onTribeItemSelectedFromSelectionList(selectedTribe: EthnicGroupModel) {
        countyLevelQuestionnaire.livelihoodZoneEthnicGroups.add(selectedTribe)
    }

    fun returnMonthInitialsString(months: MutableList<MonthsModel>): String {
        var monthsString = ""
        for (currentMonth in months) {
            monthsString =
                if (currentMonth.monthName.length > 3) monthsString + " ${currentMonth.monthName.substring(
                    0,
                    4
                )}," else monthsString + " ${currentMonth.monthName},"
        }
        return monthsString
    }

    override fun onMonthSelected(
        selectedMonth: MonthsModel,
        seasonsResponsesEnum: SeasonsResponsesEnum
    ) {

        binding.apply {
            lzSeasonsCalendar.apply {

                /* Seasons responses */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SEASONS_DRY) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.dry.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.dry.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.dry.add(selectedMonth)
                    }
                    dryMonth.text = returnMonthInitialsString(lzSeasonsResponses.dry)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SEASONS_LONG_RAINS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.longRains.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.longRains.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.longRains.add(selectedMonth)
                    }
                    longRainMonth.text = returnMonthInitialsString(lzSeasonsResponses.longRains)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SEASONS_SHORT_RAINS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.shortRains.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.shortRains.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.shortRains.add(selectedMonth)
                    }
                    shortRainMonth.text = returnMonthInitialsString(lzSeasonsResponses.shortRains)
                }


                /* Crop production responses */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.MAIZE_LAND_PREPARATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.maizeLandPreparation.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.maizeLandPreparation.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.maizeLandPreparation.add(selectedMonth)
                    }
                    landPrepMaize.text =
                        returnMonthInitialsString(lzSeasonsResponses.maizeLandPreparation)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CASSAVA_LAND_PREPARATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.cassavaLandPreparation.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.cassavaLandPreparation.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.cassavaLandPreparation.add(selectedMonth)
                    }
                    landPrepCassava.text =
                        returnMonthInitialsString(lzSeasonsResponses.cassavaLandPreparation)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.RICE_LAND_PREPARATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.riceLandPreparation.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.riceLandPreparation.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.riceLandPreparation.add(selectedMonth)
                    }
                    landPrepRice.text =
                        returnMonthInitialsString(lzSeasonsResponses.riceLandPreparation)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SORGHUM_LAND_PREPARATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.sorghumLandPreparation.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.sorghumLandPreparation.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.sorghumLandPreparation.add(selectedMonth)
                    }
                    landPrepSorghum.text =
                        returnMonthInitialsString(lzSeasonsResponses.sorghumLandPreparation)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEGUMES_LAND_PREPARATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.legumesLandPreparation.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.legumesLandPreparation.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.legumesLandPreparation.add(selectedMonth)
                    }
                    landPrepLegumes.text =
                        returnMonthInitialsString(lzSeasonsResponses.legumesLandPreparation)
                }

                if (seasonsResponsesEnum == SeasonsResponsesEnum.MAIZE_PLANTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.maizePlanting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.maizePlanting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.maizePlanting.add(selectedMonth)
                    }
                    plantingMaize.text = returnMonthInitialsString(lzSeasonsResponses.maizePlanting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CASSAVA_PLANTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.cassavaPlanting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.cassavaPlanting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.cassavaPlanting.add(selectedMonth)
                    }
                    plantingCassava.text =
                        returnMonthInitialsString(lzSeasonsResponses.cassavaPlanting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.RICE_PLANTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.ricePlanting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.ricePlanting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.ricePlanting.add(selectedMonth)
                    }
                    plantingRice.text = returnMonthInitialsString(lzSeasonsResponses.ricePlanting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SORGHUM_PLANTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.sorghumPlanting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.sorghumPlanting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.sorghumPlanting.add(selectedMonth)
                    }
                    plantingSorghum.text =
                        returnMonthInitialsString(lzSeasonsResponses.sorghumPlanting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEGUMES_PLANTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.legumesPlanting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.legumesPlanting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.legumesPlanting.add(selectedMonth)
                    }
                    plantingLegumes.text =
                        returnMonthInitialsString(lzSeasonsResponses.legumesPlanting)
                }

                if (seasonsResponsesEnum == SeasonsResponsesEnum.MAIZE_HARVESTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.maizeHarvesting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.maizeHarvesting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.maizeHarvesting.add(selectedMonth)
                    }
                    harvestingMaize.text =
                        returnMonthInitialsString(lzSeasonsResponses.maizeHarvesting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CASSAVA_HARVESTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.cassavaHarvesting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.cassavaHarvesting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.cassavaHarvesting.add(selectedMonth)
                    }
                    plantingCassava.text =
                        returnMonthInitialsString(lzSeasonsResponses.cassavaHarvesting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.RICE_HARVESTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.riceHarvesting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.riceHarvesting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.riceHarvesting.add(selectedMonth)
                    }
                    harvestingRice.text =
                        returnMonthInitialsString(lzSeasonsResponses.riceHarvesting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.SORGHUM_HARVESTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.sorghumHarvesting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.sorghumHarvesting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.sorghumHarvesting.add(selectedMonth)
                    }
                    harvestingRice.text =
                        returnMonthInitialsString(lzSeasonsResponses.sorghumHarvesting)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEGUMES_HARVESTING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.legumesHarvesting.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.legumesHarvesting.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.legumesHarvesting.add(selectedMonth)
                    }
                    harvestingSorghum.text =
                        returnMonthInitialsString(lzSeasonsResponses.legumesHarvesting)
                }


                /* Livestock production responses */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LIVESTOCK_IN_MIGRATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.livestockInMigration.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.livestockInMigration.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.livestockInMigration.add(selectedMonth)
                    }
                    livestockInMigration.text =
                        returnMonthInitialsString(lzSeasonsResponses.livestockInMigration)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LIVESTOCK_OUT_MIGRATION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.livestockOutMigration.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.livestockOutMigration.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.livestockOutMigration.add(selectedMonth)
                    }
                    livestockOutMigration.text =
                        returnMonthInitialsString(lzSeasonsResponses.livestockOutMigration)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_MILK_PRODUCTION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highMilkProduction.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highMilkProduction.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highMilkProduction.add(selectedMonth)
                    }
                    milkHigh.text = returnMonthInitialsString(lzSeasonsResponses.highMilkProduction)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_MILK_PRODUCTION) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowMilkProduction.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowMilkProduction.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowMilkProduction.add(selectedMonth)
                    }
                    milkLow.text = returnMonthInitialsString(lzSeasonsResponses.lowMilkProduction)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_CALVING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highCalving.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highCalving.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highCalving.add(selectedMonth)
                    }
                    calvingHigh.text = returnMonthInitialsString(lzSeasonsResponses.highCalving)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_CALVING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowCalving.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowCalving.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowCalving.add(selectedMonth)
                    }
                    calvingLow.text = returnMonthInitialsString(lzSeasonsResponses.lowCalving)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_KIDDING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highKidding.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highKidding.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highKidding.add(selectedMonth)
                    }
                    kiddingHigh.text = returnMonthInitialsString(lzSeasonsResponses.highKidding)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_KIDDING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowKidding.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowKidding.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowKidding.add(selectedMonth)
                    }
                    kiddingLow.text = returnMonthInitialsString(lzSeasonsResponses.lowKidding)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_FOOD_PRICES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highFoodPrices.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highFoodPrices.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highFoodPrices.add(selectedMonth)
                    }
                    foodPricesHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highFoodPrices)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_FOOD_PRICES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowFoodPrices.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowFoodPrices.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowFoodPrices.add(selectedMonth)
                    }
                    foodPricesLow.text = returnMonthInitialsString(lzSeasonsResponses.lowFoodPrices)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_LIVESTOCK_PRICES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highLivestockPrices.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highLivestockPrices.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highLivestockPrices.add(selectedMonth)
                    }
                    livestockPricesHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highLivestockPrices)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_LIVESTOCK_PRICES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowLivestockPrices.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowLivestockPrices.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowLivestockPrices.add(selectedMonth)
                    }
                    livestockPricesLow.text =
                        returnMonthInitialsString(lzSeasonsResponses.lowLivestockPrices)
                }


                /* Others */
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_CASUAL_LABOUR_AVAILABILITY) {
                    val doesMonthAlreadyExist =
                        lzSeasonsResponses.highCasualLabourAvailability.filter {
                            it.monthId == selectedMonth.monthId
                        }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highCasualLabourAvailability.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highCasualLabourAvailability.add(selectedMonth)
                    }
                    casualLabourAvailabilityHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highCasualLabourAvailability)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_CASUAL_LABOUR_AVAILABILITY) {
                    val doesMonthAlreadyExist =
                        lzSeasonsResponses.lowCasualLabourAvailability.filter {
                            it.monthId == selectedMonth.monthId
                        }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowCasualLabourAvailability.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowCasualLabourAvailability.add(selectedMonth)
                    }
                    casualLabourAvailabilityLow.text =
                        returnMonthInitialsString(lzSeasonsResponses.lowCasualLabourAvailability)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_CASUAL_LABOUR_WAGES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highCasualLabourWages.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highCasualLabourWages.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highCasualLabourWages.add(selectedMonth)
                    }
                    casualLabourWagesHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highCasualLabourWages)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_CASUAL_LABOUR_WAGES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowCasualLabourWages.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowCasualLabourWages.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowCasualLabourWages.add(selectedMonth)
                    }
                    casualLabourWagesLow.text =
                        returnMonthInitialsString(lzSeasonsResponses.lowCasualLabourWages)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_REMITTANCES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highRemittances.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highRemittances.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highRemittances.add(selectedMonth)
                    }
                    remittancesHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highRemittances)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_REMITTANCES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowRemittances.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowRemittances.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowRemittances.add(selectedMonth)
                    }
                    remittancesLow.text =
                        returnMonthInitialsString(lzSeasonsResponses.lowRemittances)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_FISHING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highFish.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highFish.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highFish.add(selectedMonth)
                    }
                    fishingHigh.text = returnMonthInitialsString(lzSeasonsResponses.highFish)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_FISHING) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowFish.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowFish.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowFish.add(selectedMonth)
                    }
                    fishingLow.text = returnMonthInitialsString(lzSeasonsResponses.lowFish)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_MARKET_ACCESS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highMarketAccess.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highMarketAccess.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highMarketAccess.add(selectedMonth)
                    }
                    marketAccessHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highMarketAccess)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_MARKET_ACCESS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowMarketAccess.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowMarketAccess.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowMarketAccess.add(selectedMonth)
                    }
                    marketAccessLow.text =
                        returnMonthInitialsString(lzSeasonsResponses.lowMarketAccess)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.HIGH_DISEASE_OUTBREAK) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.highDiseaseOutbreak.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.highDiseaseOutbreak.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.highDiseaseOutbreak.add(selectedMonth)
                    }
                    diseaseOutbreakHigh.text =
                        returnMonthInitialsString(lzSeasonsResponses.highDiseaseOutbreak)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LOW_DISEASE_OUTBREAK) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.lowDiseaseOutbreak.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.lowDiseaseOutbreak.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.lowDiseaseOutbreak.add(selectedMonth)
                    }
                    diseaseOutbreakLow.text =
                        returnMonthInitialsString(lzSeasonsResponses.lowDiseaseOutbreak)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.WATER_STRESS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.waterStress.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.waterStress.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.waterStress.add(selectedMonth)
                    }
                    waterStressMonth.text =
                        returnMonthInitialsString(lzSeasonsResponses.waterStress)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CONFLICT_RISK) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.conflictRisks.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.conflictRisks.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.conflictRisks.add(selectedMonth)
                    }
                    conflictRiskMonth.text =
                        returnMonthInitialsString(lzSeasonsResponses.conflictRisks)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.CEREMONIES) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.ceremonies.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.ceremonies.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.ceremonies.add(selectedMonth)
                    }
                    ceremoniesMonth.text = returnMonthInitialsString(lzSeasonsResponses.ceremonies)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.LEAN_SEASONS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.leanSeasons.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.leanSeasons.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.leanSeasons.add(selectedMonth)
                    }
                    leanSeasonsMonth.text =
                        returnMonthInitialsString(lzSeasonsResponses.leanSeasons)
                }
                if (seasonsResponsesEnum == SeasonsResponsesEnum.FOOD_SECURITY_ASSESSMENTS) {
                    val doesMonthAlreadyExist = lzSeasonsResponses.foodSecurityAssessments.filter {
                        it.monthId == selectedMonth.monthId
                    }.isNotEmpty()

                    if (doesMonthAlreadyExist) {
                        lzSeasonsResponses.foodSecurityAssessments.remove(selectedMonth)
                    } else {
                        lzSeasonsResponses.foodSecurityAssessments.add(selectedMonth)
                    }
                    foodSecurityMonth.text =
                        returnMonthInitialsString(lzSeasonsResponses.foodSecurityAssessments)
                }
            }
        }


//        (seasonCalendarDialog as android.app.AlertDialog).dismiss()
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

    override fun onCurrentCropHasNoError(
        lzCropProductionResponseItem: LzCropProductionResponseItem
    ) {
        countyLevelQuestionnaire.lzCropProductionResponses.cropProductionResponses.add(
            lzCropProductionResponseItem
        )
        System.out.println()
    }

    override fun onACropHasAValidationError() {
        inflateErrorModal("Missing data", "Kindly fill in all the data in the form")
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

    fun returnAppropriateCropPercentagErrorMessage(): String {
        if (doesLongRainsRainFedCropsHavePercentageError().hasAPercentageError) {
            return "Long rains rainfed crops has a percentage error. The value " + isGreaterThanOrLessThan(
                doesLongRainsRainFedCropsHavePercentageError().variationStatus
            ) + " by " + doesLongRainsRainFedCropsHavePercentageError().differenceValue.toString()
        } else if (doesLongRainsIrrigatedCropsHavePercentageError().hasAPercentageError) {
            return "Long rains irrigated crops has a percentage error. The value " + isGreaterThanOrLessThan(
                doesLongRainsIrrigatedCropsHavePercentageError().variationStatus
            ) + " by " + doesLongRainsIrrigatedCropsHavePercentageError().differenceValue.toString()
        } else if (doesShortRainFedCropsHavePercentageError().hasAPercentageError) {
            return "Short rains rainfed crops has a percentage error. The value " + isGreaterThanOrLessThan(
                doesShortRainFedCropsHavePercentageError().variationStatus
            ) + " by " + doesShortRainFedCropsHavePercentageError().differenceValue.toString()
        } else if (doesShortRainsIrrigatedCropsHavePercentageError().hasAPercentageError) {
            return "Short rains irrigated crops has a percentage error. The value " + isGreaterThanOrLessThan(
                doesShortRainsIrrigatedCropsHavePercentageError().variationStatus
            ) + " by " + doesShortRainsIrrigatedCropsHavePercentageError().differenceValue.toString()
        }
        return ""
    }

    fun doesCropProductionHavePercentageErrors(): Boolean {
        return doesLongRainsRainFedCropsHavePercentageError().hasAPercentageError || doesLongRainsIrrigatedCropsHavePercentageError().hasAPercentageError
                || doesShortRainFedCropsHavePercentageError().hasAPercentageError || doesShortRainsIrrigatedCropsHavePercentageError().hasAPercentageError
    }

    fun isGreaterThanOrLessThan(percentageValidationEnum: PercentageValidationEnum): String {
        return if (percentageValidationEnum == PercentageValidationEnum.HIGH) " is greater than" else " is less than"
    }

    fun doesLongRainsRainFedCropsHavePercentageError(): CropPercentageValidationModel {
        var percentageValue: Double = 0.0
        for (currentResponseItem in cropProductionResponseItems) {
            percentageValue =
                percentageValue + currentResponseItem.longRainsSeason.rainfedCultivatedAreaPercentage.value
        }

        if (percentageValue != 100.0) {

            if (percentageValue > 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.HIGH,
                    percentageValue - 100.0
                )
            }

            if (percentageValue < 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.LOW,
                    100.0 - percentageValue
                )
            }

        }

        return CropPercentageValidationModel(
            false,
            PercentageValidationEnum.EXACT,
            0.0
        )
    }

    fun doesLongRainsIrrigatedCropsHavePercentageError(): CropPercentageValidationModel {
        var percentageValue: Double = 0.0
        for (currentResponseItem in cropProductionResponseItems) {
            percentageValue =
                percentageValue + currentResponseItem.longRainsSeason.irrigatedCultivatedArea.value
        }

        if (percentageValue != 100.0) {

            if (percentageValue > 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.HIGH,
                    percentageValue - 100.0
                )
            }

            if (percentageValue < 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.LOW,
                    100.0 - percentageValue
                )
            }

        }

        return CropPercentageValidationModel(
            false,
            PercentageValidationEnum.EXACT,
            0.0
        )
    }


    fun doesShortRainFedCropsHavePercentageError(): CropPercentageValidationModel {
        var percentageValue: Double = 0.0
        for (currentResponseItem in cropProductionResponseItems) {
            percentageValue =
                percentageValue + currentResponseItem.shortRainsSeason.rainfedCultivatedAreaPercentage.value
        }

        if (percentageValue != 100.0) {

            if (percentageValue > 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.HIGH,
                    percentageValue - 100.0
                )
            }

            if (percentageValue < 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.LOW,
                    100.0 - percentageValue
                )
            }

        }

        return CropPercentageValidationModel(
            false,
            PercentageValidationEnum.EXACT,
            0.0
        )
    }


    fun doesShortRainsIrrigatedCropsHavePercentageError(): CropPercentageValidationModel {
        var percentageValue: Double = 0.0
        for (currentResponseItem in cropProductionResponseItems) {
            percentageValue =
                percentageValue + currentResponseItem.shortRainsSeason.irrigatedCultivatedArea.value
        }

        if (percentageValue != 100.0) {

            if (percentageValue > 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.HIGH,
                    percentageValue - 100.0
                )
            }

            if (percentageValue < 100.0) {
                return CropPercentageValidationModel(
                    true,
                    PercentageValidationEnum.LOW,
                    100.0 - percentageValue
                )
            }

        }

        return CropPercentageValidationModel(
            false,
            PercentageValidationEnum.EXACT,
            0.0
        )
    }

    override fun onCropItemSelectedFromSelectionList(selectedCrop: CropModel, position: Int) {
        crops.set(position, selectedCrop)
        binding.apply {
            cropSelectionLayout.apply {
                activity?.let { context ->
                    val adapter =
                        CropSelectionListAdapter(
                            context,
                            R.layout.lz_selection_item,
                            crops,
                            this@CountyLevelFragment
                        )
                    cropsList.adapter = adapter
                    cropsList.setSelection(position)
                }
            }
        }
        if (selectedCrop.hasBeenSelected) {
            countyLevelQuestionnaire.selectedCrops.add(selectedCrop)
        } else {
            countyLevelQuestionnaire.selectedCrops.remove(selectedCrop)
        }
    }

    override fun onCropProductionResponseItemSubmited(
        responseItem: WgCropProductionResponseItem,
        position: Int
    ) {
        cropProductionResponseItems.set(position, responseItem)
    }

    override fun onATribeSelected(currentTribe: EthnicGroupModel, position: Int) {
        ethnicGroups.set(position, currentTribe)
        if (currentTribe.hasBeenSelected) {
            countyLevelQuestionnaire.livelihoodZoneEthnicGroups.add(currentTribe)
        } else {
            countyLevelQuestionnaire.livelihoodZoneEthnicGroups.remove(currentTribe)
        }
        binding.apply {
            ethnicGroupSelection.apply {
                activity?.let { context ->
                    val adapter =
                        TribesListViewAdapter(
                            context,
                            R.layout.lz_selection_item,
                            ethnicGroups,
                            this@CountyLevelFragment
                        )
                    tribesList.adapter = adapter
                    tribesList.setSelection(position)
                }
            }
        }

    }


    private fun inflateHazardsRankModal(
        hazardsRanks: MutableList<RankResponseItem>,
        hazardTypeEnum: HazardTypeEnum
    ) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)
        val list: RecyclerView = v.findViewById(R.id.listRv)

        val ranksAdapter =
            HazardsRankingAdapter(
                hazardsRanks,
                this,
                hazardTypeEnum
            )
        val gridLayoutManager = GridLayoutManager(context, 1)
        list.layoutManager = gridLayoutManager
        list.hasFixedSize()
        list.adapter = ranksAdapter

        openHazardsRankModal(v)
    }

    private fun openHazardsRankModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setView(v)
        builder.setCancelable(true)
        hazardsRankingDialog = builder.create()
        (hazardsRankingDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (hazardsRankingDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (hazardsRankingDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (hazardsRankingDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (hazardsRankingDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAHazardRankItemSelected(
        selectedRankItem: RankResponseItem,
        position: Int,
        hazardTypeEnum: HazardTypeEnum
    ) {
        hazardsRanks.remove(selectedRankItem)
        binding.apply {

            lzHazards.apply {

                if (hazardTypeEnum == HazardTypeEnum.ANIMAL_RUSTLING) {
                    animalRustlingRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.animalRustling.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.BANDITRY) {
                    banditryRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.banditry.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.TERRORISM) {
                    terrorismRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.terrorism.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.ETHNIC_CONFLICT) {
                    ethicConflictRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.ethnicConflict.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.POLITICAL_CONFLICT) {
                    politicalViolenceRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.politicalViolence.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.DROUGHT) {
                    droughtRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.drought.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.LIVESTOCK_PESTS_DISEASES) {
                    pestAndDiseaseRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.livestockPestsAndDiseases.importanceRank =
                        selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.HAILSTORMS) {
                    hailstormsOrFrostRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.hailstormsOrFrost.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.FLOODING) {
                    floodingRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.flooding.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.LANDSLIDES) {
                    landslidesRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.landslides.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.HIGH_WINDS) {
                    windsOrCycloneRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.highWindsOrCyclones.importanceRank =
                        selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.BUSH_FIRES) {
                    bushFiresRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.bushFires.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.CROP_PESTS) {
                    cropPestsRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.cropPests.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.LOCUST_INVASION) {
                    locustInvasionRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.locustInvasion.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.CROP_DISEASES) {
                    cropDiseasesRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.cropDiseases.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.TERMINAL_ILLNESS) {
                    terminalIllnessRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.terminalIllnesses.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.MALARIA) {
                    malariaOutbreakRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.malariaPowerOutBreak.importanceRank =
                        selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.WATERBORNE_DISEASES) {
                    waterBorneDiseaseRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.waterBornDiseases.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.HUMAN_WILDLIFE_CONFLICT) {
                    humanWildlifeConflictRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.humanWildlifeConflict.importanceRank =
                        selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.HIGH_FOOD_PRICES) {
                    highFoodPriceRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.highFoodPrices.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.FOOD_SHORTAGE) {
                    foodShortageRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.marketFoodShortages.importanceRank =
                        selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.DRINKING_WATER_SHORTAGE) {
                    drinkingWaterShortageRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.drinkingWaterShortages.importanceRank =
                        selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.INVASIVE_PLANTS) {
                    invasivePlantsRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.invasivePlants.importanceRank = selectedRankItem.rankPosition
                }
                if (hazardTypeEnum == HazardTypeEnum.OTHERS) {
                    othersRank.text = selectedRankItem.rankPosition.toString()
                    hazardResponses.others.importanceRank = selectedRankItem.rankPosition
                }
            }
        }

        (hazardsRankingDialog as androidx.appcompat.app.AlertDialog).dismiss()
    }

    override fun onAZoneCharectaristicsSubmitted(
        currentResponseItem: ZoneCharectaristicsResponseItem,
        position: Int
    ) {
        zoneCharectaristicsItemsList.set(position,currentResponseItem)
    }
}