package com.civicguard.data.repository

import com.civicguard.data.remote.AuthApi
import com.civicguard.data.remote.dto.AuthResponse
import com.civicguard.data.remote.dto.CitizenRegisterRequest
import com.civicguard.data.remote.dto.LoginRequest
import com.civicguard.util.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) {

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveAuthToken(body.token)
                sessionManager.saveUserRole(body.role)
                sessionManager.saveUserDetails(body.userId, body.name)
                Result.success(body)
            } else {
                Result.failure(Exception("Login semi-failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: CitizenRegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveAuthToken(body.token)
                sessionManager.saveUserRole(body.role)
                sessionManager.saveUserDetails(body.userId, body.name)
                Result.success(body)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean = sessionManager.fetchAuthToken() != null
}
