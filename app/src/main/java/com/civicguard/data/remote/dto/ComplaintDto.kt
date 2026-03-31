package com.civicguard.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ComplaintResponse(
    @Json(name = "id") val id: String,
    @Json(name = "ticketNumber") val ticketNumber: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String,
    @Json(name = "status") val status: String,
    @Json(name = "priority") val priority: String,
    @Json(name = "imageUrl") val imageUrl: String?,
    @Json(name = "resolutionImageUrl") val resolutionImageUrl: String?,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "pincode") val pincode: String,
    @Json(name = "city") val city: String,
    @Json(name = "createdAt") val createdAt: Long,
    @Json(name = "updatedAt") val updatedAt: Long,
    @Json(name = "deadline") val deadline: Long,
    @Json(name = "confidence") val confidence: Double,
    @Json(name = "authentic") val authentic: Boolean
)

@JsonClass(generateAdapter = true)
data class ComplaintRequest(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "pincode") val pincode: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String
)
