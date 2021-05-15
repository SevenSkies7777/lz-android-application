package com.silasonyango.ndma.ui.county.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.county.responses.ZoneCharectaristicsResponseItem

class ZoneCharectaristicsAdapter(
    val zoneCharectaristicsResponseItemList: MutableList<ZoneCharectaristicsResponseItem>,
    val zoneCharectaristicsAdapterCallBack: ZoneCharectaristicsAdapter.ZoneCharectaristicsAdapterCallBack,
    val context: Context
) : RecyclerView.Adapter<ZoneCharectaristicsAdapter.ViewHolder>() {

    private var charectaristicsDialog: android.app.AlertDialog? = null

    val fontAwesome: Typeface =
        Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf")

    interface ZoneCharectaristicsAdapterCallBack {

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
    }

    override fun getItemCount() = zoneCharectaristicsResponseItemList.size

    private fun inflateZoneCharectaristicsModal() {wq   q13
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.error_message_layout, null)
        val close = v.findViewById<TextView>(R.id.close)
        close.setOnClickListener {
            (charectaristicsDialog as android.app.AlertDialog).cancel()
        }

        openZoneCharectaristicsModal(v)
    }

    private fun openZoneCharectaristicsModal(v: View) {
        val width =
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        val height =
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        charectaristicsDialog = builder.create()
        (charectaristicsDialog as android.app.AlertDialog).apply {
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