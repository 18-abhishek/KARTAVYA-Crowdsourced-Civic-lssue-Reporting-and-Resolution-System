package com.example.kartavya.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Issue status lifecycle for civic complaints.
 */
enum class IssueStatus {
    REPORTED,
    ACKNOWLEDGED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED;

    /** User-facing label */
    fun displayLabel(): String = when (this) {
        REPORTED -> "Reported"
        ACKNOWLEDGED -> "Acknowledged"
        IN_PROGRESS -> "In Progress"
        RESOLVED -> "Resolved"
        REJECTED -> "Rejected"
    }
}

/**
 * Firestore document model for issues/{issueId}.
 *
 * Every issue is linked to its creator via [userId] (Firebase UID).
 * Security rules enforce that userId == request.auth.uid on creation.
 */
data class CivicIssue(
    @DocumentId
    val issueId: String = "",
    val userId: String = "",
    val reporterName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrls: List<String> = emptyList(),
    val audioUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val status: String = IssueStatus.REPORTED.name,
    val routingTo: String = "NDMC Authority",
    val priority: String = "Moderate (48h target)",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    /** Parse the status string back to enum safely. */
    fun statusEnum(): IssueStatus = try {
        IssueStatus.valueOf(status)
    } catch (_: Exception) {
        IssueStatus.REPORTED
    }
}
