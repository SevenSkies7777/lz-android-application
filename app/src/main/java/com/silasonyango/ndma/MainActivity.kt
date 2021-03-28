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
import com.google.gson.Gson
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.appStore.model.QuestionnaireType
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.ui.county.destinations.CountyLevelFragment
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.wealthgroup.WealthGroupDialogFragment
import com.silasonyango.ndma.util.Util

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var questionnaireMenuDialog: android.app.AlertDialog? = null

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
                val wealthGroupDialogFragment = WealthGroupDialogFragment.newInstance(questionnaireId)
                wealthGroupDialogFragment.show(this.supportFragmentManager, "CountyLevel")
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
}