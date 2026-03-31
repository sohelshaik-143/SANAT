package com.civicguard.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "name") val name: String,
    @Json(name = "role") val role: String,
    @Json(name = "language") val language: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "designation") val designation: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class CitizenRegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String, // +91XXXXXXXXXX
    @Json(name = "password") val password: String,
    @Json(name = "pincode") val pincode: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "state") val state: String? = null,
    @Json(name = "language") val language: String? = "en"
)
