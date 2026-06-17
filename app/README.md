# App Android - Sistema de Ventas Hoteleras

Kotlin + Jetpack Compose. Mínimo Android 8.0 (API 26).

## Setup en Android Studio

1. Abrir carpeta `app/` en Android Studio (File → Open)
2. Esperar que Gradle sincronice
3. Reemplazar `AQUI_TU_GOOGLE_MAPS_API_KEY` en `AndroidManifest.xml`
4. Conectar emulador o dispositivo físico
5. Run → Run app

## Conexión al Backend

El emulador de Android usa `10.0.2.2` para conectarse al `localhost` del PC.
El backend debe correr en `http://localhost:3000`.

Si usas celular físico en la misma red WiFi, cambia `BASE_URL` en
`util/Constants.kt` a la IP local de tu PC (ej: `http://192.168.1.100:3000/api/`).

## Pantallas

| Pantalla | Descripción |
|----------|-------------|
| RouteScreen | Selector de ciudad, mapa con hoteles, lista con estado visitado |
| VisitReportScreen | Grabación de voz + notas + envío al backend |
| OpportunitiesScreen | Lista de oportunidades con polling cada 10s |

## API Keys necesarias

- **Google Maps SDK** → Consola Google Cloud → Maps SDK for Android
  → Agregar en `AndroidManifest.xml`

## Permisos solicitados en runtime

- RECORD_AUDIO (antes de grabar)
- ACCESS_FINE_LOCATION (para mapa)

> **Nota:** La pantalla de Login no está implementada en la app para simplificar
> la demo. Agrega login llamando a `POST /api/auth/login` y guardando el JWT
> en SharedPreferences (`Constants.PREFS_TOKEN`). El OkHttp interceptor en
> `NetworkModule` lo adjunta automáticamente a cada request.
