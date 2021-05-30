package com.example.fhir_medication_request.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fhir_medication_request.MainActivity
import com.example.fhir_medication_request.R
import com.example.fhir_medication_request.database.model.MedicationRequest
import com.example.fhir_medication_request.databinding.MedReqCardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MedicationRequestListAdapter(
    private val requestListFull: ArrayList<MedicationRequest>
) : RecyclerView.Adapter<MedicationRequestListAdapter.ViewHolder>(), Filterable {

    var guest: Boolean = true

    private var requestListFiltered = requestListFull
    private var lastPos = -1
    private lateinit var binding: MedReqCardBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationRequestListAdapter.ViewHolder {
        binding = MedReqCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationRequestListAdapter.ViewHolder, position: Int) {
        holder.bindTo(requestListFiltered[position])

        if (holder.absoluteAdapterPosition > lastPos) {
            val animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.slide_in_row)
            holder.itemView.startAnimation(animation)
            lastPos = holder.absoluteAdapterPosition
        }
    }

    override fun getItemCount(): Int = requestListFiltered.size

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<MedicationRequest> = ArrayList()
            val results = FilterResults()

            if (constraint == null || constraint.isEmpty()) {
                results.count = requestListFull.size
                results.values = requestListFull
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                requestListFull.forEach { request ->
                    if (request.medication.lowercase().contains(filterPattern) || request.subject.lowercase().contains(filterPattern))
                        filteredList.add(request)
                }

                results.count = filteredList.size
                results.values = filteredList
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            @Suppress("UNCHECKED_CAST") // type erasure ノಠ益ಠノ彡┻━┻
            requestListFiltered = results.values as ArrayList<MedicationRequest>
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(private val medReqBinding: MedReqCardBinding) : RecyclerView.ViewHolder(medReqBinding.root) {
        private val status: TextView = medReqBinding.medReqStatusText
        private val intent: TextView = medReqBinding.medReqIntentText
        private val medication: TextView = medReqBinding.medReqMedicationText
        private val subject: TextView = medReqBinding.medReqSubjectText

        fun bindTo(request: MedicationRequest) {
            status.text = request.status
            intent.text = request.intent
            medication.text = request.medication
            subject.text = request.subject

            itemView.setOnClickListener {
                (itemView.context as MainActivity).startAddOrEditActivity(request)
            }

            val deleteBtn = medReqBinding.medReqDeleteBtn
            deleteBtn.isEnabled = !guest
            deleteBtn.setOnClickListener {
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this medication request?")
                    .setIcon(R.drawable.ic_baseline_delete_24)
                    .setPositiveButton(R.string.yes) { _, _ -> (itemView.context as MainActivity).deleteItem(request) }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        }
    }
}