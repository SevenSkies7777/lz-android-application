package com.silasonyango.ndma.ui.home

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.silasonyango.ndma.MainActivity
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaire
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.config.Constants.QUESTIONNAIRE_COMPLETED
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.databinding.FragmentHomeBinding
import com.silasonyango.ndma.login.model.LoginResponseModel
import com.silasonyango.ndma.services.model.Status
import com.silasonyango.ndma.ui.county.adapters.LzCropProductionRecyclerViewAdapter
import com.silasonyango.ndma.ui.home.adapters.CountyQuestionnaireAdapter
import com.silasonyango.ndma.ui.home.adapters.WealthGroupQuestionnaireAdapter
import com.silasonyango.ndma.ui.model.QuestionnaireApiResponse
import com.silasonyango.ndma.ui.model.QuestionnaireStatus

class HomeFragment : Fragment(), CountyQuestionnaireAdapter.CountyQuestionnaireAdapterCallBack, WealthGroupQuestionnaireAdapter.WealthGroupQuestionnaireAdapterCallBack {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var binding: FragmentHomeBinding

    var broadCastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                when (intent?.action) {
                    QUESTIONNAIRE_COMPLETED -> handleQuestionnaireCompleted()
                }
            }
        }

        activity?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(broadCastReceiver as BroadcastReceiver, IntentFilter(QUESTIONNAIRE_COMPLETED))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        activity?.let {
            broadCastReceiver?.let { it1 ->
                LocalBroadcastManager.getInstance(it)
                    .unregisterReceiver(it1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        populateQuestionnairesList()
        populateWealthGroupQuestionnairesList()



        return binding.root
    }

    private fun handleQuestionnaireCompleted() {
        populateQuestionnairesList()
        populateWealthGroupQuestionnairesList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerObservers()
    }



    private fun populateQuestionnairesList() {
        val gson = Gson()
        val sharedPreferences: SharedPreferences? =
            context?.applicationContext?.getSharedPreferences(
                "MyPref",
                Context.MODE_PRIVATE
            )
        val questionnairesListString =
            sharedPreferences?.getString(Constants.QUESTIONNAIRES_LIST_OBJECT, null)
        val questionnairesListObject: CountyLevelQuestionnaireListObject =
            gson.fromJson(
                questionnairesListString,
                CountyLevelQuestionnaireListObject::class.java
            )

        binding.apply {
            val countyQuestionnaireAdapter = activity?.let {
                CountyQuestionnaireAdapter(
                    questionnairesListObject.questionnaireList,
                    this@HomeFragment,
                    it
                )
            }
            val gridLayoutManager = GridLayoutManager(activity, 1)
            countyLevelRV.layoutManager = gridLayoutManager
            countyLevelRV.hasFixedSize()
            countyLevelRV.adapter =
                countyQuestionnaireAdapter
        }
    }


    private fun populateWealthGroupQuestionnairesList() {
        val gson = Gson()
        val sharedPreferences: SharedPreferences? =
            context?.applicationContext?.getSharedPreferences(
                "MyPref",
                Context.MODE_PRIVATE
            )
        val questionnairesListString =
            sharedPreferences?.getString(Constants.WEALTH_GROUP_LIST_OBJECT, null)
        val questionnairesListObject: WealthGroupQuestionnaireListObject =
            gson.fromJson(
                questionnairesListString,
                WealthGroupQuestionnaireListObject::class.java
            )

        System.out.println()

        binding.apply {
            val wealthGroupAdapter = activity?.let {
                WealthGroupQuestionnaireAdapter(
                    questionnairesListObject.questionnaireList,
                    this@HomeFragment,
                    it
                )
            }
            val gridLayoutManager = GridLayoutManager(activity, 1)
            wealthGroupRV.layoutManager = gridLayoutManager
            wealthGroupRV.hasFixedSize()
            wealthGroupRV.adapter =
                wealthGroupAdapter
        }
    }


    fun registerObservers() {
        homeViewModel.questionnaireApiResponse.observe(viewLifecycleOwner, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(activity, "Questionnaire submitted succesfully", Toast.LENGTH_SHORT).show()
                        if ((resource.data as QuestionnaireApiResponse).questionnaireType == 1) {
                            updateSubmittedCountyLevelQuestionnaire((resource.data as QuestionnaireApiResponse).questionnaireUniqueId)
                        } else if ((resource.data as QuestionnaireApiResponse).questionnaireType == 2) {
                            updateSubmittedWealthGroupQuestionnaire((resource.data as QuestionnaireApiResponse).questionnaireUniqueId)
                        }
                        populateQuestionnairesList()
                        populateWealthGroupQuestionnairesList()
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                    Status.UNAUTHORISED -> {

                    }
                    Status.UNPROCESSABLE_ENTITY -> {
                        Toast.makeText(activity, "Duplicate questionnaire", Toast.LENGTH_SHORT).show()
                        populateQuestionnairesList()
                        populateWealthGroupQuestionnairesList()
                    }
                }
            }
        })

        homeViewModel.isQuestionnaireCompleted.observe(viewLifecycleOwner, Observer {
            if (it) {
                populateQuestionnairesList()
                populateWealthGroupQuestionnairesList()
                homeViewModel.setIsQuestionnaireCompleted(false)
            }
        })
    }

    private fun updateSubmittedWealthGroupQuestionnaire(questionnaireUniqueId: String) {
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
        var wealthGroupQuestionnaires: MutableList<WealthGroupQuestionnaire> = questionnairesListObject.questionnaireList
        val questionnaireToBeUpdated = wealthGroupQuestionnaires.first {
            it.uniqueId == questionnaireUniqueId
        }
        questionnaireToBeUpdated.questionnaireStatus = QuestionnaireStatus.SUBMITTED_TO_BACKEND
        wealthGroupQuestionnaires = wealthGroupQuestionnaires.filter {
            it.uniqueId != questionnaireUniqueId
        } as MutableList<WealthGroupQuestionnaire>
        wealthGroupQuestionnaires.add(questionnaireToBeUpdated)

        questionnairesListObject.questionnaireList = wealthGroupQuestionnaires
        editor?.remove(Constants.WEALTH_GROUP_LIST_OBJECT)
        val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
        editor?.putString(
            Constants.WEALTH_GROUP_LIST_OBJECT,
            newQuestionnaireObjectString
        )
        editor?.commit()
    }


    private fun updateSubmittedCountyLevelQuestionnaire(questionnaireUniqueId: String) {
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
        var countyLevelQuestionnaires: MutableList<CountyLevelQuestionnaire> = questionnairesListObject.questionnaireList
        val questionnaireToBeUpdated = countyLevelQuestionnaires.first {
            it.uniqueId == questionnaireUniqueId
        }
        questionnaireToBeUpdated.questionnaireStatus = QuestionnaireStatus.SUBMITTED_TO_BACKEND
        countyLevelQuestionnaires = countyLevelQuestionnaires.filter {
            it.uniqueId != questionnaireUniqueId
        } as MutableList<CountyLevelQuestionnaire>
        countyLevelQuestionnaires.add(questionnaireToBeUpdated)

        questionnairesListObject.questionnaireList = countyLevelQuestionnaires
        editor?.remove(Constants.QUESTIONNAIRES_LIST_OBJECT)
        val newQuestionnaireObjectString: String = gson.toJson(questionnairesListObject)
        editor?.putString(
            Constants.QUESTIONNAIRES_LIST_OBJECT,
            newQuestionnaireObjectString
        )
        editor?.commit()
    }



    override fun onQuestionnaireItemClicked(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        homeViewModel.submitWealthGroupQuestionnaire(wealthGroupQuestionnaire)
    }

    override fun onCountyLevelQuestionnaireItemClicked(countyLevelQuestionnaire: CountyLevelQuestionnaire) {
        homeViewModel.submitCountyLevelQuestionnaire(countyLevelQuestionnaire)
    }
}