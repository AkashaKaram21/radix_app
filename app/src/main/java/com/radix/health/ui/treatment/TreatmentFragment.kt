package com.radix.health.ui.treatment

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
import com.radix.health.R
import com.radix.health.RadixApplication
import com.radix.health.data.model.Treatment
import com.radix.health.data.repository.RadixRepository
import com.radix.health.databinding.FragmentTreatmentBinding
import com.radix.health.session.SessionManager
import com.radix.health.util.Formatters
import com.radix.health.util.IsolationProgress
import com.radix.health.util.UiState
import kotlinx.coroutines.launch

class TreatmentFragment : Fragment() {

    private var _binding: FragmentTreatmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TreatmentViewModel by viewModels {
        val app = RadixApplication.get()
        TreatmentViewModel.Factory(app.repository, app.sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreatmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is UiState.Loading
            when (state) {
                is UiState.Success -> bind(state.data)
                is UiState.Error -> showEmpty()
                else -> Unit
            }
        }
        viewModel.load()
    }

    private fun showEmpty() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE
    }

    private fun bind(treatment: Treatment?) {
        if (treatment == null) { showEmpty(); return }
        binding.tvEmpty.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE

        binding.tvIsotope.text = treatment.isotopeName ?: "—"
        binding.tvRoom.text = treatment.room.toString()
        binding.tvInitialDose.text = "${treatment.initialDose} mCi"
        binding.tvSafety.text = "${treatment.safetyThreshold} mSv"
        binding.tvIsolationDays.text = "${treatment.isolationDays}"
        binding.tvStart.text = Formatters.shortDate(treatment.startDate)
        binding.tvEnd.text = treatment.endDate?.let(Formatters::shortDate) ?: "—"
        binding.tvStatus.setText(
            if (treatment.isActive) R.string.treatment_status_active
            else R.string.treatment_status_completed
        )
        val progress = IsolationProgress.compute(treatment.startDate, treatment.isolationDays)
        binding.tvRemainingTime.text = "${progress.remainingDays}d ${progress.remainingHours}h"
        binding.progressTreatment.progress = (progress.elapsedFraction * 100).toInt()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class TreatmentViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<UiState<Treatment?>>(UiState.Idle)
    val state: LiveData<UiState<Treatment?>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdOnce() ?: error("Sin sesión")
                val patient = repository.getPatientByUser(userId)
                val list = repository.getTreatments(patient.id)
                val current = list.firstOrNull { it.isActive } ?: list.firstOrNull()
                _state.value = UiState.Success(current)
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
            TreatmentViewModel(repository, sessionManager) as T
    }
}
