package com.mdmac.organizer.ui.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.R
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.data.planner.PlannerRepository
import com.mdmac.organizer.databinding.FragmentPlannerBinding
import com.mdmac.organizer.databinding.ItemDayStripBoxBinding
import com.mdmac.organizer.util.DateRangeUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlannerViewModel by viewModels {
        val dao = OrganizerDatabase.getInstance(requireContext()).plannerDao()
        PlannerViewModel.Factory(PlannerRepository(dao))
    }

    private lateinit var entryAdapter: PlannerEntryAdapter
    private lateinit var calendarAdapter: CalendarGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        entryAdapter = PlannerEntryAdapter(
            onClick = { entry ->
                AddEditPlannerEntryDialog(entry) { viewModel.save(it) }
                    .show(childFragmentManager, "edit_entry")
            },
            onLongClick = { entry ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete entry?")
                    .setMessage("This can't be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(entry) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        )
        binding.entriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.entriesRecyclerView.adapter = entryAdapter

        calendarAdapter = CalendarGridAdapter(onDayClick = { day -> viewModel.selectDay(day.dateMillis) })
        binding.calendarGridRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.calendarGridRecyclerView.adapter = calendarAdapter

        binding.modeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                binding.btnDay.id -> PlannerViewMode.DAY
                binding.btnMonth.id -> PlannerViewMode.MONTH
                else -> PlannerViewMode.WEEK
            }
            viewModel.setMode(mode)
        }
