package com.mdmac.organizer.ui.notes

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.data.notes.NoteBlock
import com.mdmac.organizer.data.notes.NoteBlockType
import com.mdmac.organizer.databinding.ItemNoteBlockBinding

// Note: mutates NoteBlock objects in place rather than using DiffUtil,
// so typing doesn't trigger rebinds that would fight the keyboard/cursor.
class NoteBlockAdapter(
    private val blocks: MutableList<NoteBlock>,
    private val onDeleted: () -> Unit
) : RecyclerView.Adapter<NoteBlockAdapter.VH>() {

    inner class VH(val binding: ItemNoteBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        var watcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNoteBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val block = blocks[position]
        val binding = holder.binding

        holder.watcher?.let { binding.blockText.removeTextChangedListener(it) }
        if (binding.blockText.text?.toString() != block.text) {
            binding.blockText.setText(block.text)
        }

        val isChecklist = block.type == NoteBlockType.CHECKLIST_ITEM
        binding.blockCheckbox.visibility = if (isChecklist) View.VISIBLE else View.GONE
        binding.blockText.setLines(if (isChecklist) 1 else 1)
        binding.blockText.maxLines = if (isChecklist) 1 else 20
        binding.blockText.isSingleLine = isChecklist

        if (isChecklist) {
            binding.blockCheckbox.setOnCheckedChangeListener(null)
            binding.blockCheckbox.isChecked = block.checked
            applyStrike(binding.blockText, block.checked)
            binding.blockCheckbox.setOnCheckedChangeListener { _, checked ->
                block.checked = checked
                applyStrike(binding.blockText, checked)
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { block.text = s?.toString().orEmpty() }
        }
        binding.blockText.addTextChangedListener(watcher)
        holder.watcher = watcher

        binding.blockDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                blocks.removeAt(pos)
                notifyItemRemoved(pos)
                onDeleted()
            }
        }
    }

    private fun applyStrike(editText: android.widget.EditText, struck: Boolean) {
        editText.paintFlags = if (struck) {
            editText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            editText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int = blocks.size
}
