package com.radix.health

import android.app.Application
import com.radix.health.data.remote.RetrofitClient
import com.radix.health.data.repository.RadixRepository
import com.radix.health.session.SessionManager

/**
 * Inyector simple — evita una dependencia adicional (Hilt) mientras
 * mantiene una única instancia compartida de sesión, API y repositorio.
 */
class RadixApplication : Application() {

    lateinit var sessionManager: SessionManager
        private set

    val repository: RadixRepository by lazy {
        RadixRepository(RetrofitClient.api(sessionManager))
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
        instance = this
    }

    companion object {
        @Volatile private var instance: RadixApplication? = null
        fun get(): RadixApplication = checkNotNull(instance) {
            "RadixApplication aún no se ha inicializado"
        }
    }
}
