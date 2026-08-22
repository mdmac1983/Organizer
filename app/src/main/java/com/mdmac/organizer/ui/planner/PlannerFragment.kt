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
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.data.planner.PlannerRepository
import com.mdmac.organizer.databinding.FragmentPlannerBinding
import kotlinx.coroutines.launch

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlannerViewModel by viewModels {
        val dao = OrganizerDatabase.getInstance(requireContext()).plannerDao()
        PlannerViewModel.Factory(PlannerRepository(dao))
    }

    private lateinit var adapter: PlannerEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlannerEntryAdapter(
            onClick = { entry ->
                AddEditPlannerEntryDialog(entry) { viewModel.save(it) }
                    .show(childFragmentManager, "edit_entry")
            },
            onLongClick = { entry ->
                viewModel.delete(entry)
                true
            }
        )
        binding.entriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.entriesRecyclerView.adapter = adapter

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
                    viewModel.entries.collect { list ->
                        adapter.submitList(list)
                        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    combine2(viewModel.anchorMillis, viewModel.mode) { anchor, mode ->
                        binding.periodLabel.text =
                            com.mdmac.organizer.util.DateRangeUtils.label(anchor, mode)
                    }
                }
            }
        }
    }

    // small local helper since combine() needs both flows collected together
    private suspend fun combine2(
        f1: kotlinx.coroutines.flow.StateFlow<Long>,
        f2: kotlinx.coroutines.flow.StateFlow<PlannerViewMode>,
        action: (Long, PlannerViewMode) -> Unit
    ) {
        kotlinx.coroutines.flow.combine(f1, f2) { a, m -> a to m }
            .collect { (a, m) -> action(a, m) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
