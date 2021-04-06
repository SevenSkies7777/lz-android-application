package com.silasonyango.ndma.ui.home.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.model.LivelihoodZoneModel
import com.silasonyango.ndma.ui.county.model.SubLocationZoneAssignmentModel

class SubLocationZoneAssignmentAdapter(
    private val subLocationZoneAssignmentList: MutableList<SubLocationZoneAssignmentModel>,
    val subLocationZoneAssignmentAdapterCallBack: SubLocationZoneAssignmentAdapter.SubLocationZoneAssignmentAdapterCallBack,
    val livelihoodZonesList: MutableList<LivelihoodZoneModel>,
    val context: Context
) : RecyclerView.Adapter<SubLocationZoneAssignmentAdapter.ViewHolder>(), LivelihoodZonesAdapter.LivelihoodZonesAdapterCallBack {

    private var livelihoodZoneAlertDialog: android.app.AlertDialog? = null

    lateinit var currentSubLocationZoneAssignmentModel: SubLocationZoneAssignmentModel

    interface SubLocationZoneAssignmentAdapterCallBack {
        fun onLivelihoodZoneSelected(selectedSubLocationZoneAssignment: SubLocationZoneAssignmentModel)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var subLocationName: TextView = view.findViewById<TextView>(R.id.subLocationName)
        var lzDropDown: View = view.findViewById<View>(R.id.lzDropDown)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sublocation_zone_assignment_item, viewGroup, false)

        return ViewHolder(view)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        currentSubLocationZoneAssignmentModel = subLocationZoneAssignmentList.get(position)
        viewHolder.subLocationName.text = subLocationZoneAssignmentList.get(position).subLocation.subLocationName
        viewHolder.lzDropDown.setOnClickListener {
            inflateLivelihoodZoneModal(livelihoodZonesList)
        }
    }

    override fun getItemCount() = subLocationZoneAssignmentList.size

    private fun inflateLivelihoodZoneModal(livelihoodZoneModelList: MutableList<LivelihoodZoneModel>) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.list_layout, null)

        val listRecyclerView = v.findViewById<RecyclerView>(R.id.listRv)

        val lzAdapter = LivelihoodZonesAdapter(
            livelihoodZoneModelList,
            this
        )
        val gridLayoutManager = GridLayoutManager(context, 1)
        listRecyclerView.layoutManager = gridLayoutManager
        listRecyclerView.hasFixedSize()
        listRecyclerView.adapter = lzAdapter

        openLivelihoodZoneModal(v)
    }

    private fun openLivelihoodZoneModal(v: View) {
        val width =
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        livelihoodZoneAlertDialog = builder.create()
        (livelihoodZoneAlertDialog as android.app.AlertDialog).apply {
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

    override fun onLivelihoodZoneItemClicked(selectedLivelihoodZone: LivelihoodZoneModel) {
        currentSubLocationZoneAssignmentModel.livelihoodZoneId = selectedLivelihoodZone.livelihoodZoneId
        subLocationZoneAssignmentAdapterCallBack.onLivelihoodZoneSelected(currentSubLocationZoneAssignmentModel)
        (livelihoodZoneAlertDialog as android.app.AlertDialog).dismiss()
    }
}