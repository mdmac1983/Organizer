package com.mdmac.organizer.ui.contacts

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.data.contacts.ContactRepository
import com.mdmac.organizer.databinding.FragmentContactsBinding
import kotlinx.coroutines.launch

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels {
        val dao = OrganizerDatabase.getInstance(requireContext()).contactDao()
        ContactViewModel.Factory(ContactRepository(dao))
    }

    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactAdapter(
            onClick = { contact ->
                AddEditContactDialog(contact) { viewModel.save(it) }
                    .show(childFragmentManager, "edit_contact")
            },
            onLongClick = { contact ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete contact?")
                    .setMessage("This can't be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(contact) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        )
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.contactsRecyclerView.adapter = adapter

        binding.fabAddContact.setOnClickListener {
            AddEditContactDialog(null) { viewModel.save(it) }
                .show(childFragmentManager, "add_contact")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contacts.collect { list ->
                    adapter.submitList(list)
                    binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
