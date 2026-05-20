package com.radix.health.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.radix.health.data.repository.RadixRepository
import com.radix.health.session.SessionManager
import com.radix.health.util.UiState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Estado/efectos del login. La Activity sólo observa estos LiveData y emite
 * eventos via [submit] — no hace nada de red por sí misma.
 */
class LoginViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    sealed class LoginEvent {
        data object NeedsChangePassword : LoginEvent()
        data object EnterApp : LoginEvent()
    }

    private val _state = MutableLiveData<UiState<LoginEvent>>(UiState.Idle)
    val state: LiveData<UiState<LoginEvent>> = _state

    val rememberedEmail: LiveData<String?> = MutableLiveData<String?>().also { live ->
        viewModelScope.launch { live.value = sessionManager.rememberEmailOnce() }
    }

    fun submit(email: String, password: String, remember: Boolean) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            _state.value = UiState.Error("empty")
            return
        }
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                // Limpiamos cualquier token previo para que el AuthInterceptor no
                // mande un Bearer caducado en la petición de login.
                sessionManager.clear()
                val response = repository.login(cleanEmail, cleanPassword)
                sessionManager.saveLogin(
                    token = response.token,
                    userId = response.id,
                    role = response.role,
                    mustChange = response.mustChangePassword
                )
                sessionManager.saveRememberEmail(if (remember) cleanEmail else null)
                _state.value = if (response.mustChangePassword)
                    UiState.Success(LoginEvent.NeedsChangePassword)
                else UiState.Success(LoginEvent.EnterApp)
            } catch (e: HttpException) {
                _state.value = UiState.Error(
                    if (e.code() == 401) "credentials" else "http_${e.code()}"
                )
            } catch (e: IOException) {
                _state.value = UiState.Error("network")
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "credentials")
            }
        }
    }

    class Factory(
        private val repository: RadixRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(repository, sessionManager) as T
    }
}
