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
        binding.btnWeek.isChecked = true

        binding.btnPrev.setOnClickListener { viewModel.shift(forward = false) }
        binding.btnNext.setOnClickListener { viewModel.shift(forward = true) }
        binding.periodLabel.setOnClickListener { viewModel.jumpToToday() }

        binding.fabAddEntry.setOnClickListener {
            AddEditPlannerEntryDialog(null) { viewModel.save(it) }
                .show(childFragmentManager, "add_entry")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mode.collect { mode -> updateModeVisibility(mode) }
                }
                launch {
                    viewModel.entries.collect { list ->
                        entryAdapter.submitList(list)
                        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    combine(viewModel.anchorMillis, viewModel.mode) { anchor, mode -> anchor to mode }
                        .collect { (anchor, mode) ->
                            binding.periodLabel.text = DateRangeUtils.label(anchor, mode)
                            when (mode) {
                                PlannerViewMode.WEEK -> populateWeekStrip(anchor)
                                PlannerViewMode.MONTH -> calendarAdapter.submitList(DateRangeUtils.monthGridDays(anchor))
                                PlannerViewMode.DAY -> Unit
                            }
                        }
                }
                launch {
                    viewModel.selectedDayMillis.collect { selected ->
                        calendarAdapter.selectedDayMillis = selected
                    }
                }
                launch {
                    viewModel.monthEntries.collect { list ->
                        calendarAdapter.daysWithEntries = list.map { it.dateTimeMillis }.toSet()
                    }
                }
            }
        }
    }

    private fun updateModeVisibility(mode: PlannerViewMode) {
        binding.weekDayStripContainer.visibility = if (mode == PlannerViewMode.WEEK) View.VISIBLE else View.GONE
        binding.monthCalendarContainer.visibility = if (mode == PlannerViewMode.MONTH) View.VISIBLE else View.GONE
        entryAdapter.bulletStyle = (mode == PlannerViewMode.DAY)
    }

    private fun populateWeekStrip(anchorMillis: Long) {
        binding.weekDayStripContainer.removeAllViews()
        val (weekStart, _) = DateRangeUtils.weekRange(anchorMillis)
        val cal = Calendar.getInstance().apply { timeInMillis = weekStart }
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        repeat(7) {
            val cellBinding = ItemDayStripBoxBinding.inflate(layoutInflater, binding.weekDayStripContainer, false)
            cellBinding.dayStripName.text = dayFormat.format(cal.time).uppercase(Locale.getDefault())
            cellBinding.dayStripNumber.text = cal.get(Calendar.DAY_OF_MONTH).toString()

            val isToday = DateRangeUtils.isSameDay(cal.timeInMillis, System.currentTimeMillis())
            cellBinding.dayStripNumber.setBackgroundResource(
                if (isToday) R.drawable.bg_calendar_day_today else 0
            )

            binding.weekDayStripContainer.addView(cellBinding.root)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
