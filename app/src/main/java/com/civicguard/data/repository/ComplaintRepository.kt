package com.civicguard.data.repository

import com.civicguard.data.local.ComplaintDao
import com.civicguard.data.local.entity.ComplaintEntity
import com.civicguard.data.remote.ComplaintApi
import com.civicguard.data.remote.dto.ComplaintResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepository @Inject constructor(
    private val api: ComplaintApi,
    private val dao: ComplaintDao
) {

    val complaints: Flow<List<ComplaintResponse>> = dao.getAllComplaints().map { entities ->
        entities.map { it.toDto() }
    }

    suspend fun refreshComplaints() {
        try {
            val response = api.getMyComplaints()
            if (response.isSuccessful && response.body() != null) {
                // Assuming backend returns a Map with "content" as List of complaint objects
                val rawData = response.body()!!
                val content = rawData["content"] as? List<Map<String, Any>>
                // Map raw content to Entity (simplified for now)
                // In production, we'd use Moshi to parse the inner list properly
                // For now, assume it's directly mapped
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ComplaintEntity.toDto() = ComplaintResponse(
        id = id,
        ticketNumber = ticketNumber,
        title = title,
        description = description,
        category = category,
        status = status,
        priority = priority,
        imageUrl = imageUrl,
        resolutionImageUrl = resolutionImageUrl,
        latitude = latitude,
        longitude = longitude,
        pincode = pincode,
        city = city,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deadline = deadline,
        confidence = confidence,
        authentic = isAuthentic
    )
    
    // Reverse mapping for caching
}
