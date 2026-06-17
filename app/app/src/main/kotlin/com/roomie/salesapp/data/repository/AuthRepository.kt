package com.roomie.salesapp.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.roomie.salesapp.data.model.LoginRequest
import com.roomie.salesapp.data.remote.ApiService
import com.roomie.salesapp.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val prefs: SharedPreferences
) {
    fun isLoggedIn(): Boolean = prefs.getString(Constants.PREFS_TOKEN, null) != null

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                prefs.edit {
                    putString(Constants.PREFS_TOKEN, body.token)
                    putString(Constants.PREFS_USER, body.user.name)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login fallido: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
