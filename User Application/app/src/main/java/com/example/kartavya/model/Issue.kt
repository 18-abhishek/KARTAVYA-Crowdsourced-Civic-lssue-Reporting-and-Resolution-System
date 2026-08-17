package com.example.kartavya.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
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
    val priority: String = "Moderate",
    val upvotes: Int = 0,
    val upvotedBy: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    /** Parse the status string back to enum safely. */
    fun statusEnum(): IssueStatus {
        val s = status.trim().uppercase().replace(" ", "_")
        return when {
            s.contains("RESOLV") -> IssueStatus.RESOLVED
            s.contains("REJECT") -> IssueStatus.REJECTED
            s.contains("PROGRESS") || s.contains("ASSIGN") -> IssueStatus.IN_PROGRESS
            s.contains("ACKNOWL") || s.contains("VERIF") -> IssueStatus.ACKNOWLEDGED
            else -> try { IssueStatus.valueOf(s) } catch (_: Exception) { IssueStatus.REPORTED }
        }
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): CivicIssue {
            val issueId = doc.id
            val userId = doc.getString("userId") ?: doc.getString("uid") ?: ""
            val reporterName = doc.getString("reporterName") ?: doc.getString("reporter") ?: "Citizen"
            val title = doc.getString("title") ?: "Civic Issue"
            
            val description = doc.getString("description")
                ?: doc.getString("summary")
                ?: ""

            val category = doc.getString("category") ?: "General"

            val imageUrlStr = doc.getString("imageUrl")
            val imageUrlsList = (doc.get("imageUrls") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val finalImageUrls = if (imageUrlsList.isNotEmpty()) {
                imageUrlsList
            } else if (!imageUrlStr.isNullOrBlank()) {
                listOf(imageUrlStr)
            } else {
                emptyList()
            }

            val audioUrl = doc.getString("audioUrl")
            val latitude = doc.getDouble("latitude")
            val longitude = doc.getDouble("longitude")
            val address = doc.getString("address")
                ?: doc.getString("location")
                ?: ""

            val rawStatus = doc.getString("status") ?: IssueStatus.REPORTED.name
            val routingTo = doc.getString("routingTo")
                ?: doc.getString("department")
                ?: "Municipal Authority"
            val priority = doc.getString("priority") ?: "Moderate"

            val upvotes = doc.getLong("upvoteCount")?.toInt()
                ?: doc.getLong("upvotes")?.toInt()
                ?: 0
                
            val upvotedBy = (doc.get("upvotedBy") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

            val createdAt = doc.getTimestamp("createdAt")
                ?: doc.getTimestamp("timestamp")
            val updatedAt = doc.getTimestamp("updatedAt")

            return CivicIssue(
                issueId = issueId,
                userId = userId,
                reporterName = reporterName,
                title = title,
                description = description,
                category = category,
                imageUrls = finalImageUrls,
                audioUrl = audioUrl,
                latitude = latitude,
                longitude = longitude,
                address = address,
                status = rawStatus,
                routingTo = routingTo,
                priority = priority,
                upvotes = upvotes,
                upvotedBy = upvotedBy,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}
