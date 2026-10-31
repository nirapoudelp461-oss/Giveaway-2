package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class AuthResult {
    data class Success(val uid: String, val email: String, val token: String, val displayName: String?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class FirebaseAuthService(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("giveaway_auth", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getStoredUid(): String? = prefs.getString("user_uid", null)
    fun getStoredToken(): String? = prefs.getString("auth_token", null)
    fun getStoredEmail(): String? = prefs.getString("user_email", null)
    fun getStoredUsername(): String? = prefs.getString("user_username", null)
    fun isLoggedIn(): Boolean = !getStoredUid().isNullOrEmpty()

    fun saveSession(uid: String, email: String, token: String, username: String) {
        prefs.edit()
            .putString("user_uid", uid)
            .putString("user_email", email)
            .putString("auth_token", token)
            .putString("user_username", username)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    suspend fun signUp(email: String, pass: String, username: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", pass)
                put("returnSecureToken", true)
            }
            val body = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(FirebaseConfig.AUTH_SIGN_UP_URL)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                val jsonObj = JSONObject(respBody)

                if (response.isSuccessful) {
                    val uid = jsonObj.optString("localId")
                    val token = jsonObj.optString("idToken")
                    saveSession(uid, email, token, username)
                    AuthResult.Success(uid, email, token, username)
                } else {
                    val errorObj = jsonObj.optJSONObject("error")
                    val msg = errorObj?.optString("message") ?: "Registration failed (${response.code})"
                    val userFriendlyMsg = when {
                        msg.contains("EMAIL_EXISTS") -> "This email address is already in use."
                        msg.contains("OPERATION_NOT_ALLOWED") -> "Email/password accounts need to be enabled in Firebase."
                        msg.contains("TOO_MANY_ATTEMPTS") -> "Too many attempts. Please try again later."
                        msg.contains("WEAK_PASSWORD") -> "Password should be at least 6 characters."
                        else -> msg
                    }
                    AuthResult.Error(userFriendlyMsg)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Network error during registration")
        }
    }

    suspend fun signIn(email: String, pass: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", pass)
                put("returnSecureToken", true)
            }
            val body = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(FirebaseConfig.AUTH_SIGN_IN_URL)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                val jsonObj = JSONObject(respBody)

                if (response.isSuccessful) {
                    val uid = jsonObj.optString("localId")
                    val token = jsonObj.optString("idToken")
                    val displayName = jsonObj.optString("displayName").ifBlank { email.substringBefore("@") }
                    saveSession(uid, email, token, displayName)
                    AuthResult.Success(uid, email, token, displayName)
                } else {
                    val errorObj = jsonObj.optJSONObject("error")
                    val msg = errorObj?.optString("message") ?: "Login failed (${response.code})"
                    val userFriendlyMsg = when {
                        msg.contains("EMAIL_NOT_FOUND") -> "No account found with this email."
                        msg.contains("INVALID_PASSWORD") -> "Incorrect password. Please try again."
                        msg.contains("USER_DISABLED") -> "This account has been disabled."
                        else -> msg
                    }
                    AuthResult.Error(userFriendlyMsg)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Network error during sign in")
        }
    }

    suspend fun sendPasswordReset(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("requestType", "PASSWORD_RESET")
                put("email", email)
            }
            val body = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(FirebaseConfig.AUTH_RESET_PASSWORD_URL)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Password reset email sent to $email")
                } else {
                    Result.failure(IOException("Failed to send reset email (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loginAsDemoUser(username: String = "GamerChampion"): AuthResult.Success {
        val uid = "user_demo_" + System.currentTimeMillis().toString().takeLast(6)
        val email = "$username.rewards@giveaway.gg".lowercase()
        val token = "mock_token_" + System.currentTimeMillis()
        saveSession(uid, email, token, username)
        return AuthResult.Success(uid, email, token, username)
    }
}
