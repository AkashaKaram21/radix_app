package com.radix.health.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.radix.health.RadixApplication
import com.radix.health.databinding.ActivityOnboardingBinding
import com.radix.health.ui.login.LoginActivity
import kotlinx.coroutines.launch

/**
 * Pantalla previa al login. Permite elegir entre tema claro, oscuro o del
 * sistema antes de iniciar sesión por primera vez. Equivale a la "theme gate"
 * de la versión iOS.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.optionLight.setOnClickListener { selectMode(AppCompatDelegate.MODE_NIGHT_NO) }
        binding.optionDark.setOnClickListener { selectMode(AppCompatDelegate.MODE_NIGHT_YES) }
        binding.optionSystem.setOnClickListener {
            selectMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding.btnConfirm.setOnClickListener { confirm() }
    }

    private fun selectMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        binding.optionLight.isChecked = mode == AppCompatDelegate.MODE_NIGHT_NO
        binding.optionDark.isChecked = mode == AppCompatDelegate.MODE_NIGHT_YES
        binding.optionSystem.isChecked = mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    private fun confirm() {
        lifecycleScope.launch {
            RadixApplication.get().sessionManager.markOnboardingDone()
            startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
            finish()
        }
    }
}
