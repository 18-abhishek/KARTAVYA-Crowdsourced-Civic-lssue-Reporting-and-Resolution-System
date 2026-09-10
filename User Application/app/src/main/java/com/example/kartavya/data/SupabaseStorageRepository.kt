package com.example.kartavya.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID
import com.example.kartavya.config.AppConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

/**
 * Result data class for AI Complaint Processing endpoint: POST /ai/process-complaint
 */
data class AiProcessResult(
    val success: Boolean,
    val approved: Boolean,
    val category: String = "",
    val summary: String = "",
    val description: String = "",
    val priority: String = "",
    val reason: String = "",
    val transcript: String = "",
    val firestoreWritten: Boolean = false,
    val issueId: String = ""
)

/**
 * Manages network communications with the Node.js backend server.
 *
 * Change [BACKEND_BASE_URL] in one place when switching between local emulator (10.0.2.2:8080),
 * local LAN IP, or Cloudflare Tunnel public URL.
 */
object SupabaseStorageRepository {

    private const val TAG = "KartavyaBackend"

    // Single source of truth for backend Base URL.
    // For Android Emulator -> http://10.0.2.2:8080
    // For Physical Phone / Cloudflare Tunnel -> https://your-tunnel-name.trycloudflare.com
    val BACKEND_BASE_URL: String get() = AppConfig.BACKEND_BASE_URL

    private const val IMAGE_UPLOAD_PATH = "/upload/image"
    private const val AUDIO_UPLOAD_PATH = "/upload/audio"
    private const val AI_PROCESS_PATH = "/ai/process-complaint"

    fun generateIssueId(): String = UUID.randomUUID().toString()

    private suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser ?: return@withContext null
        try {
            Tasks.await(user.getIdToken(false))?.token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch Firebase Auth token: ${e.message}")
            null
        }
    }

    /**
     * Pings the /health endpoint to wake up the Render free-plan server.
     * Retries up to [maxAttempts] times with [delayMs] between attempts.
     * Call this before any AI processing to ensure the server is awake.
     */
    suspend fun warmUpServer(maxAttempts: Int = 6, delayMs: Long = 5000L): Boolean =
        withContext(Dispatchers.IO) {
            val healthUrl = "$BACKEND_BASE_URL/health"
            Log.d(TAG, "WARM-UP: Pinging $healthUrl (up to $maxAttempts attempts)")
            repeat(maxAttempts) { attempt ->
                try {
                    val conn = (URL(healthUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10000
                        readTimeout = 10000
                    }
                    val code = conn.responseCode
                    conn.disconnect()
                    if (code in 200..299) {
                        Log.d(TAG, "WARM-UP: Server is awake after ${attempt + 1} attempt(s)")
                        return@withContext true
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "WARM-UP attempt ${attempt + 1} failed: ${e.message} — retrying in ${delayMs}ms")
                }
                if (attempt < maxAttempts - 1) delay(delayMs)
            }
            Log.w(TAG, "WARM-UP: Server did not respond after $maxAttempts attempts")
            false
        }

    /**
     * Uploads an issue photo to Firebase Storage.
     *
     * @return Result containing the download URL.
     */
    suspend fun uploadIssueImage(
        uid: String,
        issueId: String,
        imageUri: Uri,
        context: Context,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "IMAGE UPLOAD START issueId=$issueId")
        try {
            onProgress?.invoke(0.1f)
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("images/issue_${issueId}_image.jpg")
            
            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
                onProgress?.invoke((progress / 100.0).toFloat())
            }
            
            Tasks.await(uploadTask)
            val downloadUri = Tasks.await(imageRef.downloadUrl)
            
            onProgress?.invoke(1.0f)
            Log.d(TAG, "IMAGE UPLOAD SUCCESS url: $downloadUri")
            Result.success(downloadUri.toString())
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Image upload error"
            Log.e(TAG, "IMAGE UPLOAD FAILURE: $errorMsg", e)
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Uploads a voice recording to Firebase Storage.
     *
     * @return Result containing the download URL.
     */
    suspend fun uploadIssueAudio(
        uid: String,
        issueId: String,
        audioFile: File
    ): Result<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "AUDIO UPLOAD START issueId=$issueId path=${audioFile.absolutePath}")
        try {
            if (!audioFile.exists()) {
                throw IllegalStateException("Audio file does not exist at ${audioFile.absolutePath}")
            }
            val storageRef = Firebase.storage.reference
            val extension = audioFile.extension.ifBlank { "m4a" }
            val audioRef = storageRef.child("audio/issue_${issueId}_voice.$extension")
            
            val uri = Uri.fromFile(audioFile)
            val uploadTask = audioRef.putFile(uri)
            
            Tasks.await(uploadTask)
            val downloadUri = Tasks.await(audioRef.downloadUrl)
            
            Log.d(TAG, "AUDIO UPLOAD SUCCESS url: $downloadUri")
            Result.success(downloadUri.toString())
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Audio upload error"
            Log.e(TAG, "AUDIO UPLOAD FAILURE: $errorMsg", e)
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Calls the Node.js AI endpoint: POST /ai/process-complaint
     *
     * Transmits relative image/audio paths and metadata. Gemini first verifies the image.
     * If approved, Sarvam transcribes audio, and Gemini updates Firestore directly.
     */
    suspend fun processComplaintWithAi(
        issueId: String,
        userId: String,
        imageUrl: String,
        audioUrl: String?,
        reporterName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
        routingTo: String? = null
    ): Result<AiProcessResult> = withContext(Dispatchers.IO) {
        val endpoint = "$BACKEND_BASE_URL$AI_PROCESS_PATH"
        Log.d(TAG, "AI PROCESS START endpoint=$endpoint issueId=$issueId")
        // Render free plan: retry up to 3 times on 404/503 (cold-start race condition)
        val maxRetries = 3
        var currentDelay = 2000L
        var lastError: Exception = Exception("AI processing failed")
        val token = getAuthToken()
        
        for (attempt in 1..maxRetries) {
            val result = runCatching { doProcessComplaintWithAi(endpoint, issueId, userId, imageUrl, audioUrl, reporterName, latitude, longitude, address, routingTo, token) }
            if (result.isSuccess) return@withContext result
            val ex = result.exceptionOrNull()
            val msg = ex?.message ?: ""
            Log.w(TAG, "AI PROCESS attempt $attempt/$maxRetries failed: $msg")
            if ((msg.contains("404") || msg.contains("503") || msg.contains("502") || msg.contains("timeout")) && attempt < maxRetries) {
                Log.d(TAG, "Render cold-start or timeout detected — waiting ${currentDelay}ms before retry...")
                delay(currentDelay)
                currentDelay *= 2
                continue
            }
            lastError = Exception(msg)
            break
        }
        Result.failure(lastError)
    }

    /**
     * Internal single-attempt AI processing call.
     */
    @Throws(Exception::class)
    private fun doProcessComplaintWithAi(
        endpoint: String,
        issueId: String,
        userId: String,
        imageUrl: String,
        audioUrl: String?,
        reporterName: String?,
        latitude: Double?,
        longitude: Double?,
        address: String?,
        routingTo: String?,
        token: String?
    ): AiProcessResult {
        val payload = JSONObject().apply {
            put("issueId", issueId)
            put("userId", userId)
            put("imageUrl", imageUrl)
            if (!audioUrl.isNullOrBlank()) put("audioUrl", audioUrl)
            if (!reporterName.isNullOrBlank()) put("reporterName", reporterName)
            if (latitude != null) put("latitude", latitude)
            if (longitude != null) put("longitude", longitude)
            if (!address.isNullOrBlank()) put("address", address)
            if (!routingTo.isNullOrBlank()) put("routingTo", routingTo)
        }

        val jsonBytes = payload.toString().toByteArray(StandardCharsets.UTF_8)
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 120000
            readTimeout = 120000
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            setFixedLengthStreamingMode(jsonBytes.size)
        }

        try {
            connection.outputStream.use { os ->
                os.write(jsonBytes)
                os.flush()
            }
            val responseCode = connection.responseCode
            val responseBody = readHttpResponse(connection, responseCode)
            Log.d(TAG, "AI PROCESS RESPONSE CODE: $responseCode")
            Log.d(TAG, "AI PROCESS BODY: $responseBody")

            if (responseCode !in 200..299) {
                throw IllegalStateException("Backend server error ($responseCode): $responseBody")
            }

            val json = JSONObject(responseBody)
            val isSuccess = json.optBoolean("success", false)
            val isApproved = json.optBoolean("approved", false)
            val aiObj = json.optJSONObject("ai")
            val imgVerObj = json.optJSONObject("imageVerification")
            val firestoreObj = json.optJSONObject("firestore")

            val category = aiObj?.optString("category") ?: imgVerObj?.optString("category") ?: json.optString("category", "")
            val summary = aiObj?.optString("summary") ?: ""
            val description = aiObj?.optString("description") ?: ""
            val priority = aiObj?.optString("priority") ?: ""
            val reason = aiObj?.optString("reason") ?: imgVerObj?.optString("reason") ?: json.optString("reason", "")
            val transcript = aiObj?.optString("transcript") ?: json.optString("transcript", "")
            val firestoreWritten = firestoreObj?.optBoolean("written", false) ?: false
            val firestoreIssueId = firestoreObj?.optString("issueId") ?: json.optString("issueId", issueId)

            Log.d(TAG, "AI PROCESS RESULT approved=$isApproved category=$category")
            return AiProcessResult(
                success = isSuccess,
                approved = isApproved,
                category = category,
                summary = summary,
                description = description,
                priority = priority,
                reason = reason,
                transcript = transcript,
                firestoreWritten = firestoreWritten,
                issueId = firestoreIssueId
            )
        } finally {
            connection.disconnect()
        }
    }


}
