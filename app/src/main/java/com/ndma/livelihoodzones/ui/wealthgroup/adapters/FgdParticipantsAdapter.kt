package com.ndma.livelihoodzones.ui.wealthgroup.adapters

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
import androidx.recyclerview.widget.RecyclerView
import com.ndma.livelihoodzones.R
import com.ndma.livelihoodzones.ui.wealthgroup.model.FgdParticipantModel

class FgdParticipantsAdapter(
    val fgdParticipants: MutableList<FgdParticipantModel>,
    val fgdParticipantsAdapterCallBack: FgdParticipantsAdapter.FgdParticipantsAdapterCallBack,
    val context: Context,
    val isAResume: Boolean
) : RecyclerView.Adapter<FgdParticipantsAdapter.ViewHolder>() {

    private var genderDialog: androidx.appcompat.app.AlertDialog? = null

    private var disabilityDialog: androidx.appcompat.app.AlertDialog? = null

    private var educationLevelDialog: androidx.appcompat.app.AlertDialog? = null

    private var consentDialog: androidx.appcompat.app.AlertDialog? = null

    private var ageBandDialog: androidx.appcompat.app.AlertDialog? = null

    interface FgdParticipantsAdapterCallBack {
        fun onAParticipantUpdated(updatedParticipant: FgdParticipantModel, position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvGender: TextView = view.findViewById<TextView>(R.id.gender)
        var tvDisability: TextView = view.findViewById<TextView>(R.id.disability)
        var tvLevelOfEducation: TextView = view.findViewById<TextView>(R.id.levelOfEducation)
        var tvConsentToParticipate: TextView =
            view.findViewById<TextView>(R.id.consentToParticipate)
        var tvSelectAgeBand: TextView =
            view.findViewById<TextView>(R.id.tvSelectAgeBand)

        var etParticipantName: EditText = view.findViewById<EditText>(R.id.participantName)

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

        if (isAResume) {
            viewHolder.etParticipantName.setText(currentParticipant.participantName)
            viewHolder.tvGender.text = if (currentParticipant.gender == 1) "Male" else "Female"
            viewHolder.tvDisability.text =
                if (currentParticipant.disability == 1) "Disabled" else "Not disabled"
            viewHolder.tvLevelOfEducation.text = returnEducationLevelString(currentParticipant)
            viewHolder.tvConsentToParticipate.text = if (currentParticipant.consentToParticipate == 1) "Consented" else  "Not consented"
            viewHolder.tvSelectAgeBand.text = returnAgeBandString(currentParticipant.age)

            if (currentParticipant.gender == 0) {
                viewHolder.tvGender.text = "Select gender..."
            }
            if (currentParticipant.disability == 0) {
                viewHolder.tvDisability.text = "Has any disability..."
            }
            if (currentParticipant.disability == 0) {
                viewHolder.tvDisability.text = "Disability?..."
            }
            if (currentParticipant.levelOfEducation == 0) {
                viewHolder.tvLevelOfEducation.text = "Level of education..."
            }
            if (currentParticipant.consentToParticipate == 0) {
                viewHolder.tvConsentToParticipate.text = "Consent to participate..."
            }
            if (currentParticipant.age == 0.0) {
                viewHolder.tvSelectAgeBand.text = "Select age band..."
            }
        }

        viewHolder.tvGender.setOnClickListener {
            inflateGenderModal(currentParticipant, position, viewHolder)
        }
        viewHolder.tvDisability.setOnClickListener {
            inflateDisabilityModal(currentParticipant, position, viewHolder)
        }
        viewHolder.tvLevelOfEducation.setOnClickListener {
            inflateEducationLevelModal(currentParticipant, position, viewHolder)
        }
        viewHolder.tvConsentToParticipate.setOnClickListener {
            inflateConsentModal(currentParticipant, position, viewHolder)
        }

        viewHolder.tvSelectAgeBand.setOnClickListener {
            inflateAgeBandModal(currentParticipant, position, viewHolder)
        }

        viewHolder.etParticipantName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    currentParticipant.participantName = editable.toString()
                    fgdParticipantsAdapterCallBack.onAParticipantUpdated(
                        currentParticipant,
                        position
                    )

                }, 1000)
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

    private fun inflateGenderModal(
        updatedParticipant: FgdParticipantModel,
        position: Int,
        viewHolder: ViewHolder
    ) {
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


    private fun inflateDisabilityModal(
        updatedParticipant: FgdParticipantModel,
        position: Int,
        viewHolder: ViewHolder
    ) {
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


    private fun inflateEducationLevelModal(
        updatedParticipant: FgdParticipantModel,
        position: Int,
        viewHolder: ViewHolder
    ) {
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


    private fun inflateConsentModal(
        updatedParticipant: FgdParticipantModel,
        position: Int,
        viewHolder: ViewHolder
    ) {
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

    fun returnEducationLevelString(currentParticipant: FgdParticipantModel): String {
        if (currentParticipant.levelOfEducation == 1) {
            return "Non-formal education"
        }
        if (currentParticipant.levelOfEducation == 2) {
            return  "Primary"
        }
        if (currentParticipant.levelOfEducation == 3) {
            return  "Secondary"
        }
        if (currentParticipant.levelOfEducation == 4) {
            return  "Post-Secondary"
        }

        return ""
    }

    fun returnAgeBandString(ageBandCode: Double): String {
        if (ageBandCode == 1.0) {
            return "18-24 years"
        }
        if (ageBandCode == 2.0) {
            return "25-35 years"
        }
        if (ageBandCode == 3.0) {
            return "36-49 years"
        }
        if (ageBandCode == 4.0) {
            return "50-59 years"
        }
        if (ageBandCode == 5.0) {
            return "60 and above years"
        }
        return ""
    }

    private fun inflateAgeBandModal(
        updatedParticipant: FgdParticipantModel,
        position: Int,
        viewHolder: ViewHolder
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        val v = (inflater as LayoutInflater).inflate(R.layout.age_band_layout, null)
        val eighteenToTwentyFour = v.findViewById<TextView>(R.id.eighteenToTwentyFour)
        val twentyFiveToThirtyFive = v.findViewById<TextView>(R.id.twentyFiveToThirtyFive)
        val thirtySixToFourtyNine = v.findViewById<TextView>(R.id.thirtySixToFourtyNine)
        val fiftyToFiftyNine = v.findViewById<TextView>(R.id.fiftyToFiftyNine)
        val aboveSixtyYears = v.findViewById<TextView>(R.id.aboveSixtyYears)

        eighteenToTwentyFour.setOnClickListener {
            updatedParticipant.age = 1.0
            viewHolder.tvSelectAgeBand.text = "18-24 years"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (ageBandDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        twentyFiveToThirtyFive.setOnClickListener {
            updatedParticipant.age = 2.0
            viewHolder.tvSelectAgeBand.text = "25-35 years"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (ageBandDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        thirtySixToFourtyNine.setOnClickListener {
            updatedParticipant.age = 3.0
            viewHolder.tvSelectAgeBand.text = "36-49 years"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (ageBandDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        fiftyToFiftyNine.setOnClickListener {
            updatedParticipant.age = 4.0
            viewHolder.tvSelectAgeBand.text = "50-59 years"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (ageBandDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }
        aboveSixtyYears.setOnClickListener {
            updatedParticipant.age = 5.0
            viewHolder.tvSelectAgeBand.text = "Above 60 years"
            fgdParticipantsAdapterCallBack.onAParticipantUpdated(updatedParticipant, position)
            (consentDialog as androidx.appcompat.app.AlertDialog).dismiss()
        }

        openAgeBandModal(v)
    }

    private fun openAgeBandModal(v: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setView(v)
        builder.setCancelable(true)
        ageBandDialog = builder.create()
        (ageBandDialog as androidx.appcompat.app.AlertDialog).setCancelable(true)
        (ageBandDialog as androidx.appcompat.app.AlertDialog).setCanceledOnTouchOutside(true)
        (ageBandDialog as androidx.appcompat.app.AlertDialog).window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        (ageBandDialog as androidx.appcompat.app.AlertDialog).show()
        val window = (ageBandDialog as androidx.appcompat.app.AlertDialog).window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}