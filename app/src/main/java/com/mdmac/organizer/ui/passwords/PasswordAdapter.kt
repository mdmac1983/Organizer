package com.mdmac.organizer.ui.passwords

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.data.passwords.PasswordCrypto
import com.mdmac.organizer.data.passwords.PasswordEntry
import com.mdmac.organizer.databinding.ItemPasswordBinding

class PasswordAdapter(
    private val onClick: (PasswordEntry) -> Unit,
    private val onLongClick: (PasswordEntry) -> Boolean,
    private val onCopy: (String) -> Unit
) : ListAdapter<PasswordEntry, PasswordAdapter.VH>(DIFF) {

    private val revealedIds = mutableSetOf<Long>()

    inner class VH(val binding: ItemPasswordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPasswordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = getItem(position)
        holder.binding.siteName.text = entry.siteName
        holder.binding.username.text = entry.username

        val revealed = revealedIds.contains(entry.id)
        holder.binding.passwordMasked.text =
            if (revealed) PasswordCrypto.decrypt(entry.encryptedPassword) else "••••••••"

        holder.binding.btnToggleReveal.setOnClickListener {
            if (revealed) revealedIds.remove(entry.id) else revealedIds.add(entry.id)
            notifyItemChanged(holder.bindingAdapterPosition)
        }
        holder.binding.btnCopy.setOnClickListener {
            onCopy(PasswordCrypto.decrypt(entry.encryptedPassword))
        }
        holder.itemView.setOnClickListener { onClick(entry) }
        holder.itemView.setOnLongClickListener { onLongClick(entry) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PasswordEntry>() {
            override fun areItemsTheSame(old: PasswordEntry, new: PasswordEntry) = old.id == new.id
            override fun areContentsTheSame(old: PasswordEntry, new: PasswordEntry) = old == new
        }
    }
}
