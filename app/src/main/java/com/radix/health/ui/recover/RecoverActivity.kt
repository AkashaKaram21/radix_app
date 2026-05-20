package com.radix.health.ui.recover

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.radix.health.databinding.ActivityRecoverBinding

/**
 * Pantalla stub para recuperar contraseña. El backend aún no expone un
 * endpoint público para esto, así que sólo registramos la solicitud y
 * mostramos confirmación.
 */
class RecoverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
            if (email.isBlank()) {
                binding.inputEmailLayout.error = getString(
                    com.radix.health.R.string.login_error_empty
                )
                return@setOnClickListener
            }
            binding.inputEmailLayout.error = null
            Snackbar.make(
                binding.root,
                "Si el email existe en nuestro sistema te enviaremos instrucciones.",
                Snackbar.LENGTH_LONG
            ).show()
            binding.root.postDelayed({ finish() }, 1500)
        }
    }
}
