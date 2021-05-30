package com.example.fhir_medication_request.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.fhir_medication_request.R
import com.example.fhir_medication_request.database.model.*
import com.example.fhir_medication_request.databinding.CategoryFormBinding
import com.example.fhir_medication_request.databinding.DosageFormBinding
import com.example.fhir_medication_request.databinding.NoteFormBinding
import com.example.fhir_medication_request.databinding.UserFormBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class FormAdapter<T, B : ViewBinding>(
    private val rows: List<T>,
    private val listClass: Class<T>,
    private val bindingClass: Class<B>
) : RecyclerView.Adapter<FormAdapter<T, B>.ViewHolder>() {

    internal var isGuest = true

    companion object {
        inline operator fun <reified T, reified B: ViewBinding>invoke(rows: List<T>) = FormAdapter(rows, T::class.java, B::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when {
            bindingClass.isAssignableFrom(UserFormBinding::class.java) && listClass.isAssignableFrom(User::class.java) ->
                UserViewHolder(UserFormBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            bindingClass.isAssignableFrom(CategoryFormBinding::class.java) && listClass.isAssignableFrom(Category::class.java) ->
                CategoryViewHolder(CategoryFormBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            bindingClass.isAssignableFrom(NoteFormBinding::class.java) && listClass.isAssignableFrom(Note::class.java) ->
                NoteViewHolder(NoteFormBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            bindingClass.isAssignableFrom(DosageFormBinding::class.java) && listClass.isAssignableFrom(Dosage::class.java) ->
                DosageViewHolder(DosageFormBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> null!! // Should never get called -> will throw a NPE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(rows[position])

    override fun getItemCount(): Int = rows.size

    abstract inner class ViewHolder(viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        abstract fun bind(item: T)
    }

    inner class UserViewHolder(private val identifierFormBinding: UserFormBinding) : ViewHolder(identifierFormBinding) {
        override fun bind(item: T) {
            val user = item as User
            with (identifierFormBinding.identifierUid) {
                isEnabled = !isGuest
                setText(user.uid.value)
                addTextChangedListener { user.uid.value = it.toString() }
            }
            with (identifierFormBinding.identifierEmail) {
                isEnabled = !isGuest
                setText(user.email.value)
                addTextChangedListener { user.email.value = it.toString() }
            }
            with (identifierFormBinding.identifierName) {
                isEnabled = !isGuest
                setText(user.displayName.value)
                addTextChangedListener { user.displayName.value = it.toString() }
            }
            with (identifierFormBinding.identifierFormDeleteBtn) {
                isEnabled = !isGuest
                setOnClickListener {
                    (itemView.context as AddEditActivity).deleteIdentifierFormRow(user)
                }
            }
        }
    }

    inner class CategoryViewHolder(private val categoryFormBinding: CategoryFormBinding) : ViewHolder(categoryFormBinding) {
        override fun bind(item: T) {
            val category = item as Category
            with(categoryFormBinding.categorySpinner) {
                isEnabled = !isGuest
                if (category.text.isNotEmpty())
                    setText(category.text)
                else
                    setText(itemView.context.resources.getStringArray(R.array.categories)[0])
                setAdapter(ArrayAdapter.createFromResource(
                    itemView.context, R.array.categories, R.layout.support_simple_spinner_dropdown_item
                ))
                addTextChangedListener { category.text = it.toString() }
            }
            with (categoryFormBinding.identifierFormDeleteBtn) {
                isEnabled = !isGuest
                setOnClickListener {
                    (itemView.context as AddEditActivity).deleteCategoryFormRow(category)
                }
            }
        }
    }

    inner class NoteViewHolder(private val noteFormBinding: NoteFormBinding) : ViewHolder(noteFormBinding) {
        override fun bind(item: T) {
            val note = item as Note
            with(noteFormBinding.noteAuthor) {
                isEnabled = !isGuest
                val author = note.author
                if (author != null)
                    setText(author.authorString)
                else
                    setText("")
                addTextChangedListener { note.author = Author(it.toString()) }
            }
            with(noteFormBinding.noteDateTime) {
                isEnabled = !isGuest
                text = null
                val dateTime = note.time
                if (dateTime != null)
                    setText(SimpleDateFormat.getDateTimeInstance().format(dateTime.toDate()))
                else
                    setText("")
                setOnClickListener { chooseDateTime(note) }
                addTextChangedListener {
                    val date = try {
                        SimpleDateFormat.getDateTimeInstance().parse(it.toString())
                    } catch (e: Exception) {
                        null
                    }
                    note.time = if (date != null) Timestamp(date) else null
                }
            }
            with(noteFormBinding.noteText) {
                isEnabled = !isGuest
                setText(note.text)
                addTextChangedListener { note.text = it.toString() }
            }
            with (noteFormBinding.noteFormDeleteBtn) {
                isEnabled = !isGuest
                setOnClickListener {
                    (itemView.context as AddEditActivity).deleteNoteFormRow(note)
                }
            }
            with (noteFormBinding.noteDateTimeClearBtn) {
                isEnabled = !isGuest
                setOnClickListener {
                    note.time = null
                    noteFormBinding.noteDateTime.text = null
                }
            }
        }

        private fun chooseDateTime(note: Note) {
            val currentDateTime = Calendar.getInstance()
            val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
            val startMinute = currentDateTime.get(Calendar.MINUTE)

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(CalendarConstraints.Builder().setStart(
                    LocalDateTime.of(1970, 1, 1, 0, 0).atZone(
                        ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli()).build()
                )
                .build()
            val fragManager = (itemView.context as AddEditActivity).supportFragmentManager

            datePicker.addOnPositiveButtonClickListener { date ->
                val timePicker = MaterialTimePicker.Builder().setHour(startHour).setMinute(startMinute).setTimeFormat(
                    if (DateFormat.is24HourFormat(itemView.context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
                ).build()

                timePicker.addOnPositiveButtonClickListener {
                    val pickedDateTime = Calendar.getInstance()
                    pickedDateTime.timeInMillis = date
                    pickedDateTime.set(pickedDateTime[Calendar.YEAR], pickedDateTime[Calendar.MONTH], pickedDateTime[Calendar.DATE], timePicker.hour, timePicker.minute)
                    note.time = Timestamp(pickedDateTime.time)
                    noteFormBinding.noteDateTime.setText(SimpleDateFormat.getDateTimeInstance().format(pickedDateTime.time))
                }
                timePicker.show(fragManager, "AuthoredOnTime")
            }
            datePicker.show(fragManager, "AuthoredOnDate")
        }
    }

    inner class DosageViewHolder(private val dosageFormBinding: DosageFormBinding) : ViewHolder(dosageFormBinding) {
        override fun bind(item: T) {
            val dosage = item as Dosage
            with(dosageFormBinding.dosageText) {
                isEnabled = !isGuest
                setText(dosage.text)
                addTextChangedListener { dosage.text = it.toString() }
            }
            with (dosageFormBinding.dosageFormDeleteBtn) {
                isEnabled = !isGuest
                setOnClickListener {
                    (itemView.context as AddEditActivity).deleteDosageFormRow(dosage)
                }
            }
        }
    }
}