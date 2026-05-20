package com.radix.health.ui.changepassword

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.radix.health.R
import com.radix.health.RadixApplication
import com.radix.health.databinding.ActivityChangePasswordBinding
import com.radix.health.ui.main.MainActivity
import com.radix.health.util.UiState

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    private val viewModel: ChangePasswordViewModel by viewModels {
        val app = RadixApplication.get()
        ChangePasswordViewModel.Factory(app.repository, app.sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.state.observe(this) { render(it) }

        binding.btnSave.setOnClickListener {
            viewModel.submit(
                binding.inputNew.text?.toString().orEmpty(),
                binding.inputConfirm.text?.toString().orEmpty()
            )
        }
    }

    private fun render(state: UiState<Unit>) {
        binding.progress.visibility = if (state is UiState.Loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = state !is UiState.Loading
        when (state) {
            is UiState.Success -> {
                Snackbar.make(
                    binding.root,
                    R.string.change_password_success,
                    Snackbar.LENGTH_SHORT
                ).show()
                binding.root.postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 600)
            }
            is UiState.Error -> {
                val msg = when (state.message) {
                    "short" -> R.string.change_password_error_short
                    "mismatch" -> R.string.change_password_error_mismatch
                    else -> R.string.generic_error
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }
}
