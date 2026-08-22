package com.mdmac.organizer.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.R
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.data.notes.NoteRepository
import com.mdmac.organizer.databinding.FragmentNotesBinding
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels {
        val dao = OrganizerDatabase.getInstance(requireContext()).noteDao()
        NotesViewModel.Factory(NoteRepository(dao))
    }

    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NoteAdapter(
            onClick = { note -> openEditor(noteId = note.id, folderId = null) },
            onLongClick = { note -> viewModel.deleteNote(note); true }
        )
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notesRecyclerView.adapter = adapter

        binding.fabAddNote.setOnClickListener {
            val currentSel = viewModel.selection.value
            val folderId = (currentSel as? FolderSelection.Specific)?.folderId
            openEditor(noteId = 0L, folderId = folderId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notes.collect { list ->
                        adapter.submitList(list)
                        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch { viewModel.folders.collect { rebuildChips(it) } }
            }
        }
    }

    private fun rebuildChips(folders: List<com.mdmac.organizer.data.notes.NoteFolder>) {
        val group = binding.folderChipGroup
        group.removeAllViews()

        group.addView(makeChip("All", true) { viewModel.select(FolderSelection.All) })
        group.addView(makeChip("Unfiled", false) { viewModel.select(FolderSelection.Unfiled) })
        folders.forEach { folder ->
            group.addView(makeChip(folder.name, false) {
                viewModel.select(FolderSelection.Specific(folder.id, folder.name))
            })
        }
        group.addView(makeChip("+ New folder", false, checkable = false) { showNewFolderDialog() })
    }

    private fun makeChip(label: String, checked: Boolean, checkable: Boolean = true, onSelected: () -> Unit): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = checkable
            isChecked = checked
            setChipBackgroundColorResource(android.R.color.transparent)
            chipStrokeWidth = 2f
            setOnClickListener { onSelected() }
        }
    }

    private fun showNewFolderDialog() {
        val input = EditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isNotEmpty()) viewModel.addFolder(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditor(noteId: Long, folderId: Long?) {
        val intent = android.content.Intent(requireContext(), NoteEditorActivity::class.java)
        intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, noteId)
        intent.putExtra(NoteEditorActivity.EXTRA_FOLDER_ID, folderId ?: -1L)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
