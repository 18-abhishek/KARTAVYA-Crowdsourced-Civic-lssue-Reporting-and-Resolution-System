package com.example.kartavya.data

import android.util.Log
import com.example.kartavya.model.UserProfile
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Manages user profile documents in Firestore at users/{uid}.
 *
 * Uses Firebase UID as the document ID to guarantee one-to-one mapping.
 * Profile creation is idempotent — calling getOrCreateProfile multiple
 * times for the same UID will not create duplicates.
 */
object UserRepository {

    private const val TAG = "UserRepository"
    private const val COLLECTION = "users"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Create user profile if it doesn't exist, or update login metadata
     * if it does. Uses server timestamps.
     *
     * @return The current [UserProfile] from Firestore.
     */
    suspend fun getOrCreateProfile(user: FirebaseUser): UserProfile {
        val docRef = db.collection(COLLECTION).document(user.uid)
        val snapshot = docRef.get().await()

        return if (snapshot.exists()) {
            // Update login metadata only
            docRef.update(
                mapOf(
                    "name" to (user.displayName ?: "Citizen"),
                    "email" to (user.email ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "lastLoginAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Re-fetch to get server-resolved timestamps
            val updated = docRef.get().await()
            updated.toObject(UserProfile::class.java) ?: UserProfile(uid = user.uid)
        } else {
            // First-time profile creation
            val newProfile = hashMapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: "Citizen"),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "createdAt" to FieldValue.serverTimestamp(),
                "lastLoginAt" to FieldValue.serverTimestamp(),
                "civicPoints" to 0,
                "reportsFiled" to 0,
                "issuesFixed" to 0,
                "rank" to "Newcomer"
            )
            docRef.set(newProfile).await()
            Log.i(TAG, "Created new user profile for ${user.uid}")

            val created = docRef.get().await()
            created.toObject(UserProfile::class.java) ?: UserProfile(uid = user.uid)
        }
    }

    /**
     * Listen to real-time changes on the user's profile document.
     *
     * @return [ListenerRegistration] that the caller must remove on dispose.
     */
    fun observeProfile(uid: String, onUpdate: (UserProfile?) -> Unit): ListenerRegistration {
        return db.collection(COLLECTION).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Profile listen error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    onUpdate(snapshot.toObject(UserProfile::class.java))
                } else {
                    onUpdate(null)
                }
            }
    }

    /**
     * Increment civic points and report count after a successful submission.
     */
    suspend fun incrementOnReport(uid: String, pointsEarned: Int = 50) {
        try {
            db.collection(COLLECTION).document(uid).update(
                mapOf(
                    "reportsFiled" to FieldValue.increment(1),
                    "civicPoints" to FieldValue.increment(pointsEarned.toLong())
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to increment report stats", e)
        }
    }
}
