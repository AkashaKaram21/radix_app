package com.radix.health.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.radix.health.data.model.Message
import com.radix.health.data.model.Patient
import com.radix.health.data.model.Treatment
import com.radix.health.data.model.WatchMetrics
import com.radix.health.data.repository.RadixRepository
import com.radix.health.session.SessionManager
import com.radix.health.util.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Carga en paralelo los datos del paciente activo: tratamientos, alertas,
 * última lectura del reloj y mensajes. Si alguna llamada falla, esa parte
 * se trata como ausente — no se sustituyen valores clínicos por defecto.
 */
data class DashboardData(
    val patient: Patient?,
    val activeTreatment: Treatment?,
    val pendingAlerts: Int,
    val latestMetrics: WatchMetrics?,
    val lastMessage: Message?
)

class DashboardViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<UiState<DashboardData>>(UiState.Idle)
    val state: LiveData<UiState<DashboardData>> = _state

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdOnce()
                    ?: throw IllegalStateException("Sin sesión")
                val patient = repository.getPatientByUser(userId)
                coroutineScope {
                    val treatments = async { runCatching { repository.getTreatments(patient.id) }.getOrDefault(emptyList()) }
                    val alerts = async { runCatching { repository.getAlerts(patient.id) }.getOrDefault(emptyList()) }
                    val metrics = async { runCatching { repository.getLatestWatch(patient.id) }.getOrNull() }
                    val messages = async { runCatching { repository.getMessages(patient.id) }.getOrDefault(emptyList()) }

                    val trts = treatments.await()
                    val active = trts.firstOrNull { it.isActive } ?: trts.firstOrNull()
                    val pending = alerts.await().count { !it.isResolved }
                    val msg = messages.await().lastOrNull()
                    _state.postValue(
                        UiState.Success(DashboardData(patient, active, pending, metrics.await(), msg))
                    )
                }
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
            DashboardViewModel(repository, sessionManager) as T
    }
}
