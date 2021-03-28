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
import com.google.gson.Gson
import com.silasonyango.ndma.R
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.database.questionnaires.entity.QuestionnaireTypesEntity

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        populateQuestionnairesList()


        return root
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
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        val questionnairesListString =
            sharedPreferences?.getString(Constants.QUESTIONNAIRES_LIST_OBJECT, null)
        val questionnairesListObject: CountyLevelQuestionnaireListObject =
            gson.fromJson(
                questionnairesListString,
                CountyLevelQuestionnaireListObject::class.java
            )
    }
}