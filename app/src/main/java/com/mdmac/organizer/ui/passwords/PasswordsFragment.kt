package com.mdmac.organizer.ui.passwords

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.data.passwords.PasswordRepository
import com.mdmac.organizer.databinding.FragmentPasswordsBinding
import com.mdmac.organizer.security.PinManager
import kotlinx.coroutines.launch

class PasswordsFragment : Fragment() {

    private var _binding: FragmentPasswordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinManager: PinManager
    private var pendingFirstPin: String? = null // holds step-1 PIN while awaiting confirmation

    private val viewModel: PasswordViewModel by viewModels {
        val dao = OrganizerDatabase.getInstance(requireContext()).passwordDao()
        PasswordViewModel.Factory(PasswordRepository(dao))
    }

    private lateinit var adapter: PasswordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinManager = PinManager(requireContext())

        setupList()
        showLockScreen()
        binding.btnSubmitPin.setOnClickListener { handlePinSubmit() }
    }

    private fun setupList() {
        adapter = PasswordAdapter(
            onClick = { entry ->
                AddEditPasswordDialog(entry) { viewModel.save(it) }
                    .show(childFragmentManager, "edit_password")
            },
            onLongClick = { entry ->
                viewModel.delete(entry)
                true
            },
            onCopy = { plainPassword ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("password", plainPassword))
                Toast.makeText(requireContext(), "Password copied", Toast.LENGTH_SHORT).show()
            }
        )
        binding.passwordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.passwordsRecyclerView.adapter = adapter

        binding.fabAddPassword.setOnClickListener {
            AddEditPasswordDialog(null) { viewModel.save(it) }
                .show(childFragmentManager, "add_password")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entries.collect { list ->
                    adapter.submitList(list)
                    binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showLockScreen() {
        binding.lockContainer.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
        binding.lockError.visibility = View.GONE
        binding.inputPin.text?.clear()

        if (pinManager.isPinSet()) {
            binding.lockTitle.text = "Enter PIN"
            binding.lockSubtitle.visibility = View.GONE
            binding.btnSubmitPin.text = "Unlock"
        } else {
            binding.lockTitle.text = "Create PIN"
            binding.lockSubtitle.visibility = View.VISIBLE
            binding.lockSubtitle.text = "Choose a PIN to protect this tab"
            binding.btnSubmitPin.text = "Continue"
            pendingFirstPin = null
        }
    }

    private fun handlePinSubmit() {
        val entered = binding.inputPin.text?.toString().orEmpty()
        if (entered.isEmpty()) return

        if (pinManager.isPinSet()) {
            if (pinManager.verifyPin(entered)) {
                unlock()
            } else {
                binding.lockError.visibility = View.VISIBLE
                binding.lockError.text = "Incorrect PIN"
                binding.inputPin.text?.clear()
            }
        } else {
            val first = pendingFirstPin
            if (first == null) {
                pendingFirstPin = entered
                binding.lockTitle.text = "Confirm PIN"
                binding.lockSubtitle.text = "Enter the same PIN again"
                binding.inputPin.text?.clear()
                binding.lockError.visibility = View.GONE
            } else if (first == entered) {
                pinManager.setPin(entered)
                unlock()
            } else {
                binding.lockError.visibility = View.VISIBLE
                binding.lockError.text = "PINs didn't match — try again"
                pendingFirstPin = null
                binding.lockTitle.text = "Create PIN"
                binding.lockSubtitle.text = "Choose a PIN to protect this tab"
                binding.inputPin.text?.clear()
            }
        }
    }

    private fun unlock() {
        binding.lockContainer.visibility = View.GONE
        binding.contentContainer.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
