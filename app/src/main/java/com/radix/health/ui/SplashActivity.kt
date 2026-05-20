package com.radix.health.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.radix.health.RadixApplication
import com.radix.health.session.SessionManager
import com.radix.health.ui.changepassword.ChangePasswordActivity
import com.radix.health.ui.login.LoginActivity
import com.radix.health.ui.main.MainActivity
import com.radix.health.ui.onboarding.OnboardingActivity
import kotlinx.coroutines.launch

/**
 * Decide la pantalla inicial según el estado de la sesión:
 *  - onboarding pendiente → OnboardingActivity
 *  - sin token            → LoginActivity
 *  - mustChangePassword   → ChangePasswordActivity
 *  - en caso contrario    → MainActivity
 */
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels {
        SplashViewModel.Factory(RadixApplication.get().sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val next = viewModel.resolveNext()
            startActivity(Intent(this@SplashActivity, next))
            finish()
        }
    }
}

class SplashViewModel(private val session: SessionManager) : ViewModel() {

    suspend fun resolveNext(): Class<*> {
        if (!session.onboardingDoneOnce()) return OnboardingActivity::class.java
        val token = session.tokenOnce()
        if (token.isNullOrBlank()) return LoginActivity::class.java
        if (session.mustChangePasswordOnce()) return ChangePasswordActivity::class.java
        return MainActivity::class.java
    }

    class Factory(private val session: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SplashViewModel(session) as T
    }
}
