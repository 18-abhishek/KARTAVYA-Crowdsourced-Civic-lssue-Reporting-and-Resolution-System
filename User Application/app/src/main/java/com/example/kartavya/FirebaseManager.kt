package com.example.kartavya

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

data class UserProfileData(
    val uid: String = "",
    val name: String = "Citizen",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val civicPoints: Int = 1250,
    val reportsFiled: Int = 0,
    val issuesFixed: Int = 0,
    val rank: String = "Top 5%"
)

data class CivicReportData(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val category: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoUrl: String? = null,
    val audioUrl: String? = null,
    val routingTo: String = "NDMC Authority",
    val priority: String = "Moderate (48h target)",
    val status: String = "Under Review",
    val reporterUid: String = "",
    val reporterName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object FirebaseManager {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val webClientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()

        if (webClientIdRes != 0) {
            val webClientId = context.getString(webClientIdRes)
            if (webClientId.isNotBlank()) {
                gsoBuilder.requestIdToken(webClientId)
            }
        }

        return GoogleSignIn.getClient(context, gsoBuilder.build())
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("FirebaseUser is null after sign in")
            syncUserProfile(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUserProfile(user: FirebaseUser): UserProfileData {
        val docRef = firestore.collection("users").document(user.uid)
        val snapshot = docRef.get().await()

        val profile = if (snapshot.exists()) {
            val existing = snapshot.toObject(UserProfileData::class.java) ?: UserProfileData(uid = user.uid)
            val updated = existing.copy(
                name = user.displayName ?: existing.name,
                email = user.email ?: existing.email,
                photoUrl = user.photoUrl?.toString() ?: existing.photoUrl,
                lastLoginAt = System.currentTimeMillis()
            )
            docRef.update(
                mapOf(
                    "name" to updated.name,
                    "email" to updated.email,
                    "photoUrl" to updated.photoUrl,
                    "lastLoginAt" to updated.lastLoginAt
                )
            ).await()
            updated
        } else {
            val newProfile = UserProfileData(
                uid = user.uid,
                name = user.displayName ?: "Citizen",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis(),
                civicPoints = 1250,
                reportsFiled = 0,
                issuesFixed = 0
            )
            docRef.set(newProfile).await()
            newProfile
        }
        return profile
    }

    fun observeUserProfile(uid: String, onUpdate: (UserProfileData?) -> Unit): ListenerRegistration {
        return firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    onUpdate(snapshot.toObject(UserProfileData::class.java))
                } else {
                    onUpdate(null)
                }
            }
    }

    suspend fun uploadPhoto(userId: String, imageUri: Uri): String {
        val imageId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("users/$userId/images/$imageId.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        storageRef.putFile(imageUri, metadata).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun uploadAudio(userId: String, audioFile: File): String {
        val audioId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("users/$userId/audio/$audioId.m4a")
        val metadata = StorageMetadata.Builder()
            .setContentType("audio/mp4")
            .build()

        val fileUri = Uri.fromFile(audioFile)
        storageRef.putFile(fileUri, metadata).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun submitReport(report: CivicReportData): Result<String> {
        return try {
            val docRef = firestore.collection("issues").document(report.id)
            docRef.set(report).await()

            // Increment reporter's points and report count in Firestore
            if (report.reporterUid.isNotBlank()) {
                firestore.collection("users").document(report.reporterUid).update(
                    mapOf(
                        "reportsFiled" to FieldValue.increment(1),
                        "civicPoints" to FieldValue.increment(50)
                    )
                ).await()
            }
            Result.success(report.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRecentIssues(onUpdate: (List<CivicReportData>) -> Unit): ListenerRegistration {
        return firestore.collection("issues")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { it.toObject(CivicReportData::class.java) }
                    onUpdate(list)
                } else {
                    onUpdate(emptyList())
                }
            }
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        auth.signOut()
        try {
            val gsc = getGoogleSignInClient(context)
            gsc.signOut().addOnCompleteListener {
                onComplete()
            }
        } catch (e: Exception) {
            onComplete()
        }
    }
}
