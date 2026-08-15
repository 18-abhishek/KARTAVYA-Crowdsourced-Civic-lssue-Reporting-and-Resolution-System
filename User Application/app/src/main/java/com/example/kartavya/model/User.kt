package com.example.kartavya.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore document model for users/{uid}.
 *
 * The document ID is the Firebase Authentication UID, guaranteeing
 * one-to-one mapping between Firebase accounts and user records.
 *
 * Note: Firebase UID uniqueness does NOT guarantee one physical human
 * cannot create multiple Google accounts.
 */
data class UserProfile(
    @DocumentId
    val uid: String = "",
    val name: String = "Citizen",
    val email: String = "",
    val photoUrl: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val lastLoginAt: Timestamp? = null,
    val civicPoints: Int = 0,
    val reportsFiled: Int = 0,
    val issuesFixed: Int = 0,
    val rank: String = "Newcomer"
)
