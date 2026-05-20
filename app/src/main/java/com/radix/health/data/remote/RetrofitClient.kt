package com.radix.health.data.remote

import com.radix.health.BuildConfig
import com.radix.health.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Punto único de creación del cliente Retrofit.
 *
 * Permite cambiar la base URL en runtime (útil para apuntar a backend local en
 * desarrollo desde la UI de ajustes).
 */
object RetrofitClient {

    @Volatile private var apiService: ApiService? = null
    @Volatile private var currentBaseUrl: String = BuildConfig.API_BASE_URL

    fun api(sessionManager: SessionManager, baseUrl: String = currentBaseUrl): ApiService {
        val cached = apiService
        if (cached != null && baseUrl == currentBaseUrl) return cached
        synchronized(this) {
            val existing = apiService
            if (existing != null && baseUrl == currentBaseUrl) return existing
            currentBaseUrl = baseUrl
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val service = retrofit.create(ApiService::class.java)
            apiService = service
            return service
        }
    }
}
