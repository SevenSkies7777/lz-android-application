package com.ndma.livelihoodzones

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ndma.livelihoodzones.appStore.AppStore
import com.ndma.livelihoodzones.appStore.model.*
import com.ndma.livelihoodzones.config.Constants
import com.ndma.livelihoodzones.config.Constants.RESUME_QUESTIONNAIRE_ID
import com.ndma.livelihoodzones.login.model.GeographyObject
import com.ndma.livelihoodzones.ui.county.destinations.CountyLevelFragment
import com.ndma.livelihoodzones.ui.county.model.*
import com.ndma.livelihoodzones.ui.home.adapters.*
import com.ndma.livelihoodzones.ui.wealthgroup.WealthGroupDialogFragment
import com.ndma.livelihoodzones.util.GpsTracker
import com.ndma.livelihoodzones.util.Util

class MainActivity : AppCompatActivity(), SubCountyAdapter.SubCountyAdapterCallBack,
    WardAdapter.WardAdapterCallBack, SubLocationAdapter.SubLocationAdapterCallBack,
    WealthGroupAdapter.WealthGroupAdapterCallBack,
    LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack,
    WgQuestionnaireTypeAdapter.WgQuestionnaireTypeAdapterCallBack {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var questionnaireMenuDialog: android.app.AlertDialog? = null

    private var geographyDialog: android.app.AlertDialog? = null

    private var subCountyDialog: android.app.AlertDialog? = null

    private var wardDialog: android.app.AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    private var subLocationDialog: android.app.AlertDialog? = null

    private var wealthGroupDialog: android.app.AlertDialog? = null

    private var livelihoodZoneAlertDialog: android.app.AlertDialog? = null

    private var questionnaireTypeAlertDialog: android.app.AlertDialog? = null

    var questionnaireSessionLocation: QuestionnaireSessionLocation = QuestionnaireSessionLocation()

    lateinit var selectedSubCountyText: TextView

    lateinit var selectedWardText: TextView

    lateinit var selectedSubLocationText: TextView

    lateinit var selectedWealthGroupText: TextView

    lateinit var questionnaireTypeText: TextView

    lateinit var livelihoodZoneText: TextView

    lateinit var questionnaireId: String

    lateinit var questionnaireName: String

    lateinit var selectedSubCounty: SubCountyModel

    lateinit var selectedWard: WardModel

    var resumeZonalQuestionnaireBroadCastReceiver: BroadcastReceiver? = null

    var dialogDismissBroadCastReceiver: BroadcastReceiver? = null

    val WRITE_STORAGE_PERMISSION_CODE: Int = 100

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)


        dialogDismissBroadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                when (intent?.action) {
                    Constants.DISMISS_MAIN_ACTIVITY_DIALOGS -> {
                        questionnaireMenuDialog?.dismiss()
                        geographyDialog?.dismiss()
                    }
                }
            }
        }


        val filter = IntentFilter()
        filter.addAction(Constants.DISMISS_MAIN_ACTIVITY_DIALOGS)
        this.applicationContext?.registerReceiver(dialogDismissBroadCastReceiver, filter)


        resumeZonalQuestionnaireBroadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                when (intent?.action) {
                    Constants.RESUME_ZONAL_QUESTIONNAIRE -> intent.getStringExtra(RESUME_QUESTIONNAIRE_ID)?.let {
                        handleZonalQuestionnaireResume(it)
                    }
                    Constants.RESUME_WEALTH_GROUP_QUESTIONNAIRE -> intent.getStringExtra(RESUME_QUESTIONNAIRE_ID)?.let {
                        handleWealthGroupQuestionnaireResume(it)
                    }
                }
            }
        }

        val resumeZonalQuestionnaireFilter = IntentFilter()
        resumeZonalQuestionnaireFilter.addAction(Constants.RESUME_ZONAL_QUESTIONNAIRE)
        resumeZonalQuestionnaireFilter.addAction(Constants.RESUME_WEALTH_GROUP_QUESTIONNAIRE)
        this.applicationContext?.registerReceiver(resumeZonalQuestionnaireBroadCastReceiver, resumeZonalQuestionnaireFilter)

        val sharedPreferences: SharedPreferences? =
            baseContext?.applicationContext?.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        val gson = Gson()

        if (sharedPreferences?.getString(Constants.QUESTIONNAIRES_LIST_OBJECT, null)
                .isNullOrEmpty()
        ) {
            val countyLevelQuestionnaireListObject = CountyLevelQuestionnaireListObject()

            val responsesJson: String = gson.toJson(countyLevelQuestionnaireListObject)
            editor?.putString(Constants.QUESTIONNAIRES_LIST_OBJECT, responsesJson)
            editor?.commit()
        }

        if (sharedPreferences?.getString(Constants.WEALTH_GROUP_LIST_OBJECT, null)
                .isNullOrEmpty()
        ) {
            val wealthGroupQuestionnaireListObject = WealthGroupQuestionnaireListObject()

            val responsesJson: String = gson.toJson(wealthGroupQuestionnaireListObject)
            editor?.putString(Constants.WEALTH_GROUP_LIST_OBJECT, responsesJson)
            editor?.commit()
        }

        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            inflateQuestionnaireMenuModal()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun inflateQuestionnaireMenuModal() {
        lateinit var selectedQuestionnaireType: QuestionnaireType
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.questionnaire_menu_layout, null)
        val countyLevelMenuItem: View = v.findViewById(R.id.countyLevelWrapper)
        val wealthGroupMenuItem: View = v.findViewById(R.id.wealthGroupWrapper)
        val tvModalMessage: View = v.findViewById(R.id.wealthGroupWrapper)
        val menuContent: View = v.findViewById(R.id.menuWrapper)
        val questionnaireNameWrapper: View = v.findViewById(R.id.questionnaireName)
        val modalTitle: TextView = v.findViewById(R.id.modalTitle)
        val submitButton: TextView = v.findViewById(R.id.submit)
        val etQuestionnaireName: EditText = v.findViewById(R.id.etQuestionnaireName)

        countyLevelMenuItem.setOnClickListener {
            val questionnaireId = Util.generateUniqueId()
            AppStore.getInstance().countyLevelQuestionnairesList.add(
                CountyLevelQuestionnaire(
                    questionnaireId,
                    "none"
                )
            )
            val countyLevelDialogFragment = CountyLevelFragment.newInstance(
                questionnaireId,
                "none",
                false
            )
            countyLevelDialogFragment.show(this.supportFragmentManager, "CountyLevel")
        }

        wealthGroupMenuItem.setOnClickListener {
            val questionnaireId = Util.generateUniqueId()
            AppStore.getInstance().wealthGroupQuestionnaireList.add(
                WealthGroupQuestionnaire(
                    questionnaireId,
                    "none"
                )
            )
            this.questionnaireId = questionnaireId
            this.questionnaireName = "none"
            inflateGeographyDialog()
        }

        openQuestionnaireMenuModal(v)
    }

    private fun openQuestionnaireMenuModal(v: View) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        questionnaireMenuDialog = builder.create()
        (questionnaireMenuDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogBottom(questionnaireMenuDialog as AlertDialog)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

    }

    fun dialogBottom(dialog: Dialog) {
        val window = dialog.window
        val wlp = window?.attributes?.apply {
            gravity = Gravity.BOTTOM
        }
        window?.attributes = wlp
    }

    fun dialogCenter(dialog: Dialog) {
        val window = dialog.window
        val wlp = window?.attributes?.apply {
            gravity = Gravity.CENTER
        }
        window?.attributes = wlp
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun inflateGeographyDialog() {
        val gson = Gson()
        val sharedPreferences: SharedPreferences? =
            this.applicationContext?.getSharedPreferences(
                "MyPref",
                Context.MODE_PRIVATE
            )
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        val geographyString =
            sharedPreferences?.getString(Constants.GEOGRAPHY_OBJECT, null)
        val geographyObject: GeographyObject =
            gson.fromJson(
                geographyString,
                GeographyObject::class.java
            )
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.geographic_configuration_layout, null)

        val subCountyDropDown = v.findViewById<View>(R.id.subCountyDropDown)
        val wardDropDown = v.findViewById<View>(R.id.wardDropDown)
        val subLocationDropDown = v.findViewById<View>(R.id.subLocationDropDown)
        val wealthGroupDropDown = v.findViewById<View>(R.id.wealthGroupDropDown)
        val submitButton = v.findViewById<TextView>(R.id.geographySubmitButton)
        val livelihoodZoneDropDown = v.findViewById<View>(R.id.livelihoodZoneDropDown)
        val questionnaireTypeDropDown = v.findViewById<View>(R.id.questionnaireTypeDropDown)
        selectedSubCountyText = v.findViewById<TextView>(R.id.subCountyText)
        selectedWardText = v.findViewById<TextView>(R.id.wardText)
        selectedSubLocationText = v.findViewById<TextView>(R.id.subLocationText)
        selectedWealthGroupText = v.findViewById<TextView>(R.id.wealthGroupText)
        questionnaireTypeText = v.findViewById<TextView>(R.id.questionnaireTypeText)
        livelihoodZoneText = v.findViewById<TextView>(R.id.livelihoodZoneText)


        val wealthGroupModelList: MutableList<WealthGroupModel> = ArrayList()
        wealthGroupModelList.add(WealthGroupModel("Very Poor", 1))
        wealthGroupModelList.add(WealthGroupModel("Poor", 2))
        wealthGroupModelList.add(WealthGroupModel("Medium", 3))
        wealthGroupModelList.add(WealthGroupModel("Better Off", 4))

        val wgQuestionnaireTypesList: MutableList<WgQuestionnaireTypeModel> = ArrayList()
        wgQuestionnaireTypesList.add(WgQuestionnaireTypeModel(0, "Summarized questionnaire", 1))
        wgQuestionnaireTypesList.add(WgQuestionnaireTypeModel(0, "Raw data questionnaire", 2))

        subCountyDropDown.setOnClickListener {
            inflateSubCountyModal(geographyObject.county.subCounties)
        }

        wardDropDown.setOnClickListener {
            inflateWardModal(selectedSubCounty.wards)
        }

        subLocationDropDown.setOnClickListener {
            inflateSubLocationModal(selectedWard.subLocations)
        }

        wealthGroupDropDown.setOnClickListener {
            inflateWealthGroupModal(wealthGroupModelList)
        }

        questionnaireTypeDropDown.setOnClickListener {
            inflateWgQuestionnaireTypeModal(wgQuestionnaireTypesList)
        }

        livelihoodZoneDropDown.setOnClickListener {
            inflateLivelihoodZoneModal(geographyObject.currentUserAssignedCountyLivelihoodZones)
        }

        submitButton.setOnClickListener {

            if (questionnaireSessionLocation.selectedSubCounty != null
                && questionnaireSessionLocation.selectedWard != null
                && questionnaireSessionLocation.selectedSubLocation != null
                && questionnaireSessionLocation.selectedLivelihoodZone != null
                && questionnaireSessionLocation.selectedWgQuestionnaireType != null
            ) {

                var latitude: Double = 0.0
                var longitude: Double = 0.0
                val gpsTracker: GpsTracker = GpsTracker(this)
                if (isStoragePermissionGranted()) {
                    latitude = gpsTracker.latitude
                    longitude = gpsTracker.longitude
                    questionnaireSessionLocation.latitude = latitude
                    questionnaireSessionLocation.longitude = longitude
                    val wealthGroupDialogFragment = WealthGroupDialogFragment.newInstance(
                        questionnaireId,
                        questionnaireName,
                        questionnaireSessionLocation,
                        false
                    )
                    wealthGroupDialogFragment.show(this.supportFragmentManager, "WealthGroup")
                }

            } else {
                inflateErrorModal("Data error", "Kindly fill out the missing fields")
            }
        }

        openGeographyModal(v)
    }

    private fun openGeographyModal(v: View) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        geographyDialog = builder.create()
        (geographyDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogBottom(geographyDialog as AlertDialog)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

    }

    private fun inflateSubCountyModal(subCounties: MutableList<SubCountyModel>) {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val countyQuestionnaireAdapter = SubCountyAdapter(
            subCounties,
            this
        )
        val gridLayoutManager = GridLayoutManager(this, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter =
            countyQuestionnaireAdapter

        openSubCountyModal(v)
    }

    private fun openSubCountyModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        subCountyDialog = builder.create()
        (subCountyDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogCenter(subCountyDialog as AlertDialog)
            window?.setLayout(
                width,
                height
            )
        }

    }


    private fun inflateWardModal(wardModelList: MutableList<WardModel>) {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val WardAdapter = WardAdapter(
            wardModelList,
            this
        )
        val gridLayoutManager = GridLayoutManager(this, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = WardAdapter

        openWardModal(v)
    }

    private fun openWardModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        wardDialog = builder.create()
        (wardDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogCenter(wardDialog as AlertDialog)
            window?.setLayout(
                width,
                height
            )
        }

    }


    private fun inflateSubLocationModal(subLocationModalList: MutableList<SubLocationModel>) {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val subLocationAdapter = SubLocationAdapter(
            subLocationModalList,
            this
        )
        val gridLayoutManager = GridLayoutManager(this, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = subLocationAdapter

        openSubLocationModal(v)
    }

    private fun openSubLocationModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        subLocationDialog = builder.create()
        (subLocationDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogCenter(subLocationDialog as AlertDialog)
            window?.setLayout(
                width,
                height
            )
        }

    }


    private fun inflateWealthGroupModal(wealthGroupModelList: MutableList<WealthGroupModel>) {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val wealthGroupAdapter = WealthGroupAdapter(
            wealthGroupModelList,
            this
        )
        val gridLayoutManager = GridLayoutManager(this, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = wealthGroupAdapter

        openWealthGroupModal(v)
    }

    private fun openWealthGroupModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        wealthGroupDialog = builder.create()
        (wealthGroupDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogCenter(wealthGroupDialog as AlertDialog)
            window?.setLayout(
                width,
                height
            )
        }

    }


    private fun inflateLivelihoodZoneModal(livelihoodZoneModelList: MutableList<LivelihoodZoneModel>) {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val lzAdapter = LivelihoodZonesAdapter(
            livelihoodZoneModelList,
            this
        )
        val gridLayoutManager = GridLayoutManager(this, 1)
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

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        livelihoodZoneAlertDialog = builder.create()
        (livelihoodZoneAlertDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogCenter(livelihoodZoneAlertDialog as AlertDialog)
            window?.setLayout(
                width,
                height
            )
        }

    }


    private fun inflateWgQuestionnaireTypeModal(wgQuestionnaireTypeList: MutableList<WgQuestionnaireTypeModel>) {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val lzAdapter = WgQuestionnaireTypeAdapter(
            wgQuestionnaireTypeList,
            this
        )
        val gridLayoutManager = GridLayoutManager(this, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = lzAdapter

        openWgQuestionnaireTypeModal(v)
    }

    private fun openWgQuestionnaireTypeModal(v: View) {
        val width =
            (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setView(v)
        builder.setCancelable(true)
        questionnaireTypeAlertDialog = builder.create()
        (questionnaireTypeAlertDialog as android.app.AlertDialog).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            dialogCenter(questionnaireTypeAlertDialog as AlertDialog)
            window?.setLayout(
                width,
                height
            )
        }

    }

    override fun onSubCountyItemClicked(selectedSubCounty: SubCountyModel) {
        questionnaireSessionLocation.selectedSubCounty = selectedSubCounty
        selectedSubCountyText.text = selectedSubCounty.subCountyName
        this.selectedSubCounty = selectedSubCounty
        (subCountyDialog as android.app.AlertDialog).dismiss()
    }

    override fun onWardItemClicked(selectedWard: WardModel) {
        questionnaireSessionLocation.selectedWard = selectedWard
        selectedWardText.text = selectedWard.wardName
        this.selectedWard = selectedWard
        (wardDialog as android.app.AlertDialog).dismiss()
    }

    override fun onSubLocationItemClicked(selectedSubLocation: SubLocationModel) {
        questionnaireSessionLocation.selectedSubLocation = selectedSubLocation
        selectedSubLocationText.text = selectedSubLocation.subLocationName
        (subLocationDialog as android.app.AlertDialog).dismiss()
    }

    override fun onWealthGroupItemClicked(selectedWealthGroup: WealthGroupModel) {
        questionnaireSessionLocation.selectedWealthGroup = selectedWealthGroup
        selectedWealthGroupText.text = selectedWealthGroup.wealthGroupName
        (wealthGroupDialog as android.app.AlertDialog).dismiss()
    }

    override fun onLivelihoodZoneItemClicked(selectedLivelihoodZone: LivelihoodZoneModel) {
        questionnaireSessionLocation.selectedLivelihoodZone = selectedLivelihoodZone
        livelihoodZoneText.text = selectedLivelihoodZone.livelihoodZoneName
        (livelihoodZoneAlertDialog as android.app.AlertDialog).dismiss()
    }

    override fun onWgQuestionnaireTypeItemClicked(selectedWgQuestionnaireType: WgQuestionnaireTypeModel) {
        questionnaireSessionLocation.selectedWgQuestionnaireType = selectedWgQuestionnaireType
        questionnaireTypeText.text = selectedWgQuestionnaireType.wgQuestionnaireTypeDescription
        (questionnaireTypeAlertDialog as android.app.AlertDialog).dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isStoragePermissionGranted(): Boolean {
        val scopedActivity = this

        val isPermissionGranted = ContextCompat.checkSelfPermission(
            scopedActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

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

            if (questionnaireSessionLocation.selectedSubCounty != null
                && questionnaireSessionLocation.selectedWard != null
                && questionnaireSessionLocation.selectedSubLocation != null
                && questionnaireSessionLocation.selectedLivelihoodZone != null
                && questionnaireSessionLocation.selectedWgQuestionnaireType != null
            ) {

                var latitude: Double = 0.0
                var longitude: Double = 0.0
                val gpsTracker: GpsTracker = GpsTracker(this)

                latitude = gpsTracker.latitude
                longitude = gpsTracker.longitude
                questionnaireSessionLocation.latitude = latitude
                questionnaireSessionLocation.longitude = longitude
                val wealthGroupDialogFragment = WealthGroupDialogFragment.newInstance(
                    questionnaireId,
                    questionnaireName,
                    questionnaireSessionLocation,
                    false
                )
                wealthGroupDialogFragment.show(this.supportFragmentManager, "WealthGroup")

            } else {
                inflateErrorModal("Data error", "Kindly fill out all the missing fields")
            }

        }
    }

    private fun inflateErrorModal(errorTitle: String, errorMessage: String) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
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

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
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

    override fun onDestroy() {
        super.onDestroy()
        this.applicationContext?.unregisterReceiver(resumeZonalQuestionnaireBroadCastReceiver)
        this.applicationContext?.unregisterReceiver(dialogDismissBroadCastReceiver)
    }

    fun handleZonalQuestionnaireResume(resumeQuestionnaireId: String) {
        val countyLevelDialogFragment = CountyLevelFragment.newInstance(
            resumeQuestionnaireId,
            "none",
            true
        )
        countyLevelDialogFragment.show(this.supportFragmentManager, "CountyLevel")
    }

    fun handleWealthGroupQuestionnaireResume(questionnaireId: String) {
        val wealthGroupDialogFragment = WealthGroupDialogFragment.newInstance(
            questionnaireId,
            "none",
            null,
            true
        )
        wealthGroupDialogFragment.show(this.supportFragmentManager, "WealthGroup")
    }


}