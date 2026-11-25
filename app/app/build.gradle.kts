import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// URLs del backend por variante (sin tocar codigo):
//  - debug: backend local. Por defecto 10.0.2.2 (emulador -> PC). Para celular fisico,
//           pon en app/local.properties:  api.url.dev=http://<IP-de-tu-PC>:3000/api/
//  - demo/release: backend en Render (API_URL_DEMO en gradle.properties).
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val devApiUrl: String = localProps.getProperty("api.url.dev") ?: "http://10.0.2.2:3000/api/"
val demoApiUrl: String = localProps.getProperty("api.url.demo")
    ?: (project.findProperty("API_URL_DEMO") as String?)
    ?: error("Falta API_URL_DEMO en gradle.properties")

android {
    namespace = "com.roomie.salesapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.roomie.salesapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$devApiUrl\"")
        }
        // Variante para la demo: apunta a Render y se instala directo desde Android Studio
        // (firmada con la llave debug). Elegir en Build > Select Build Variant > demo.
        create("demo") {
            initWith(getByName("debug"))
            buildConfigField("String", "BASE_URL", "\"$demoApiUrl\"")
            matchingFallbacks += listOf("debug")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"$demoApiUrl\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.android)

    // WebView + AssetLoader (sirve assets locales bajo https:// virtual para Leaflet/OSM)
    implementation("androidx.webkit:webkit:1.11.0")

    // JSON
    implementation(libs.gson)

    debugImplementation(libs.androidx.ui.tooling)
}
