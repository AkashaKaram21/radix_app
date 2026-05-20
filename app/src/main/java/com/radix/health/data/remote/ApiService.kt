package com.radix.health.data.remote

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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints REST consumidos por la app, espejo del backend descrito en AGENTS.md.
 */
interface ApiService {

    // ─── Auth ───
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    // ─── Users ───
    @PUT("api/users/{id}")
    suspend fun updatePassword(@Path("id") id: Long, @Body body: PasswordUpdate): User

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Long): User

    // ─── Patients ───
    @GET("api/patients/profile/{userId}")
    suspend fun getPatientByUser(@Path("userId") userId: Long): Patient

    @GET("api/patients/{id}")
    suspend fun getPatient(@Path("id") id: Long): Patient

    // ─── Treatments ───
    @GET("api/treatments/patient/{patientId}")
    suspend fun getTreatmentsByPatient(@Path("patientId") patientId: Long): List<Treatment>

    // ─── Smartwatches ───
    @GET("api/smartwatches/patient/{patientId}")
    suspend fun getSmartwatchesByPatient(@Path("patientId") patientId: Long): List<Smartwatch>

    // ─── Watch metrics ───
    @GET("api/watch/patient/{patientId}/latest")
    suspend fun getLatestWatchByPatient(@Path("patientId") patientId: Long): WatchMetrics

    // ─── Health metrics ───
    @GET("api/health-metrics/patient/{patientId}")
    suspend fun getHealthMetricsByPatient(
        @Path("patientId") patientId: Long,
        @Query("days") days: Int? = null
    ): List<HealthMetric>

    // ─── Radiation ───
    @GET("api/radiation-logs/patient/{patientId}")
    suspend fun getRadiationLogsByPatient(
        @Path("patientId") patientId: Long,
        @Query("days") days: Int? = null
    ): List<RadiationLog>

    // ─── Alerts ───
    @GET("api/alerts/patient/{patientId}")
    suspend fun getAlertsByPatient(@Path("patientId") patientId: Long): List<Alert>

    @PUT("api/alerts/{id}/resolve")
    suspend fun resolveAlert(@Path("id") id: Long): Alert

    // ─── Messages ───
    @GET("api/messages/patient/{patientId}")
    suspend fun getMessagesByPatient(@Path("patientId") patientId: Long): List<Message>

    // ─── Family Access ───
    @GET("api/family/patient/{code}")
    suspend fun getFamilyPatient(@Path("code") code: String): FamilyPatientView
}
