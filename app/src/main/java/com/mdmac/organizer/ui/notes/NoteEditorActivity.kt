package com.mdmac.organizer.ui.notes

import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.data.notes.NoteBlock
import com.mdmac.organizer.data.notes.NoteBlockType
import com.mdmac.organizer.data.notes.NoteImageStore
import com.mdmac.organizer.data.notes.NoteRepository
import com.mdmac.organizer.databinding.ActivityNoteEditorBinding

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditorBinding
    private lateinit var blockAdapter: NoteBlockAdapter

    private val viewModel: NoteEditorViewModel by viewModels {
        val dao = OrganizerDatabase.getInstance(applicationContext).noteDao()
        NoteEditorViewModel.Factory(NoteRepository(dao))
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = NoteImageStore.save(this, uri)
            if (path != null) {
                viewModel.blocks.add(NoteBlock(type = NoteBlockType.IMAGE, text = path))
                blockAdapter.notifyItemInserted(viewModel.blocks.size - 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        val folderIdExtra = intent.getLongExtra(EXTRA_FOLDER_ID, -1L)
        val folderId = if (folderIdExtra == -1L) null else folderIdExtra

        binding.toolbar.setNavigationOnClickListener { saveAndFinish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == com.mdmac.organizer.R.id.action_delete_note) {
                viewModel.blocks.clear()
                viewModel.title = ""
                finish()
                true
            } else false
        }

        blockAdapter = NoteBlockAdapter(viewModel.blocks) { blockAdapter.notifyDataSetChanged() }
        binding.blocksRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.blocksRecyclerView.adapter = blockAdapter

        binding.btnAddText.setOnClickListener {
            viewModel.blocks.add(NoteBlock(type = NoteBlockType.TEXT))
            blockAdapter.notifyItemInserted(viewModel.blocks.size - 1)
        }
        binding.btnAddChecklistItem.setOnClickListener {
            viewModel.blocks.add(NoteBlock(type = NoteBlockType.CHECKLIST_ITEM))
            blockAdapter.notifyItemInserted(viewModel.blocks.size - 1)
        }
        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        viewModel.loadIfNeeded(noteId, folderId) {
            binding.inputTitle.setText(viewModel.title)
            blockAdapter.notifyDataSetChanged()
        }

        binding.inputTitle.doAfterTextChangedCompat { viewModel.title = it }
    }

    private fun saveAndFinish() {
        viewModel.title = binding.inputTitle.text?.toString().orEmpty()
        viewModel.save { finish() }
    }

    override fun onBackPressed() {
        saveAndFinish()
    }

    private fun android.widget.EditText.doAfterTextChangedCompat(action: (String) -> Unit) {
        addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { action(s?.toString().orEmpty()) }
        })
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_FOLDER_ID = "extra_folder_id"
    }
}
