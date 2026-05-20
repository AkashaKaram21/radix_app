package com.radix.health.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.radix.health.R
import com.radix.health.RadixApplication
import com.radix.health.databinding.ActivityLoginBinding
import com.radix.health.ui.changepassword.ChangePasswordActivity
import com.radix.health.ui.family.FamilyLoginActivity
import com.radix.health.ui.main.MainActivity
import com.radix.health.ui.recover.RecoverActivity
import com.radix.health.util.UiState

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        val app = RadixApplication.get()
        LoginViewModel.Factory(app.repository, app.sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.rememberedEmail.observe(this) { email ->
            if (!email.isNullOrBlank()) {
                binding.inputEmail.setText(email)
                binding.checkRemember.isChecked = true
            }
        }

        viewModel.state.observe(this) { state -> render(state) }

        binding.btnLogin.setOnClickListener {
            viewModel.submit(
                email = binding.inputEmail.text?.toString().orEmpty(),
                password = binding.inputPassword.text?.toString().orEmpty(),
                remember = binding.checkRemember.isChecked
            )
        }

        binding.btnFamily.setOnClickListener {
            startActivity(Intent(this, FamilyLoginActivity::class.java))
        }

        binding.tvForgot.setOnClickListener {
            startActivity(Intent(this, RecoverActivity::class.java))
        }
    }

    private fun render(state: UiState<LoginViewModel.LoginEvent>) {
        binding.progress.visibility = if (state is UiState.Loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = state !is UiState.Loading

        when (state) {
            is UiState.Error -> {
                val message = when (state.message) {
                    "empty" -> getString(R.string.login_error_empty)
                    "credentials" -> getString(R.string.login_error_credentials)
                    in setOf("network", "fetch") -> getString(R.string.login_error_network)
                    else -> state.message
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            is UiState.Success -> {
                val next = when (state.data) {
                    LoginViewModel.LoginEvent.NeedsChangePassword ->
                        ChangePasswordActivity::class.java
                    LoginViewModel.LoginEvent.EnterApp ->
                        MainActivity::class.java
                }
                startActivity(Intent(this, next))
                finish()
            }
            else -> Unit
        }
    }
}
