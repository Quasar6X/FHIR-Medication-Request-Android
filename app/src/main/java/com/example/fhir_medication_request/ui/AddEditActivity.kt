package com.example.fhir_medication_request.ui

import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fhir_medication_request.R
import com.example.fhir_medication_request.common.GUEST
import com.example.fhir_medication_request.common.MEDICATION_REQUEST
import com.example.fhir_medication_request.common.SNACK
import com.example.fhir_medication_request.common.goToMain
import com.example.fhir_medication_request.database.model.*
import com.example.fhir_medication_request.databinding.*
import com.example.fhir_medication_request.viewmodel.AddEditViewModel
import com.example.fhir_medication_request.viewmodel.MedicationRequestViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private var medicationRequest: MedicationRequest? = null

    private var authoredOn: Timestamp? = null

    private val identifierRows = ArrayList<User>()
    private val categoryRows = ArrayList<Category>()
    private val noteRows = ArrayList<Note>()
    private val dosageInstructionRows = ArrayList<Dosage>()

    private val medReqViewModel: MedicationRequestViewModel by viewModels()
    private val addEditViewModel: AddEditViewModel by viewModels()
    private val auth by lazy { Firebase.auth }

    private val identifierFormAdapter: FormAdapter<User, UserFormBinding> =
        FormAdapter(identifierRows)
    private val categoryFormAdapter: FormAdapter<Category, CategoryFormBinding> =
        FormAdapter(categoryRows)
    private val noteFormAdapter: FormAdapter<Note, NoteFormBinding> =
        FormAdapter(noteRows)
    private val dosageInstructionFormAdapter: FormAdapter<Dosage, DosageFormBinding> =
        FormAdapter(dosageInstructionRows)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) finish()

        val guest = intent.getBooleanExtra(GUEST, true)
        identifierFormAdapter.isGuest = guest
        categoryFormAdapter.isGuest = guest
        noteFormAdapter.isGuest = guest
        dosageInstructionFormAdapter.isGuest = guest
        binding.identifierAddBtn.isEnabled = !guest
        binding.statusSpinner.isEnabled = !guest
        binding.statusReasonEditText.isEnabled = !guest
        binding.intentSpinner.isEnabled = !guest
        binding.categoryAddBtn.isEnabled = !guest
        binding.prioritySpinner.isEnabled = !guest
        binding.performRadioYes.isEnabled = !guest
        binding.performRadioNo.isEnabled = !guest
        binding.performRadioNone.isEnabled = !guest
        binding.reportedRadioYes.isEnabled = !guest
        binding.reportedRadioNo.isEnabled = !guest
        binding.reportedRadioNone.isEnabled = !guest
        binding.medicationEditText.isEnabled = !guest
        binding.subjectEditText.isEnabled = !guest
        binding.authoredOnEditText.isEnabled = !guest
        binding.authoredOnClearBtn.isEnabled = !guest
        binding.noteAddBtn.isEnabled = !guest
        binding.dosageInstructionsAddBtn.isEnabled = !guest
        binding.substitutionAllowedRadioYes.isEnabled = !guest
        binding.substitutionAllowedRadioNo.isEnabled = !guest
        binding.substitutionReasonText.isEnabled = !guest

        intent.getStringExtra(MEDICATION_REQUEST)?.let {
            val medReq: MedicationRequest? = Json.decodeFromString(it)
            if (medReq != null)
                populateForm(medReq)
            medicationRequest = medReq
        }

        with(binding.identifiersParent) {
            layoutManager =
                LinearLayoutManager(this@AddEditActivity, LinearLayoutManager.VERTICAL, false)
            adapter = identifierFormAdapter
        }

        with(binding.categoriesParent) {
            layoutManager =
                LinearLayoutManager(this@AddEditActivity, LinearLayoutManager.VERTICAL, false)
            adapter = categoryFormAdapter
        }

        with(binding.notesParent) {
            layoutManager =
                LinearLayoutManager(this@AddEditActivity, LinearLayoutManager.VERTICAL, false)
            adapter = noteFormAdapter
        }

        with(binding.dosageInstructionsParent) {
            layoutManager =
                LinearLayoutManager(this@AddEditActivity, LinearLayoutManager.VERTICAL, false)
            adapter = dosageInstructionFormAdapter
        }

        with(binding.statusSpinner) {
            setText(resources.getStringArray(R.array.statuses)[0])
            setAdapter(
                ArrayAdapter.createFromResource(
                    this@AddEditActivity,
                    R.array.statuses,
                    R.layout.support_simple_spinner_dropdown_item
                )
            )
        }

        with(binding.intentSpinner) {
            setText(resources.getStringArray(R.array.intents)[0])
            setAdapter(
                ArrayAdapter.createFromResource(
                    this@AddEditActivity,
                    R.array.intents,
                    R.layout.support_simple_spinner_dropdown_item
                )
            )
        }

        binding.prioritySpinner.setAdapter(
            ArrayAdapter.createFromResource(
                this,
                R.array.priorities,
                R.layout.support_simple_spinner_dropdown_item
            )
        )

        binding.authoredOnEditText.setOnClickListener { chooseDateTime() }

        addEditViewModel.medication = if (binding.medicationEditText.text != null) binding.medicationEditText.text.toString() else ""
        addEditViewModel.subject = if (binding.subjectEditText.text != null) binding.subjectEditText.text.toString() else ""

        binding.medicationEditText.addTextChangedListener { addEditViewModel.medication = it.toString() }
        binding.subjectEditText.addTextChangedListener { addEditViewModel.subject = it.toString() }

        addEditViewModel.medicationError.observe(this) { (binding.medicationEditText.parent.parent as TextInputLayout).error = it }
        addEditViewModel.subjectError.observe(this) { (binding.subjectEditText.parent.parent as TextInputLayout).error = it }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        this.menuInflater.inflate(R.menu.add_edit_menu, menu)
        val guest = intent.getBooleanExtra(GUEST, true)
        menu.getItem(1).isEnabled = !guest
        if (!guest) {
            lifecycleScope.launch {
                addEditViewModel.isSubmitEnabled.collect { value ->
                    menu.getItem(1).isEnabled = value
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.menu_save_request -> {
                val newMedReq = medicationRequest ?: MedicationRequest()
                val resp = updateMedicationRequest(newMedReq)
                startActivity(goToMain(this, auth.uid!!, auth.currentUser?.isAnonymous!!).putExtra(SNACK , resp))
                finish()
                true
            }
            R.id.menu_discard_request -> {
                startActivity(goToMain(this, auth.uid!!, auth.currentUser?.isAnonymous!!))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    fun addIdentifier(@Suppress("UNUSED_PARAMETER") source: View?) {
        identifierRows.add(User())
        identifierFormAdapter.notifyItemInserted(identifierRows.size - 1)
    }

    fun addCategory(@Suppress("UNUSED_PARAMETER") source: View?) {
        categoryRows.add(Category())
        categoryFormAdapter.notifyItemInserted(categoryRows.size - 1)
    }

    fun addNote(@Suppress("UNUSED_PARAMETER") source: View?) {
        noteRows.add(Note())
        noteFormAdapter.notifyItemInserted(noteRows.size - 1)
    }

    fun addDosage(@Suppress("UNUSED_PARAMETER") source: View?) {
        dosageInstructionRows.add(Dosage())
        dosageInstructionFormAdapter.notifyItemInserted(dosageInstructionRows.size - 1)
    }

    fun clearAuthoredOn(@Suppress("UNUSED_PARAMETER") source: View?) {
        authoredOn = null
        binding.authoredOnEditText.text = null
    }

    internal fun deleteIdentifierFormRow(user: User) {
        val index = identifierRows.indexOf(user)
        identifierRows.removeAt(index)
        identifierFormAdapter.notifyItemRemoved(index)
    }

    internal fun deleteCategoryFormRow(category: Category) {
        val index = categoryRows.indexOf(category)
        categoryRows.removeAt(index)
        categoryFormAdapter.notifyItemRemoved(index)
    }

    internal fun deleteNoteFormRow(note: Note) {
        val index = noteRows.indexOf(note)
        noteRows.removeAt(index)
        noteFormAdapter.notifyItemRemoved(index)
    }

    internal fun deleteDosageFormRow(dosage: Dosage) {
        val index = dosageInstructionRows.indexOf(dosage)
        dosageInstructionRows.removeAt(index)
        dosageInstructionFormAdapter.notifyItemRemoved(index)
    }

    private fun chooseDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(
                CalendarConstraints.Builder().setStart(
                    LocalDateTime.of(1970, 1, 1, 0, 0).atZone(
                ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli()).build()
            )
            .build()

        datePicker.addOnPositiveButtonClickListener { date ->
            val timePicker = MaterialTimePicker.Builder().setHour(startHour).setMinute(startMinute).setTimeFormat(
                if (DateFormat.is24HourFormat(this)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            ).build()

            timePicker.addOnPositiveButtonClickListener {
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.timeInMillis = date
                pickedDateTime.set(pickedDateTime[Calendar.YEAR], pickedDateTime[Calendar.MONTH], pickedDateTime[Calendar.DATE], timePicker.hour, timePicker.minute)
                authoredOn = Timestamp(pickedDateTime.time)
                binding.authoredOnEditText.setText(SimpleDateFormat.getDateTimeInstance().format(pickedDateTime.time))
            }
            timePicker.show(supportFragmentManager, "AuthoredOnTime")
        }
        datePicker.show(supportFragmentManager, "AuthoredOnDate")
    }

    private fun updateMedicationRequest(mr: MedicationRequest): String {
        mr.identifiers.addAll(identifierRows)
        mr.status = binding.statusSpinner.text.toString()
        if (binding.statusReasonEditText.text?.isNotEmpty() == true)
            mr.statusReason = binding.statusReasonEditText.text.toString()
        mr.intent = binding.intentSpinner.text.toString()
        mr.categories.addAll(categoryRows)
        mr.priority = binding.prioritySpinner.text.toString()
        mr.doNotPerform = when (binding.performRadioGroup.checkedRadioButtonId) {
            R.id.perform_radio_yes -> false
            R.id.perform_radio_no -> true
            else -> null
        }
        mr.reported = when (binding.reportedRadioGroup.checkedRadioButtonId) {
            R.id.reported_radio_yes -> true
            R.id.reported_radio_no -> false
            else -> null
        }
        mr.medication = binding.medicationEditText.text.toString()
        mr.subject = binding.subjectEditText.text.toString()
        mr.authoredOn = authoredOn
        noteRows.forEach { note ->
            if (note.text.isNotBlank())
                mr.notes.add(note)
        }
        dosageInstructionRows.forEach { dosage ->
            if (dosage.text.isNotBlank())
                mr.dosageInstructions.add(dosage)
        }
        val sub = Substitution()
        sub.allowed = when (binding.substitutionAllowedRadioGroup.checkedRadioButtonId) {
            R.id.substitution_allowed_radio_yes -> true
            else -> false
        }
        if (binding.substitutionReasonText.text?.isNotEmpty() == true)
            sub.reason = binding.substitutionReasonText.text.toString()
        mr.substitution = sub
        if (mr.id.isEmpty()) {
            medReqViewModel.insert(mr)
            return "Medication request added!"
        }
        medReqViewModel.update(mr)
        return "Medication request updated!"
    }

    private fun populateForm(mr: MedicationRequest) {
        if (mr.identifiers.isNotEmpty()) {
            identifierRows.addAll(mr.identifiers)
            identifierFormAdapter.notifyDataSetChanged()
        }
        binding.statusSpinner.setText(mr.status)
        if (mr.statusReason != null)
            binding.statusReasonEditText.setText(mr.statusReason)
        binding.intentSpinner.setText(mr.intent)
        if (mr.categories.isNotEmpty()) {
            categoryRows.addAll(mr.categories)
            categoryFormAdapter.notifyDataSetChanged()
        }
        if (mr.priority != null)
            binding.prioritySpinner.setText(mr.priority)
        binding.performRadioGroup.check(
            when (mr.doNotPerform) {
                true -> R.id.perform_radio_no
                false -> R.id.perform_radio_yes
                null -> R.id.perform_radio_none
            }
        )
        binding.reportedRadioGroup.check(
            when (mr.reported) {
                true -> R.id.reported_radio_yes
                false -> R.id.reported_radio_no
                null -> R.id.reported_radio_none
            }
        )
        binding.medicationEditText.setText(mr.medication)
        binding.subjectEditText.setText(mr.subject)
        val ao = mr.authoredOn
        if (ao != null) {
            binding.authoredOnEditText.setText(SimpleDateFormat.getDateTimeInstance().format(ao.toDate()))
            authoredOn = ao
        }
        if (mr.notes.isNotEmpty()) {
            noteRows.addAll(mr.notes)
            noteFormAdapter.notifyDataSetChanged()
        }
        if (mr.dosageInstructions.isNotEmpty()) {
            dosageInstructionRows.addAll(mr.dosageInstructions)
            dosageInstructionFormAdapter.notifyDataSetChanged()
        }
        val sub = mr.substitution
        if (sub != null) {
            binding.substitutionAllowedRadioGroup.check(
                when (sub.allowed) {
                    true -> R.id.substitution_allowed_radio_yes
                    false -> R.id.substitution_allowed_radio_no
                }
            )
            if (sub.reason != null)
                binding.substitutionReasonText.setText(sub.reason)
        }
    }
}