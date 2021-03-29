package com.silasonyango.ndma.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaire
import com.silasonyango.ndma.appStore.model.WealthGroupQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.databinding.FragmentHomeBinding
import com.silasonyango.ndma.ui.county.adapters.LzCropProductionRecyclerViewAdapter
import com.silasonyango.ndma.ui.home.adapters.CountyQuestionnaireAdapter
import com.silasonyango.ndma.ui.home.adapters.WealthGroupQuestionnaireAdapter

class HomeFragment : Fragment(), CountyQuestionnaireAdapter.CountyQuestionnaireAdapterCallBack, WealthGroupQuestionnaireAdapter.WealthGroupQuestionnaireAdapterCallBack {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var binding: FragmentHomeBinding


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //registerObservers()
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
            val countyQuestionnaireAdapter = CountyQuestionnaireAdapter(
                questionnairesListObject.questionnaireList,
                this@HomeFragment
            )
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
            val wealthGroupAdapter = WealthGroupQuestionnaireAdapter(
                questionnairesListObject.questionnaireList,
                this@HomeFragment
            )
            val gridLayoutManager = GridLayoutManager(activity, 1)
            wealthGroupRV.layoutManager = gridLayoutManager
            wealthGroupRV.hasFixedSize()
            wealthGroupRV.adapter =
                wealthGroupAdapter
        }
    }

    override fun onQuestionnaireItemClicked(wealthGroupQuestionnaire: WealthGroupQuestionnaire) {
        homeViewModel.submitWealthGroupQuestionnaire(wealthGroupQuestionnaire)
    }
}