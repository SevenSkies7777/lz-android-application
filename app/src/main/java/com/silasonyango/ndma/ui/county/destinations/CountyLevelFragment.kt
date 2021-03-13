package com.silasonyango.ndma.ui.county.destinations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.ui.county.adapters.SubLocationLzAssignmentRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.model.SubLocationModel
import com.silasonyango.ndma.ui.county.viewmodel.CountyLevelViewModel

class CountyLevelFragment : DialogFragment(), SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback {

    private lateinit var countyLevelViewModel: CountyLevelViewModel

    private lateinit var binding: CountyLevelQuestionnaireLayoutBinding

    companion object {

        @JvmStatic
        fun newInstance() =
                CountyLevelFragment()
                        .apply {
                            arguments = Bundle().apply {

                            }
                        }
    }

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

    private fun defineViews() {
        binding.apply {

            lzDefinitionsLayout.apply {
                lzDefinitionsNextButton.setOnClickListener {
                    lzDefinitionsLayout.root.visibility = View.GONE
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                }
            }


            val sublocationList: MutableList<SubLocationModel> = ArrayList()
            sublocationList.add(SubLocationModel(1,"Samburu","008"))
            sublocationList.add(SubLocationModel(1,"Samburu","008"))
            sublocationList.add(SubLocationModel(1,"Samburu","008"))
            sublocationList.add(SubLocationModel(1,"Samburu","008"))
            sublocationList.add(SubLocationModel(1,"Samburu","008"))
            populateLocationAndPopulationRV(sublocationList)

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

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(matchParent, matchParent)
            window?.setBackgroundDrawable(null)
        }
    }
}