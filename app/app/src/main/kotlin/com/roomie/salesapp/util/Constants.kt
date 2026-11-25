package com.roomie.salesapp.util

import com.roomie.salesapp.BuildConfig

object Constants {
    // Se define por variante en app/build.gradle.kts (debug = local, demo/release = Render)
    val BASE_URL: String = BuildConfig.BASE_URL
    const val PREFS_TOKEN = "auth_token"
    const val PREFS_USER = "auth_user"
    const val MAX_RECORDING_SECONDS = 120
}
