package com.radix.health.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelos del dominio (espejo de las entidades JPA del backend).
 * Toda comunicación con la API se realiza usando estas clases.
 */

@JsonClass(generateAdapter = true)
data class User(
    val id: Long,
    val firstName: String,
    val lastName: String = "",
    val email: String,
    val role: String,
    val phone: String? = null,
    val licenseNumber: String? = null,
    val specialty: String? = null,
    val mustChangePassword: Boolean? = null,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Patient(
    val id: Long,
    val fullName: String,
    val phone: String? = null,
    val address: String? = null,
    val isActive: Boolean = true,
    val familyAccessCode: String? = null,
    val fkUserId: Long? = null,
    val fkDoctorId: Long? = null,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Treatment(
    val id: Long,
    val patientId: Long? = null,
    val patientName: String? = null,
    val doctorId: Long? = null,
    val doctorName: String? = null,
    val isotopeId: Long? = null,
    val isotopeName: String? = null,
    val room: Int,
    val initialDose: Double,
    val safetyThreshold: Double,
    val isolationDays: Int,
    val startDate: String,
    val endDate: String? = null,
    val isActive: Boolean = false,
    val currentRadiation: Double? = null
)

@JsonClass(generateAdapter = true)
data class Smartwatch(
    val id: Long,
    val imei: String,
    val macAddress: String,
    val model: String,
    val isActive: Boolean,
    val patientId: Long,
    val patientName: String? = null
)

@JsonClass(generateAdapter = true)
data class WatchMetrics(
    val id: Long,
    val patientId: Long,
    val imei: String,
    val bpm: Int? = null,
    val steps: Int? = null,
    val distance: Double? = null,
    val currentRadiation: Double? = null,
    val recordedAt: String,
    val sleepStart: String? = null,
    val sleepEnd: String? = null,
    val sleepQualityMinutes: Int? = null,
    val batteryPercent: Int? = null,
    val isCharging: Boolean? = null,
    val spo2: Int? = null
)

@JsonClass(generateAdapter = true)
data class HealthMetric(
    val id: Long,
    val patientId: Long,
    val treatmentId: Long? = null,
    val bpm: Int? = null,
    val steps: Int? = null,
    val distance: Double? = null,
    val currentRadiation: Double? = null,
    val recordedAt: String
)

@JsonClass(generateAdapter = true)
data class RadiationLog(
    val id: Long,
    val patientId: Long,
    val treatmentId: Long? = null,
    val radiationLevel: Double,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class Alert(
    val id: Long,
    val patientId: Long,
    val patientName: String? = null,
    val treatmentId: Long? = null,
    val alertType: String,
    val message: String,
    val isResolved: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class Message(
    val id: Long,
    val patientId: Long,
    val messageText: String,
    val isRead: Boolean,
    val sentAt: String
)

@JsonClass(generateAdapter = true)
data class Settings(
    val id: Long,
    val patientId: Long,
    val unitPreference: String,
    val theme: String,
    val notificationsEnabled: Boolean,
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class Isotope(
    val id: Long,
    val name: String,
    val symbol: String,
    val type: String,
    val halfLife: Double,
    val halfLifeUnit: String
)

// ─── Auth ───

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val id: Long,
    val firstName: String?,
    val role: String?,
    @Json(name = "mustChangePassword") val mustChangePassword: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PasswordUpdate(val password: String)

// ─── Family ───

@JsonClass(generateAdapter = true)
data class FamilyPatientView(
    val patient: Patient,
    val latestMetrics: WatchMetrics? = null
)
