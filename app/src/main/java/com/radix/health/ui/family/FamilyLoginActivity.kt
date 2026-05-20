package com.radix.health.ui.family

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.radix.health.RadixApplication
import com.radix.health.data.repository.RadixRepository
import com.radix.health.databinding.ActivityFamilyLoginBinding
import com.radix.health.util.UiState
import kotlinx.coroutines.launch

class FamilyLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFamilyLoginBinding

    private val viewModel: FamilyLoginViewModel by viewModels {
        FamilyLoginViewModel.Factory(RadixApplication.get().repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFamilyLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.state.observe(this) { state ->
            binding.progress.visibility =
                if (state is UiState.Loading) View.VISIBLE else View.GONE
            binding.btnOpen.isEnabled = state !is UiState.Loading
            when (state) {
                is UiState.Error -> Snackbar.make(
                    binding.root,
                    state.message.ifBlank { getString(com.radix.health.R.string.generic_error) },
                    Snackbar.LENGTH_LONG
                ).show()
                is UiState.Success -> {
                    val intent = Intent(this, FamilyDashboardActivity::class.java)
                    intent.putExtra(FamilyDashboardActivity.EXTRA_CODE, state.data)
                    startActivity(intent)
                }
                else -> Unit
            }
        }

        binding.btnOpen.setOnClickListener {
            val code = binding.inputCode.text?.toString().orEmpty()
            if (code.isBlank()) {
                binding.inputCodeLayout.error =
                    getString(com.radix.health.R.string.login_error_empty)
            } else {
                binding.inputCodeLayout.error = null
                viewModel.lookup(code)
            }
        }
    }
}

class FamilyLoginViewModel(
    private val repository: RadixRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState<String>>(UiState.Idle)
    val state: LiveData<UiState<String>> = _state

    fun lookup(code: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val view = repository.getFamilyPatient(code)
                _state.value = UiState.Success(view.patient.familyAccessCode ?: code)
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "")
            }
        }
    }

    class Factory(private val repository: RadixRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FamilyLoginViewModel(repository) as T
    }
}
