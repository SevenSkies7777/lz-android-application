package com.silasonyango.ndma.ui.county

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.silasonyango.ndma.R

class CountyLevelFragment : DialogFragment() {

    private lateinit var countyLevelViewModel: CountyLevelViewModel

    companion object {
        private const val SELECTED_CARD = "selectedCard"

        @JvmStatic
        fun newInstance() =
                CountyLevelFragment()
                        .apply {
                            arguments = Bundle().apply {

                            }
                        }

        const val TAG = "Card Details"
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        countyLevelViewModel =
                ViewModelProvider(this).get(CountyLevelViewModel::class.java)
        val root = inflater.inflate(R.layout.county_level_questionnaire_layout, container, false)
        return root
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(matchParent, matchParent)
            window?.setBackgroundDrawable(null)
        }
    }
}