package com.radix.health.ui.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.radix.health.data.repository.RadixRepository
import com.radix.health.session.SessionManager
import com.radix.health.util.UiState
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val repository: RadixRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val state: LiveData<UiState<Unit>> = _state

    fun submit(new: String, confirm: String) {
        if (new.length < 8) { _state.value = UiState.Error("short"); return }
        if (new != confirm) { _state.value = UiState.Error("mismatch"); return }

        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdOnce()
                    ?: throw IllegalStateException("Sin sesión")
                repository.changePassword(userId, new)
                sessionManager.clearMustChangePassword()
                _state.value = UiState.Success(Unit)
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "error")
            }
        }
    }

    class Factory(
        private val repository: RadixRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChangePasswordViewModel(repository, sessionManager) as T
    }
}
