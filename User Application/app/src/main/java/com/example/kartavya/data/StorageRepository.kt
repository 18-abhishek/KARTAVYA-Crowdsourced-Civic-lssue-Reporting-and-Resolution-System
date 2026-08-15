package com.example.kartavya.data

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

/**
 * Handles file uploads to Firebase Storage.
 *
 * Storage path structure:
 *   issues/{uid}/{issueId}/{uniqueFileName}
 *
 * Security rules enforce that {uid} matches request.auth.uid,
 * preventing users from writing to another user's directory.
 */
object StorageRepository {

    private const val TAG = "StorageRepository"
    private const val MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024  // 10 MB
    private const val MAX_AUDIO_SIZE_BYTES = 20L * 1024 * 1024  // 20 MB

    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    /**
     * Upload an image to Firebase Storage for a specific issue.
     *
     * @param uid The authenticated user's Firebase UID.
     * @param issueId A unique identifier for the issue (can be pre-generated).
     * @param imageUri The local content URI of the image.
     * @param onProgress Optional progress callback (0.0 to 1.0).
     * @return The download URL string on success.
     */
    suspend fun uploadIssueImage(
        uid: String,
        issueId: String,
        imageUri: Uri,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        return try {
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child("issues/$uid/$issueId/$fileName")

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            val uploadTask = storageRef.putFile(imageUri, metadata)

            // Report progress if callback provided
            onProgress?.let { callback ->
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = taskSnapshot.bytesTransferred.toFloat() /
                            taskSnapshot.totalByteCount.toFloat()
                    callback(progress)
                }
            }

            uploadTask.await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.i(TAG, "Uploaded image: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Image upload failed", e)
            Result.failure(Exception("Failed to upload image. Please check your connection and try again."))
        }
    }

    /**
     * Upload an audio file (voice note) to Firebase Storage.
     *
     * @param uid The authenticated user's Firebase UID.
     * @param issueId A unique identifier for the issue.
     * @param audioFile The local audio file.
     * @return The download URL string on success.
     */
    suspend fun uploadIssueAudio(
        uid: String,
        issueId: String,
        audioFile: File
    ): Result<String> {
        return try {
            val fileName = "audio_${UUID.randomUUID()}.m4a"
            val storageRef = storage.reference
                .child("issues/$uid/$issueId/$fileName")

            val metadata = StorageMetadata.Builder()
                .setContentType("audio/mp4")
                .build()

            val fileUri = Uri.fromFile(audioFile)
            storageRef.putFile(fileUri, metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.i(TAG, "Uploaded audio: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Audio upload failed", e)
            Result.failure(Exception("Failed to upload voice note. Please try again."))
        }
    }

    /**
     * Generate a unique issue ID for use before the Firestore document is created.
     * This allows uploading media to the correct path before creating the issue.
     */
    fun generateIssueId(): String = UUID.randomUUID().toString()
}
