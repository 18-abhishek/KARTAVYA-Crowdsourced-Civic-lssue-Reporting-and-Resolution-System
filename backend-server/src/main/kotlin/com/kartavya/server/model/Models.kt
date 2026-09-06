package com.kartavya.server.model

import com.fasterxml.jackson.annotation.JsonInclude

data class UploadResponse(
    val success: Boolean,
    val path: String? = null,
    val error: String? = null
)

data class AiProcessRequest(
    val issueId: String,
    val userId: String,
    val imageUrl: String,
    val audioUrl: String? = null,
    val reporterName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val routingTo: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AiDetail(
    val category: String,
    val summary: String,
    val description: String,
    val priority: String,
    val reason: String,
    val transcript: String = ""
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ImageVerification(
    val approved: Boolean,
    val category: String,
    val reason: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FirestoreResult(
    val written: Boolean = false,
    val issueId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AiProcessResponse(
    val success: Boolean,
    val approved: Boolean,
    val category: String,
    val reason: String,
    val ai: AiDetail,
    val imageVerification: ImageVerification,
    val firestore: FirestoreResult
)

data class HealthResponse(
    val status: String,
    val service: String = "kartavya-backend-server",
    val timestamp: Long = System.currentTimeMillis()
)
