package com.radix.health.ui.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.radix.health.R
import com.radix.health.RadixApplication
import com.radix.health.data.model.Alert
import com.radix.health.data.repository.RadixRepository
import com.radix.health.databinding.FragmentAlertsBinding
import com.radix.health.session.SessionManager
import com.radix.health.util.UiState
import kotlinx.coroutines.launch

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlertsViewModel by viewModels {
        val app = RadixApplication.get()
        AlertsViewModel.Factory(app.repository, app.sessionManager)
    }

    private val adapter = AlertAdapter { alert -> viewModel.resolve(alert.id) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        binding.tabFilter.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                viewModel.setFilter(
                    when (tab.position) {
                        1 -> AlertFilter.PENDING
                        2 -> AlertFilter.RESOLVED
                        else -> AlertFilter.ALL
                    }
                )
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is UiState.Loading
            if (state is UiState.Success) {
                adapter.submitList(state.data)
                binding.tvEmpty.visibility =
                    if (state.data.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        viewModel.load()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

enum class AlertFilter { ALL, PENDING, RESOLVED }

class AlertsViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private var raw: List<Alert> = emptyList()
    private var filter = AlertFilter.ALL

    private val _state = MutableLiveData<UiState<List<Alert>>>(UiState.Idle)
    val state: LiveData<UiState<List<Alert>>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdOnce() ?: error("Sin sesión")
                val patient = repository.getPatientByUser(userId)
                raw = repository.getAlerts(patient.id)
                emitFiltered()
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "")
            }
        }
    }

    fun setFilter(f: AlertFilter) { filter = f; emitFiltered() }

    fun resolve(id: Long) {
        viewModelScope.launch {
            runCatching { repository.resolveAlert(id) }
            load()
        }
    }

    private fun emitFiltered() {
        val list = when (filter) {
            AlertFilter.ALL -> raw
            AlertFilter.PENDING -> raw.filter { !it.isResolved }
            AlertFilter.RESOLVED -> raw.filter { it.isResolved }
        }.sortedByDescending { it.createdAt }
        _state.value = UiState.Success(list)
    }

    class Factory(
        private val repository: RadixRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlertsViewModel(repository, sessionManager) as T
    }
}
