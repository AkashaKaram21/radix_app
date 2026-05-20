# Radix App (Android)

Versión nativa Android en Kotlin de la app `radix-app-ios` (Expo / React
Native). Sirve para que pacientes en tratamiento de medicina nuclear puedan
consultar el estado de su tratamiento, métricas clínicas, alertas, y para
que sus familiares accedan a información limitada mediante un código.

El proyecto está pensado para abrirse directamente en **Android Studio
Hedgehog/Iguana o superior**.

## Estructura

```
radix-app-android
├── app
│   ├── build.gradle.kts
│   └── src/main
│       ├── AndroidManifest.xml
│       ├── java/com/radix/health
│       │   ├── RadixApplication.kt
│       │   ├── data
│       │   │   ├── model      → POJOs de la API (Moshi)
│       │   │   ├── remote     → ApiService Retrofit + interceptor
│       │   │   └── repository → RadixRepository (única fuente de datos)
│       │   ├── session        → SessionManager (DataStore)
│       │   ├── ui
│       │   │   ├── SplashActivity
│       │   │   ├── onboarding → selector de tema previo al login
│       │   │   ├── login, recover, changepassword
│       │   │   ├── family     → acceso limitado por código
│       │   │   ├── main       → MainActivity con BottomNavigation
│       │   │   ├── dashboard  → Fragment principal
│       │   │   ├── treatment, health, alerts, settings → Fragments
│       │   └── util           → Formatters, IsolationProgress, UiState
│       └── res
│           ├── layout
│           ├── menu, navigation
│           ├── drawable, mipmap-anydpi-v26
│           └── values, values-night
├── build.gradle.kts
└── settings.gradle.kts
```

## Stack técnico (sigue los apuntes)

Apuntes: <https://davidfs-itic.github.io/davidfs-itic/Android/>

| Tema del curso        | Implementación                                       |
|-----------------------|------------------------------------------------------|
| Kotlin                | Todo el código fuente está en Kotlin                 |
| Layouts XML           | Sin Jetpack Compose; todas las vistas son XML        |
| Arquitectura MVVM     | `ViewModel` + `LiveData` por pantalla                |
| Capa de datos         | `RadixRepository` encapsula los accesos a Retrofit   |
| Coroutines            | Llamadas suspendidas + `viewModelScope`              |
| DataStore Preferences | `SessionManager` persiste token, userId, tema        |
| Retrofit              | `ApiService` + `RetrofitClient` con `MoshiConverter` |
| Glide                 | Listo para cargar imágenes (referenciado en gradle)  |
| MPAndroidChart        | Gráfico de tendencias en `HealthFragment`            |
| Tema Material         | `Theme.RadixApp` con paletas light/dark (DESIGN.md)  |
| Observer pattern      | `LiveData.observe()` y `Flow` desde DataStore        |

## Configurar el backend

Por defecto el cliente apunta al backend de producción `https://api.raddix.pro/v1/`.
Para desarrollo local cambia el campo `API_BASE_URL` en `app/build.gradle.kts`
o ejecuta el emulador con backend en `localhost:8080`. La build de debug ya
usa `http://10.0.2.2:8080/v2/` (alias del emulador hacia el host).

Para permitir tráfico HTTP en debug, ya se ha añadido
`android:usesCleartextTraffic="true"` en el manifest.

## Endpoints consumidos

Idénticos a la versión iOS:

- `POST /api/auth/login`
- `PUT  /api/users/{id}` (cambio de contraseña)
- `GET  /api/patients/profile/{userId}`
- `GET  /api/treatments/patient/{patientId}`
- `GET  /api/alerts/patient/{patientId}`
- `PUT  /api/alerts/{id}/resolve`
- `GET  /api/health-metrics/patient/{patientId}?days=30`
- `GET  /api/radiation-logs/patient/{patientId}?days=30`
- `GET  /api/watch/patient/{patientId}/latest`
- `GET  /api/messages/patient/{patientId}`
- `GET  /api/family/patient/{code}` (acceso familiar, sin token)

## Flujo de pantallas

```
SplashActivity
  ├─ onboarding pendiente → OnboardingActivity → LoginActivity
  ├─ sin token            → LoginActivity
  ├─ mustChangePassword   → ChangePasswordActivity
  └─ por defecto          → MainActivity (bottom nav)
                              ├─ DashboardFragment
                              ├─ TreatmentFragment
                              ├─ HealthFragment   (MPAndroidChart)
                              ├─ AlertsFragment   (RecyclerView)
                              └─ SettingsFragment
LoginActivity → FamilyLoginActivity → FamilyDashboardActivity
```

## Cómo abrirlo en Android Studio

1. Abre Android Studio → **Open** → selecciona la carpeta `radix-app-android`.
2. Espera a que Gradle se sincronice (descargará Retrofit, Moshi, Material,
   MPAndroidChart…).
3. Crea un emulador con API ≥ 24.
4. Run ▶ sobre el módulo `app`.

> Si quieres que apunte a un backend local, ajusta la URL en `build.gradle.kts`
> y mira que el backend acepte el origen del emulador (`10.0.2.2`).

## Reglas de diseño (sincronizadas con DESIGN.md de iOS)

- Paleta **Soft UI** en claro, **Neon Tech** en oscuro.
- Cards 24dp, inputs 12dp, botones primarios píldora.
- Texto principal con `Inter`-like (default del sistema), titulares en bold.
- Sin valores clínicos inventados: cuando no hay dato se muestra `—` y no
  un cero.
- Rojo reservado para riesgo real (alertas críticas, logout).

## Pendiente / próximos pasos

- Wizard de cambio de contraseña con validación adicional según
  política del backend.
- Sync en background de Health Connect (equivalente Android de HealthKit).
- Vista detallada del reloj (último estado, batería, conectividad).
- Localización: actualmente todos los textos están en español.
