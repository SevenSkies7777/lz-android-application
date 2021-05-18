package com.silasonyango.ndma.ui.wealthgroup.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.silasonyango.ndma.R
import com.silasonyango.ndma.ui.model.RankResponseItem
import com.silasonyango.ndma.ui.wealthgroup.model.ConstraintCategoryEnum
import com.silasonyango.ndma.ui.wealthgroup.model.ConstraintsTypeEnum
import com.silasonyango.ndma.ui.wealthgroup.model.FgdParticipantModel

class FgdParticipantsAdapter(
    val fgdParticipants: MutableList<FgdParticipantModel>,
    val fgdParticipantsAdapterCallBack: FgdParticipantsAdapter.FgdParticipantsAdapterCallBack,
    val context: Context
) : RecyclerView.Adapter<FgdParticipantsAdapter.ViewHolder>() {

    private var genderDialog: androidx.appcompat.app.AlertDialog? = null

    private var disabilityDialog: androidx.appcompat.app.AlertDialog? = null

    private var educationLevelDialog: androidx.appcompat.app.AlertDialog? = null

    private var consentDialog: androidx.appcompat.app.AlertDialog? = null

    interface FgdParticipantsAdapterCallBack {
        fun onAParticipantUpdated(updatedParticipant: FgdParticipantModel, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvGender: TextView = view.findViewById<TextView>(R.id.gender)
        var tvDisability: TextView = view.findViewById<TextView>(R.id.disability)
        var tvLevelOfEducation: TextView = view.findViewById<TextView>(R.id.levelOfEducation)
        var tvConsentToParticipate: TextView =
            view.findViewById<TextView>(R.id.consentToParticipate)

        var etParticipantName: EditText = view.findViewById<EditText>(R.id.participantName)
        var etAge: EditText = view.findViewById<EditText>(R.id.age)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fdg_participants_item, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentParticipant = fgdParticipants.get(position)

//        if (currentParticipant.gender == 1) {
//            viewHolder.tvGender.text = "Male"
//        }
//        if (currentParticipant.gender == 2) {
//            viewHolder.tvGender.text = "Female"
//        }
//        if (currentParticipant.disability == 1) {
//            viewHolder.tvDisability.text = "Disabled"
//        }
//        if (currentParticipant.disability == 2) {
//            viewHolder.tvDisability.text = "Not Disabled"
//        }
//        if (currentParticipant.levelOfEducation == 1) {
//            viewHolder.tvLevelOfEducation.text = "Non-formal education"
//        }
//        if (currentParticipant.levelOfEducation == 2) {
//            viewHolder.tvLevelOfEducation.text = "Primary"
//        }
//        if (currentParticipant.levelOfEducation == 3) {
//            viewHolder.tvLevelOfEducation.text = "Secondary"
//        }
//        if (currentParticipant.levelOfEducation == 4) {
//            viewHolder.tvLevelOfEducation.text = "Post-Secondary"
//        }
//        if (currentParticipant.consentToParticipate == 1) {
//            viewHolder.tvConsentToParticipate.text = "Consented"
//        }
//        if (currentParticipant.consentToParticipate == 2) {
//            viewHolder.tvConsentToParticipate.text = "No consent"
//        }

//        viewHolder.etParticipantName.setText(currentParticipant.participantName)
//        viewHolder.etAge.setText(currentParticipant.age.toString())

        viewHolder.tvGender.setOnClickListener {
            inflateGenderModal(currentParticipant, position,viewHolder)
        }
        viewHolder.tvDisability.setOnClickListener {
            inflateDisabilityModal(currentParticipant, position,viewHolder)
        }
        viewHolder.tvLevelOfEducation.setOnClickListener {
            inflateEducationLevelModal(currentParticipant, position,viewHolder)
        }
        viewHolder.tvConsentToParticipate.setOnClickListener {
            inflateConsentModal(currentParticipant, position,viewHolder)
        }

        viewHolder.etParticipantName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    currentParticipant.participantName = editable.toString()
                    fgdParticipantsAdapterCallBack.onAParticipantUpdated(currentParticipant, position)

                }, 1500)
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })


        viewHolder.etAge.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    currentParticipant.age = editable.toString().toDouble()
                    fgdParticipantsAdapterCallBack.onAParticipantUpdated(currentParticipant, position)

                }, 1500)
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })

    }

    override fun getItemCount() = fgdParticipants.size

    private fun inflateGenderModal(updatedParticipant: FgdParticipantModel, position: Int, viewHolder: ViewHolder) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.gender_layout, null)
        val maleGender = v.findViewById<TextView>(R.id.maleOption)
        val femaleGender = v.findViewById<TextView>(R.id.femaleOption)

        maleGender.setOnClickListener {
            updatedParticipant.gender = 1
            viewHolder.tvGender.text = "Male"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (genderDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        femaleGender.setOnClickListener {
            updatedParticipant.gender = 2
            viewHolder.tvGender.text = "Female"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (genderDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }

        openGenderModal(v)
    }

    private fun openGenderModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        genderDialog = builder.create()
        (genderDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (genderDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (genderDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (genderDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (genderDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    private fun inflateDisabilityModal(updatedParticipant: FgdParticipantModel, position: Int, viewHolder: ViewHolder) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.disability_layout, null)
        val disabilityOption = v.findViewById<TextView>(R.id.disabilityOption)
        val noDisabilityOption = v.findViewById<TextView>(R.id.noDisabilityOption)

        disabilityOption.setOnClickListener {
            updatedParticipant.disability = 1
            viewHolder.tvDisability.text = "Disabled"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (disabilityDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        noDisabilityOption.setOnClickListener {
            updatedParticipant.disability = 2
            viewHolder.tvDisability.text = "Not Disabled"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (disabilityDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }

        openDisabilityModal(v)
    }

    private fun openDisabilityModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        disabilityDialog = builder.create()
        (disabilityDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (disabilityDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (disabilityDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (disabilityDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (disabilityDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    private fun inflateEducationLevelModal(updatedParticipant: FgdParticipantModel, position: Int, viewHolder: ViewHolder) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.education_level_layout, null)
        val nonFormalEducation = v.findViewById<TextView>(R.id.nonFormalEducation)
        val primary = v.findViewById<TextView>(R.id.primary)
        val secondary = v.findViewById<TextView>(R.id.secondary)
        val postSecondary = v.findViewById<TextView>(R.id.postSecondary)

        nonFormalEducation.setOnClickListener {
            updatedParticipant.levelOfEducation = 1
            viewHolder.tvLevelOfEducation.text = "Non-formal education"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (educationLevelDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        primary.setOnClickListener {
            updatedParticipant.levelOfEducation = 2
            viewHolder.tvLevelOfEducation.text = "Primary"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (educationLevelDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        secondary.setOnClickListener {
            updatedParticipant.levelOfEducation = 3
            viewHolder.tvLevelOfEducation.text = "Secondary"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (educationLevelDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        postSecondary.setOnClickListener {
            updatedParticipant.levelOfEducation = 4
            viewHolder.tvLevelOfEducation.text = "Post-Secondary"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (educationLevelDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }

        openEducationLevelModal(v)
    }

    private fun openEducationLevelModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        educationLevelDialog = builder.create()
        (educationLevelDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (educationLevelDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (educationLevelDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (educationLevelDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (educationLevelDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    private fun inflateConsentModal(updatedParticipant: FgdParticipantModel, position: Int, viewHolder: ViewHolder) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.consent_layout, null)
        val consentOption = v.findViewById<TextView>(R.id.consentOption)
        val noConsentOption = v.findViewById<TextView>(R.id.noConsentOption)

        consentOption.setOnClickListener {
            updatedParticipant.consentToParticipate = 1
            viewHolder.tvConsentToParticipate.text = "Consented"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (consentDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        noConsentOption.setOnClickListener {
            updatedParticipant.consentToParticipate = 2
            viewHolder.tvConsentToParticipate.text = "No consent"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (consentDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }

        openConsentModal(v)
    }

    private fun openConsentModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        consentDialog = builder.create()
        (consentDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (consentDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (consentDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (consentDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (consentDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}