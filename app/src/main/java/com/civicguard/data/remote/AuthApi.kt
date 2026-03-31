package com.civicguard.data.remote

import com.civicguard.data.remote.dto.AuthResponse
import com.civicguard.data.remote.dto.CitizenRegisterRequest
import com.civicguard.data.remote.dto.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: CitizenRegisterRequest): Response<AuthResponse>
}
