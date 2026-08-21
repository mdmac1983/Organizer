package com.mdmac.organizer.ui.planner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.data.planner.PlannerEntry
import com.mdmac.organizer.databinding.DialogPlannerEntryBinding
import com.mdmac.organizer.util.DateRangeUtils
import java.util.Calendar

class AddEditPlannerEntryDialog(
    private val existing: PlannerEntry? = null,
    private val onSave: (PlannerEntry) -> Unit
) : DialogFragment() {

    private var selectedMillis: Long = existing?.dateTimeMillis ?: System.currentTimeMillis()

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val binding = DialogPlannerEntryBinding.inflate(layoutInflater)

        binding.inputTitle.setText(existing?.title ?: "")
        binding.inputNotes.setText(existing?.notes ?: "")
        binding.btnPickDateTime.text = DateRangeUtils.formatEntryTime(selectedMillis)

        binding.btnPickDateTime.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                TimePickerDialog(requireContext(), { _, h, min ->
                    cal.set(Calendar.HOUR_OF_DAY, h)
                    cal.set(Calendar.MINUTE, min)
                    selectedMillis = cal.timeInMillis
                    binding.btnPickDateTime.text = DateRangeUtils.formatEntryTime(selectedMillis)
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "New entry" else "Edit entry")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val title = binding.inputTitle.text?.toString()?.trim().orEmpty()
                if (title.isNotEmpty()) {
                    onSave(
                        PlannerEntry(
                            id = existing?.id ?: 0,
                            title = title,
                            dateTimeMillis = selectedMillis,
                            notes = binding.inputNotes.text?.toString()?.trim().orEmpty()
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .apply { if (existing != null) setNeutralButton("Delete", null) }
            .create()
    }
}
