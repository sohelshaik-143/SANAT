package com.civicguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val priority: String,
    val imageUrl: String?,
    val resolutionImageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val pincode: String,
    val city: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deadline: Long,
    val confidence: Double,
    val isAuthentic: Boolean
)
