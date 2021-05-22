package com.ndma.livelihoodzones.ui.county.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.county.responses.ZoneCharectaristicsResponseItem

class ZoneCharectaristicsAdapter(
    val zoneCharectaristicsResponseItemList: MutableList<ZoneCharectaristicsResponseItem>,
    val zoneCharectaristicsAdapterCallBack: ZoneCharectaristicsAdapter.ZoneCharectaristicsAdapterCallBack,
    val context: Context
) : RecyclerView.Adapter<ZoneCharectaristicsAdapter.ViewHolder>() {

    private var rankDialog: AlertDialog? = null

    private var errorDialog: android.app.AlertDialog? = null

    val fontAwesome: Typeface =
        Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf")

    interface ZoneCharectaristicsAdapterCallBack {
        fun onAZoneCharectaristicsSubmitted(currentResponseItem: ZoneCharectaristicsResponseItem, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var zoneName: TextView = view.findViewById<TextView>(R.id.zoneName)
        var detailsIcon: TextView = view.findViewById<TextView>(R.id.detailsIcon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.county_lz_charectaristics_item, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentZoneCharectaristicsResponseItem = zoneCharectaristicsResponseItemList.get(position)
        viewHolder.zoneName.text = currentZoneCharectaristicsResponseItem.zone.livelihoodZoneName
        viewHolder.detailsIcon.setTypeface(fontAwesome)
        viewHolder.detailsIcon.setOnClickListener {
            inflateZoneCharectaristicsModal(currentZoneCharectaristicsResponseItem,position,viewHolder)
        }
    }

    override fun getItemCount() = zoneCharectaristicsResponseItemList.size

    private fun inflateZoneCharectaristicsModal(currentResponseItem: ZoneCharectaristicsResponseItem, position: Int, viewHolder: ViewHolder) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.zone_charectaristics_page, null)
        val close = v.findViewById<TextView>(R.id.close)
//        close.setOnClickListener {
//            (charectaristicsDialog as android.app.AlertDialog).cancel()
//        }
        val noCharectaristics = v.findViewById<TextView>(R.id.noCharectaristics)
        val xticsNumberSubmitButton = v.findViewById<TextView>(R.id.xticsNumberSubmitButton)
        val charectaristicsList = v.findViewById<LinearLayout>(R.id.charectaristicsList)
        val numberCharectaristics = v.findViewById<LinearLayout>(R.id.numberCharectaristics)
        val charectaristicsListWrapper = v.findViewById<LinearLayout>(R.id.charectaristicsListWrapper)
        val submitButton = v.findViewById<TextView>(R.id.submitButton)

        xticsNumberSubmitButton.setOnClickListener {
            if (noCharectaristics.text.toString().isNotEmpty()) {

                val editTextsList: MutableList<EditText> = ArrayList()
                for (i in 0..noCharectaristics.text.toString().toInt() - 1) {
                    editTextsList.add(EditText(context))
                }

                var ids = 1
                for (currentEditText in editTextsList) {
                    currentEditText.setId(ids)
                    currentEditText.hint = "$ids)."
                    currentEditText.setLayoutParams(
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                    charectaristicsList.addView(currentEditText)
                    ids++
                }

                numberCharectaristics.visibility = View.GONE
                charectaristicsListWrapper.visibility = View.VISIBLE

            }
        }

        submitButton.setOnClickListener {

            var allEditTextsAreEmpty = true
            for (currentEditText in charectaristicsList.children) {
                val currentString = (currentEditText as EditText).text.toString()
                if (currentString.trim().isNotEmpty()) {
                    allEditTextsAreEmpty = false
                    currentResponseItem.zoneCharectaristics.add(currentString)
                }
            }

            if (allEditTextsAreEmpty) {
                inflateErrorModal("Data error", "You have not filled in any charectaristic")
            } else {
                zoneCharectaristicsAdapterCallBack.onAZoneCharectaristicsSubmitted(currentResponseItem,position)
                viewHolder.detailsIcon.text = context.resources.getString(R.string.ic_check)
                (rankDialog as AlertDialog).dismiss()
            }

        }

        openZoneCharectaristicsModal(v)
    }

    private fun openZoneCharectaristicsModal(v: View) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        rankDialog = builder.create()
        (rankDialog as AlertDialog).setCancelable(true)
        (rankDialog as AlertDialog).setCanceledOnTouchOutside(true)
        (rankDialog as AlertDialog).window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        (rankDialog as AlertDialog).show()
        val window = (rankDialog as AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    private fun inflateErrorModal(errorTitle: String, errorMessage: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.error_message_layout, null)
        val title = v.findViewById<TextView>(R.id.title)
        val message = v.findViewById<TextView>(R.id.message)
        val close = v.findViewById<TextView>(R.id.close)
        title.text = errorTitle
        message.text = errorMessage
        close.setOnClickListener {
            (errorDialog as android.app.AlertDialog).cancel()
        }

        openErrorModal(v)
    }

    private fun openErrorModal(v: View) {
        val width =
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        errorDialog = builder.create()
        (errorDialog as android.app.AlertDialog).apply {
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
}