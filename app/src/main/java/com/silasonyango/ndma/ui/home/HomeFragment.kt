package com.silasonyango.ndma.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.silasonyango.ndma.R
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
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        homeViewModel.addQuestionnaireType(QuestionnaireTypesEntity(
            0,
            "By Wealth Group",
            1
        ))
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerObservers()
    }

    private fun registerObservers() {
        homeViewModel.allQuestionnaireTypesLiveData.observe(viewLifecycleOwner) {
            System.out.println()
        }
    }
}