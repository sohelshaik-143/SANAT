package com.civicguard.app.model

data class User(
    val name: String = "",
    val role: String = "citizen", // "citizen" or "official"
    val email: String = "",
    val department: String = "",
    val designation: String = ""
)

data class Complaint(
    val id: String = "",
    val type: String = "Civic Issue",
    val location: String = "",
    val lat: Double = 12.9716,
    val lng: Double = 77.5946,
    val status: String = "Pending",
    val date: String = "",
    val description: String = "",
    val reporter: String = "",
    val department: String = "General Triage",
    val aiConfidence: Int = 95,
    val imageUrl: String? = null
)

data class Officer(
    val officerId: String = "",
    val name: String = "",
    val designation: String = "",
    val department: String = "",
    val totalResolved: Int = 0,
    val performanceScore: Int = 0
)

data class Report(
    val id: String = "",
    val title: String = "",
    val period: String = "",
    val type: String = "",
    val issues: Int = 0,
    val resolved: Int = 0,
    val pending: Int = 0,
    val summary: String = ""
)

data class AIResult(
    val pass: Boolean = false,
    val confidence: Int = 0,
    val detected: List<DetectedIssue> = emptyList(),
    val topIssue: String = "",
    val reason: String = "",
    val isVideo: Boolean = false
)

data class DetectedIssue(
    val type: String = "",
    val confidence: Int = 0
)

data class CommunityPost(
    val id: String = "",
    val author: String = "",
    val type: String = "",
    val content: String = "",
    val location: String = "",
    val time: String = "",
    val likes: Int = 0,
    val comments: Int = 0
)
