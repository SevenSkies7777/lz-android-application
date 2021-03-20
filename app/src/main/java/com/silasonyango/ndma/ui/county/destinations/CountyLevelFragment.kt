package com.silasonyango.ndma.ui.county.destinations

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.silasonyango.ndma.R
import com.silasonyango.ndma.databinding.CountyLevelQuestionnaireLayoutBinding
import com.silasonyango.ndma.ui.county.adapters.LzCropProductionRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.adapters.LzMarketTradeRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.adapters.SubCountiesSpinnerAdapter
import com.silasonyango.ndma.ui.county.adapters.SubLocationLzAssignmentRecyclerViewAdapter
import com.silasonyango.ndma.ui.county.model.CropModel
import com.silasonyango.ndma.ui.county.model.MarketModel
import com.silasonyango.ndma.ui.county.model.SubCountyModel
import com.silasonyango.ndma.ui.county.model.SubLocationModel
import com.silasonyango.ndma.ui.county.viewmodel.CountyLevelViewModel

class CountyLevelFragment : DialogFragment(), SubLocationLzAssignmentRecyclerViewAdapter.SubLocationLzAssignmentRecyclerViewAdapterCallback, LzCropProductionRecyclerViewAdapter.LzCropProductionRecyclerViewAdapterCallBack, LzMarketTradeRecyclerViewAdapter.LzMarketTradeRecyclerViewAdapterCallBack {

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
        defineLzMarketsLayouts()
        defineNavigation()
        binding.apply {


            val sublocationList: MutableList<SubLocationModel> = ArrayList()
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            sublocationList.add(SubLocationModel(1, "Samburu", "008"))
            populateLocationAndPopulationRV(sublocationList)


            val cropModelList: MutableList<CropModel> = ArrayList()
            cropModelList.add(CropModel("Maize", 1))
            cropModelList.add(CropModel("Beans", 2))
            cropModelList.add(CropModel("Potatoes", 3))
            cropModelList.add(CropModel("Tomatoes", 4))
            cropModelList.add(CropModel("Cassava", 5))
            cropModelList.add(CropModel("Rice", 6))
            cropModelList.add(CropModel("Mango", 7))
            populateCropProductionRecyclerView(cropModelList)
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

    private fun populateCropProductionRecyclerView(cropModelList: MutableList<CropModel>) {
        binding.apply {
            cropProductionLayout.apply {
                val lzCropProductionRecyclerViewAdapter =
                        activity?.let {
                            LzCropProductionRecyclerViewAdapter(
                                    it,
                                    cropModelList,
                                    this@CountyLevelFragment
                            )
                        }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                lzCropProductionRV.layoutManager = gridLayoutManager
                lzCropProductionRV.hasFixedSize()
                lzCropProductionRV.adapter =
                        lzCropProductionRecyclerViewAdapter
            }
        }
    }

    private fun defineLzMarketsLayouts() {
        binding.apply {
            lzMarkets.apply {
                val subCounties: MutableList<SubCountyModel> = ArrayList()
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))
                subCounties.add(SubCountyModel("Laikipia", 0))

                val spinnerAdapter = context?.let {
                    SubCountiesSpinnerAdapter(it, R.layout.spinner_item_layout, R.id.tvSpinnerItemText, subCounties)
                }


                subCountySpinner.adapter = spinnerAdapter
                subCountySpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                            ) {
                                val selectedSubCounty = parent.getItemAtPosition(position)

                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {

                            }
                        }

                addEditText.setOnClickListener {
                    val etMarketName = EditText(context)
                    etMarketName.hint = "Market Name"
                    villageEditTextContainer.addView(etMarketName)
                }

                val marketList: MutableList<MarketModel> = ArrayList()

                marketBackButton.setOnClickListener {
                    for (i in 0..villageEditTextContainer.size) {
                        var text = (villageEditTextContainer.get(i) as EditText).text.toString()

                    }
                }

            }


        }
    }

    private fun populateLzMarketTradeRecyclerViewAdapter(marketList: MutableList<MarketModel>) {
        binding.apply {
            lzMarketTransactions.apply {
                val lzMarketTradeRecyclerViewAdapter =
                        activity?.let {
                            LzMarketTradeRecyclerViewAdapter(
                                    it,
                                    marketList,
                                    this@CountyLevelFragment
                            )
                        }
                val gridLayoutManager = GridLayoutManager(activity, 1)
                lzMarketTradeRV.layoutManager = gridLayoutManager
                lzMarketTradeRV.hasFixedSize()
                lzMarketTradeRV.adapter =
                        lzMarketTradeRecyclerViewAdapter
            }
        }
    }

    private fun defineNavigation() {
        binding.apply {

            lzDefinitionsLayout.apply {
                lzDefinitionsNextButton.setOnClickListener {
                    lzDefinitionsLayout.root.visibility = View.GONE
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                }
            }

            /*Location and population navigation buttons*/
            locationAndPopulationLayout.apply {
                locationNextButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.GONE
                    cropProductionLayout.root.visibility = View.VISIBLE
                }
                locationBackButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.GONE
                    lzDefinitionsLayout.root.visibility = View.VISIBLE
                }
            }

            /*Crop Production navigation buttons*/
            cropProductionLayout.apply {
                cropProductionNextButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.VISIBLE
                    cropProductionLayout.root.visibility = View.GONE
                }
                cropProductionBackButton.setOnClickListener {
                    locationAndPopulationLayout.root.visibility = View.VISIBLE
                    cropProductionLayout.root.visibility = View.GONE
                }
            }

            /*Water source navigation buttons*/
            mainWaterSource.apply {
                waterSourceBackButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.GONE
                    cropProductionLayout.root.visibility = View.VISIBLE
                }
                waterSourceNextButton.setOnClickListener {
                    mainWaterSource.root.visibility = View.GONE
                    lzMarkets.root.visibility = View.VISIBLE
                }
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