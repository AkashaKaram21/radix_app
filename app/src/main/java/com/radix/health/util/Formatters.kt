package com.radix.health.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Formatters {
    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val isoParserMillis = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US
    ).apply { timeZone = TimeZone.getTimeZone("UTC") }

    private val shortDate = SimpleDateFormat("dd MMM", Locale("es", "ES"))
    private val timeFmt = SimpleDateFormat("HH:mm", Locale("es", "ES"))

    fun parseIso(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        return try {
            isoParser.parse(value)
        } catch (_: Throwable) {
            try { isoParserMillis.parse(value) } catch (_: Throwable) { null }
        }
    }

    fun shortDate(value: String?): String =
        parseIso(value)?.let { shortDate.format(it) } ?: "Sin fecha"

    fun time(value: String?): String =
        parseIso(value)?.let { timeFmt.format(it) } ?: "Sin hora"

    fun integerOrDash(value: Int?, suffix: String = ""): String =
        if (value == null) "—" else "$value$suffix"

    fun decimalOrDash(value: Double?, decimals: Int = 1, suffix: String = ""): String =
        if (value == null) "—" else "%.${decimals}f%s".format(Locale.US, value, suffix)

    fun sleepDuration(minutes: Int?): String {
        if (minutes == null || minutes <= 0) return "—"
        val h = minutes / 60
        val m = minutes % 60
        return if (h == 0) "${m}m" else "${h}h ${m}m"
    }
}

/**
 * Cálculo del progreso de aislamiento (espejo de `calculateRemaining` en
 * la app iOS).
 */
data class IsolationProgress(
    val elapsedDays: Int,
    val remainingDays: Int,
    val remainingHours: Int,
    val elapsedFraction: Float,
    val remainingFraction: Float
) {
    companion object {
        fun compute(startIso: String?, isolationDays: Int): IsolationProgress {
            val start = Formatters.parseIso(startIso)?.time ?: return IsolationProgress(
                0, 0, 0, 0f, 0f
            )
            val totalMs = isolationDays * 24L * 60L * 60L * 1000L
            val now = System.currentTimeMillis()
            val end = start + totalMs
            val remainingMs = (end - now).coerceAtLeast(0)
            val elapsedMs = (now - start).coerceAtLeast(0)
            val totalForRatio = if (totalMs == 0L) 1L else totalMs
            return IsolationProgress(
                elapsedDays = (elapsedMs / (24L * 60L * 60L * 1000L)).toInt(),
                remainingDays = (remainingMs / (24L * 60L * 60L * 1000L)).toInt(),
                remainingHours = ((remainingMs % (24L * 60L * 60L * 1000L)) /
                    (60L * 60L * 1000L)).toInt(),
                elapsedFraction = (elapsedMs.toFloat() / totalForRatio).coerceIn(0f, 1f),
                remainingFraction = (remainingMs.toFloat() / totalForRatio).coerceIn(0f, 1f)
            )
        }
    }
}
