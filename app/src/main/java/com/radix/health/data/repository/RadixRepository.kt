package com.radix.health.data.repository

import com.radix.health.data.model.Alert
import com.radix.health.data.model.FamilyPatientView
import com.radix.health.data.model.HealthMetric
import com.radix.health.data.model.LoginRequest
import com.radix.health.data.model.LoginResponse
import com.radix.health.data.model.Message
import com.radix.health.data.model.PasswordUpdate
import com.radix.health.data.model.Patient
import com.radix.health.data.model.RadiationLog
import com.radix.health.data.model.Smartwatch
import com.radix.health.data.model.Treatment
import com.radix.health.data.model.User
import com.radix.health.data.model.WatchMetrics
import com.radix.health.data.remote.ApiService

/**
 * Repositorio único de la app — la UI nunca habla con [ApiService] directamente.
 * Esto encapsula la fuente de datos y permite testear con dobles.
 */
class RadixRepository(private val api: ApiService) {

    // ─── Auth ───
    suspend fun login(email: String, password: String): LoginResponse =
        api.login(LoginRequest(email, password))

    suspend fun changePassword(userId: Long, password: String): User =
        api.updatePassword(userId, PasswordUpdate(password))

    // ─── Paciente / usuario ───
    suspend fun getPatientByUser(userId: Long): Patient =
        api.getPatientByUser(userId)

    suspend fun getUser(userId: Long): User = api.getUser(userId)

    // ─── Datos clínicos ───
    suspend fun getTreatments(patientId: Long): List<Treatment> =
        api.getTreatmentsByPatient(patientId)

    suspend fun getSmartwatches(patientId: Long): List<Smartwatch> =
        api.getSmartwatchesByPatient(patientId)

    suspend fun getLatestWatch(patientId: Long): WatchMetrics? = try {
        api.getLatestWatchByPatient(patientId)
    } catch (_: Throwable) { null }

    suspend fun getHealthMetrics(patientId: Long, days: Int? = 30): List<HealthMetric> =
        api.getHealthMetricsByPatient(patientId, days)

    suspend fun getRadiationLogs(patientId: Long, days: Int? = 30): List<RadiationLog> =
        api.getRadiationLogsByPatient(patientId, days)

    suspend fun getAlerts(patientId: Long): List<Alert> =
        api.getAlertsByPatient(patientId)

    suspend fun resolveAlert(id: Long): Alert = api.resolveAlert(id)

    suspend fun getMessages(patientId: Long): List<Message> =
        api.getMessagesByPatient(patientId)

    // ─── Family ───
    suspend fun getFamilyPatient(code: String): FamilyPatientView =
        api.getFamilyPatient(code.trim().uppercase())
}
