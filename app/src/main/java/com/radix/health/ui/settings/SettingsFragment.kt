package com.radix.health.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.radix.health.RadixApplication
import com.radix.health.data.model.Patient
import com.radix.health.data.repository.RadixRepository
import com.radix.health.databinding.FragmentSettingsBinding
import com.radix.health.session.SessionManager
import com.radix.health.ui.login.LoginActivity
import com.radix.health.util.UiState
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        val app = RadixApplication.get()
        SettingsViewModel.Factory(app.repository, app.sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) bind(state.data)
        }
        viewModel.load()

        binding.rowTheme.setOnClickListener { showThemePicker() }
        binding.rowLogout.setOnClickListener { confirmLogout() }
    }

    private fun bind(patient: Patient) {
        binding.tvName.text = patient.fullName
        binding.tvFamilyCode.text = patient.familyAccessCode ?: "—"
        binding.tvPhone.text = patient.phone ?: "—"
    }

    private fun showThemePicker() {
        val labels = arrayOf("Sistema", "Claro", "Oscuro")
        val modes = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        )
        AlertDialog.Builder(requireContext())
            .setTitle(com.radix.health.R.string.settings_theme)
            .setItems(labels) { _, which ->
                AppCompatDelegate.setDefaultNightMode(modes[which])
            }
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setMessage(com.radix.health.R.string.settings_logout)
            .setPositiveButton(com.radix.health.R.string.generic_save) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    RadixApplication.get().sessionManager.clear()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            .setNegativeButton(com.radix.health.R.string.generic_cancel, null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class SettingsViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<UiState<Patient>>(UiState.Idle)
    val state: LiveData<UiState<Patient>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdOnce() ?: error("Sin sesión")
                _state.value = UiState.Success(repository.getPatientByUser(userId))
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "")
            }
        }
    }

    class Factory(
        private val repository: RadixRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(repository, sessionManager) as T
    }
}
