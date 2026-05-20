package com.radix.health.ui.family

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.radix.health.R
import com.radix.health.RadixApplication
import com.radix.health.data.model.FamilyPatientView
import com.radix.health.data.repository.RadixRepository
import com.radix.health.databinding.ActivityFamilyDashboardBinding
import com.radix.health.util.Formatters
import com.radix.health.util.UiState
import kotlinx.coroutines.launch

/**
 * Vista limitada de un paciente para familiares. No muestra DNI, teléfono,
 * dirección ni IDs internos, sólo el nombre y las métricas más recientes
 * cuando estén disponibles.
 */
class FamilyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFamilyDashboardBinding

    private val viewModel: FamilyDashboardViewModel by viewModels {
        FamilyDashboardViewModel.Factory(RadixApplication.get().repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFamilyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val code = intent.getStringExtra(EXTRA_CODE) ?: run { finish(); return }
        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progress.visibility = android.view.View.VISIBLE
                is UiState.Error -> {
                    binding.progress.visibility = android.view.View.GONE
                    binding.tvError.visibility = android.view.View.VISIBLE
                    binding.tvError.text = state.message.ifBlank {
                        getString(R.string.generic_error)
                    }
                }
                is UiState.Success -> {
                    binding.progress.visibility = android.view.View.GONE
                    bind(state.data)
                }
                else -> Unit
            }
        }
        viewModel.load(code)
    }

    private fun bind(view: FamilyPatientView) {
        binding.tvName.text = view.patient.fullName
        binding.tvCode.text = view.patient.familyAccessCode.orEmpty()
        val metrics = view.latestMetrics
        binding.tvBpm.text = Formatters.integerOrDash(metrics?.bpm, " BPM")
        binding.tvSteps.text = Formatters.integerOrDash(metrics?.steps, " pasos")
        binding.tvDistance.text = Formatters.decimalOrDash(metrics?.distance, 2, " km")
        binding.tvRadiation.text = Formatters.decimalOrDash(metrics?.currentRadiation, 2, " mSv")
        binding.tvUpdated.text = "Última lectura: ${Formatters.shortDate(metrics?.recordedAt)} · " +
            Formatters.time(metrics?.recordedAt)
    }

    companion object {
        const val EXTRA_CODE = "extra_code"
    }
}

class FamilyDashboardViewModel(
    private val repository: RadixRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState<FamilyPatientView>>(UiState.Idle)
    val state: LiveData<UiState<FamilyPatientView>> = _state

    fun load(code: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val view = repository.getFamilyPatient(code)
                _state.value = UiState.Success(view)
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "")
            }
        }
    }

    class Factory(private val repository: RadixRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FamilyDashboardViewModel(repository) as T
    }
}
