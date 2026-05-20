package com.radix.health.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.radix.health.R
import com.radix.health.RadixApplication
import com.radix.health.databinding.FragmentDashboardBinding
import com.radix.health.util.Formatters
import com.radix.health.util.IsolationProgress
import com.radix.health.util.UiState

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        val app = RadixApplication.get()
        DashboardViewModel.Factory(app.repository, app.sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is UiState.Loading
            when (state) {
                is UiState.Success -> bind(state.data)
                is UiState.Error -> binding.tvGreeting.text =
                    getString(R.string.generic_error)
                else -> Unit
            }
        }
        viewModel.load()
    }

    private fun bind(data: DashboardData) {
        val patientName = data.patient?.fullName?.takeIf { it.isNotBlank() } ?: "Paciente"
        binding.tvGreeting.text = getString(R.string.dashboard_greeting, patientName)

        val treatment = data.activeTreatment
        if (treatment != null) {
            val progress = IsolationProgress.compute(treatment.startDate, treatment.isolationDays)
            val remainingPct = (progress.remainingFraction * 100).toInt()
            binding.tvIsolationPercent.text = "$remainingPct%"
            binding.tvIsolationRemaining.text =
                "${progress.remainingDays}d ${progress.remainingHours}h"
            binding.progressIsolation.progress = remainingPct
            binding.tvTreatmentStatus.setText(
                if (treatment.isActive) R.string.dashboard_treatment_active
                else R.string.dashboard_treatment_inactive
            )
        } else {
            binding.tvIsolationPercent.text = "—"
            binding.tvIsolationRemaining.text = ""
            binding.progressIsolation.progress = 0
            binding.tvTreatmentStatus.setText(R.string.dashboard_treatment_inactive)
        }

        val metrics = data.latestMetrics
        binding.tvBpm.text = Formatters.integerOrDash(metrics?.bpm, " BPM")
        binding.tvSteps.text = Formatters.integerOrDash(metrics?.steps, "")
        binding.tvDistance.text = Formatters.decimalOrDash(metrics?.distance, 2, " km")
        binding.tvRadiation.text = Formatters.decimalOrDash(metrics?.currentRadiation, 2, " mSv")
        binding.tvSleep.text = Formatters.sleepDuration(metrics?.sleepQualityMinutes)
        binding.tvAlerts.text = getString(R.string.dashboard_alerts_pending, data.pendingAlerts)
        binding.tvUpdated.text = "${Formatters.shortDate(metrics?.recordedAt)} · " +
            Formatters.time(metrics?.recordedAt)

        binding.tvMotivation.text = data.lastMessage?.messageText
            ?: "Cada día de aislamiento es un paso hacia tu recuperación. Mantente fuerte."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
