package com.radix.health.data.remote

import com.radix.health.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Inyecta el header `Authorization: Bearer <token>` cuando hay sesión activa.
 *
 * No persistir el token aquí — la única fuente de verdad es [SessionManager].
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    private val publicPaths = listOf(
        "/api/auth/login",
        "/api/auth/recover",
        "/api/auth/register",
        "/api/family/"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath
        val isPublic = publicPaths.any { path.contains(it) }
        val token = if (isPublic) null else runBlocking { sessionManager.tokenOnce() }
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
