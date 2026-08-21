package com.mdmac.organizer.ui.contacts

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.organizer.data.contacts.Contact
import com.mdmac.organizer.databinding.ItemContactBinding

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val onLongClick: (Contact) -> Boolean
) : ListAdapter<Contact, ContactAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val contact = getItem(position)
        holder.binding.contactName.text = contact.name
        holder.binding.contactSubtitle.text = contact.phone.ifBlank { contact.email }
        if (!contact.photoUri.isNullOrBlank()) {
            holder.binding.contactPhoto.setImageURI(Uri.parse(contact.photoUri))
        } else {
            holder.binding.contactPhoto.setImageDrawable(null)
        }
        holder.itemView.setOnClickListener { onClick(contact) }
        holder.itemView.setOnLongClickListener { onLongClick(contact) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(old: Contact, new: Contact) = old.id == new.id
            override fun areContentsTheSame(old: Contact, new: Contact) = old == new
        }
    }
}
