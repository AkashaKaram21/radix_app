package com.radix.health.ui.health

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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.radix.health.RadixApplication
import com.radix.health.data.model.HealthMetric
import com.radix.health.data.model.RadiationLog
import com.radix.health.data.repository.RadixRepository
import com.radix.health.databinding.FragmentHealthBinding
import com.radix.health.session.SessionManager
import com.radix.health.util.Formatters
import com.radix.health.util.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Métricas de salud agregadas (cardio, pasos, distancia y radiación) usando
 * MPAndroidChart para el gráfico de tendencia, según apuntes del curso.
 */
class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HealthViewModel by viewModels {
        val app = RadixApplication.get()
        HealthViewModel.Factory(app.repository, app.sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is UiState.Loading
            if (state is UiState.Success) bind(state.data)
        }
        viewModel.load()
    }

    private fun bind(data: HealthSummary) {
        val latest = data.metrics.lastOrNull()
        binding.tvBpm.text = Formatters.integerOrDash(latest?.bpm, " BPM")
        binding.tvSteps.text = Formatters.integerOrDash(latest?.steps, "")
        binding.tvDistance.text = Formatters.decimalOrDash(latest?.distance, 2, " km")
        binding.tvRadiation.text = if (data.radiationLogs.isEmpty()) {
            getString(com.radix.health.R.string.health_no_radiation)
        } else {
            Formatters.decimalOrDash(data.radiationLogs.last().radiationLevel, 2, " mSv")
        }

        val entries = data.metrics
            .mapIndexedNotNull { i, m -> m.bpm?.let { Entry(i.toFloat(), it.toFloat()) } }
        val set = LineDataSet(entries, "BPM").apply {
            setDrawCircles(false)
            lineWidth = 2f
            setDrawValues(false)
        }
        binding.chart.data = LineData(set)
        binding.chart.description.isEnabled = false
        binding.chart.legend.isEnabled = false
        binding.chart.invalidate()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

data class HealthSummary(
    val metrics: List<HealthMetric>,
    val radiationLogs: List<RadiationLog>
)

class HealthViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<UiState<HealthSummary>>(UiState.Idle)
    val state: LiveData<UiState<HealthSummary>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdOnce() ?: error("Sin sesión")
                val patient = repository.getPatientByUser(userId)
                val summary = coroutineScope {
                    val metrics = async {
                        runCatching { repository.getHealthMetrics(patient.id, 30) }
                            .getOrDefault(emptyList())
                    }
                    val rad = async {
                        runCatching { repository.getRadiationLogs(patient.id, 30) }
                            .getOrDefault(emptyList())
                    }
                    HealthSummary(metrics.await(), rad.await())
                }
                _state.value = UiState.Success(summary)
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
            HealthViewModel(repository, sessionManager) as T
    }
}
