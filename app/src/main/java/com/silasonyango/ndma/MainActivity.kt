package com.silasonyango.ndma

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.*
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.ui.county.destinations.CountyLevelFragment
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.county.model.SubCountyModel
import com.silasonyango.ndma.ui.county.model.SubLocationModel
import com.silasonyango.ndma.ui.county.model.WardModel
import com.silasonyango.ndma.ui.home.adapters.CountyQuestionnaireAdapter
import com.silasonyango.ndma.ui.home.adapters.SubCountyAdapter
import com.silasonyango.ndma.ui.home.adapters.SubLocationAdapter
import com.silasonyango.ndma.ui.home.adapters.WardAdapter
import com.silasonyango.ndma.ui.wealthgroup.WealthGroupDialogFragment
import com.silasonyango.ndma.util.Util

class MainActivity : AppCompatActivity(), SubCountyAdapter.SubCountyAdapterCallBack, WardAdapter.WardAdapterCallBack, SubLocationAdapter.SubLocationAdapterCallBack {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var questionnaireMenuDialog: android.app.AlertDialog? = null

    private var geographyDialog: android.app.AlertDialog? = null

    private var subCountyDialog: android.app.AlertDialog? = null

    private var wardDialog: android.app.AlertDialog? = null

    private var subLocationDialog: android.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val sharedPreferences: SharedPreferences? = baseContext?.applicationContext?.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor? =  sharedPreferences?.edit()
        val gson = Gson()

        if (sharedPreferences?.getString(Constants.QUESTIONNAIRES_LIST_OBJECT, null).isNullOrEmpty()) {
            val countyLevelQuestionnaireListObject = CountyLevelQuestionnaireListObject()

            val responsesJson: String = gson.toJson(countyLevelQuestionnaireListObject)
            editor?.putString(Constants.QUESTIONNAIRES_LIST_OBJECT, responsesJson)
            editor?.commit()
        }

        if (sharedPreferences?.getString(Constants.WEALTH_GROUP_LIST_OBJECT, null).isNullOrEmpty()) {
            val wealthGroupQuestionnaireListObject = WealthGroupQuestionnaireListObject()

            val responsesJson: String = gson.toJson(wealthGroupQuestionnaireListObject)
            editor?.putString(Constants.WEALTH_GROUP_LIST_OBJECT, responsesJson)
            editor?.commit()
        }

//        val cropModel = CropModel("casava",1)
//        val testJson: String = gson.toJson(cropModel)
//        editor?.putString("crop", testJson)
//        editor?.commit()
//
//        val returnedCropString = sharedPreferences?.getString("crop", null)
//
//        val returnedCrop: CropModel =
//            gson.fromJson(returnedCropString, CropModel::class.java)

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
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
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
            menuContent.visibility = View.GONE
            questionnaireNameWrapper.visibility = View.VISIBLE
            modalTitle.text = "Questionnaire Name"
            selectedQuestionnaireType = QuestionnaireType.COUNTY_LEVEL_QUESTIONNAIRE
        }

        wealthGroupMenuItem.setOnClickListener {
            menuContent.visibility = View.GONE
            questionnaireNameWrapper.visibility = View.VISIBLE
            modalTitle.text = "Questionnaire Name"
            selectedQuestionnaireType = QuestionnaireType.WEALTH_GROUP_QUESTIONNAIRE
        }

        submitButton.setOnClickListener {
            if (selectedQuestionnaireType == QuestionnaireType.COUNTY_LEVEL_QUESTIONNAIRE) {
                val questionnaireId = Util.generateUniqueId()
                AppStore.getInstance().countyLevelQuestionnairesList.add(CountyLevelQuestionnaire(
                        questionnaireId,
                        etQuestionnaireName.text.toString()
                ))
                val countyLevelDialogFragment = CountyLevelFragment.newInstance(questionnaireId,etQuestionnaireName.text.toString())
                countyLevelDialogFragment.show(this.supportFragmentManager, "CountyLevel")
            } else {
                val questionnaireId = Util.generateUniqueId()
                AppStore.getInstance().wealthGroupQuestionnaireList.add(WealthGroupQuestionnaire(
                        questionnaireId,
                        etQuestionnaireName.text.toString()
                ))
                inflateGeographyDialog()
//                val wealthGroupDialogFragment = WealthGroupDialogFragment.newInstance(questionnaireId,etQuestionnaireName.text.toString())
//                wealthGroupDialogFragment.show(this.supportFragmentManager, "CountyLevel")
            }

            (questionnaireMenuDialog as android.app.AlertDialog).dismiss()
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


    private fun inflateGeographyDialog() {
        val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.geographic_configuration_layout, null)

        val subCountyDropDown = v.findViewById<View>(R.id.subCountyDropDown)
        val wardDropDown = v.findViewById<View>(R.id.wardDropDown)
        val subLocationDropDown = v.findViewById<View>(R.id.subLocationDropDown)

        val subCounties: MutableList<SubCountyModel> = ArrayList()
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))
        subCounties.add(SubCountyModel("Laikipia", 0))

        val wardModelList: MutableList<WardModel> = ArrayList()
        wardModelList.add(WardModel("Kisumu",0))
        wardModelList.add(WardModel("Kisumu",0))
        wardModelList.add(WardModel("Kisumu",0))
        wardModelList.add(WardModel("Kisumu",0))

        val sublocationList: MutableList<SubLocationModel> = ArrayList()
        sublocationList.add(SubLocationModel("Samburu", 1))
        sublocationList.add(SubLocationModel( "Samburu", 8))
        sublocationList.add(SubLocationModel( "Samburu", 8))
        sublocationList.add(SubLocationModel("Samburu", 9))
        sublocationList.add(SubLocationModel( "Samburu", 0))

        subCountyDropDown.setOnClickListener {
            inflateSubCountyModal(subCounties)
        }

        wardDropDown.setOnClickListener {
            inflateWardModal(wardModelList)
        }

        subLocationDropDown.setOnClickListener {
            inflateSubLocationModal(sublocationList)
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
}