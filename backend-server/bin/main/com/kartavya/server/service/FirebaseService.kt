package com.kartavya.server.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.auth.FirebaseToken
import com.kartavya.server.model.AiProcessRequest
import com.kartavya.server.model.AiProcessResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class FirebaseService(
    @Value("\${firebase.service-account-json:}") private val serviceAccountJson: String,
    @Value("\${firebase.storage.bucket:}") private val storageBucket: String
) {
    private val logger = LoggerFactory.getLogger(FirebaseService::class.java)
    private val enabled = serviceAccountJson.isNotBlank()

    init {
        if (enabled && FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(
                FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(serviceAccountJson.toByteArray())))
                    .setStorageBucket(storageBucket.takeIf { it.isNotBlank() })
                    .build()
            )
        }
    }

    fun verifyBearerToken(authorization: String?): FirebaseToken? {
        if (authorization.isNullOrBlank()) return null
        require(authorization.startsWith("Bearer ", ignoreCase = true)) { "Invalid Authorization header" }
        check(enabled) { "Firebase Admin credentials are not configured" }
        return FirebaseAuth.getInstance().verifyIdToken(authorization.substringAfter(' ').trim())
    }

    fun saveComplaint(request: AiProcessRequest, response: AiProcessResponse): Boolean {
        if (!enabled) return false
        return try {
            val data = mapOf(
                "issueId" to request.issueId,
                "userId" to request.userId,
                "reporterName" to request.reporterName,
                "latitude" to request.latitude,
                "longitude" to request.longitude,
                "address" to request.address,
                "routingTo" to request.routingTo,
                "imageUrl" to request.imageUrl,
                "audioUrl" to request.audioUrl,
                "approved" to response.approved,
                "category" to response.category,
                "reason" to response.reason,
                "ai" to response.ai,
                "imageVerification" to response.imageVerification,
                "updatedAt" to com.google.cloud.firestore.FieldValue.serverTimestamp(),
                "createdAt" to com.google.cloud.firestore.FieldValue.serverTimestamp(),
                "timestamp" to com.google.cloud.firestore.FieldValue.serverTimestamp()
            )
            FirestoreClient.getFirestore().collection("issues").document(request.issueId).set(data).get()
            true
        } catch (exception: Exception) {
            logger.warn("Could not save issue ${request.issueId} to Firestore: ${exception.message}")
            false
        }
    }
}
