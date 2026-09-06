package com.kartavya.server.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

data class StoredMedia(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String
)

interface MediaStorage {
    fun store(kind: String, originalName: String?, contentType: String?, bytes: ByteArray): String
    fun read(path: String, kind: String): StoredMedia
}

@Service
class ConfiguredMediaStorage(
    @Value("\${app.storage.provider:local}") private val provider: String,
    @Value("\${app.storage.base-path:./data/uploads}") private val localBasePath: String,
    @Value("\${firebase.storage.bucket:}") private val configuredBucket: String,
    @Value("\${firebase.service-account-json:}") private val serviceAccountJson: String
) : MediaStorage {
    private val logger = LoggerFactory.getLogger(ConfiguredMediaStorage::class.java)
    private val firebaseStorage: Storage? = if (provider.equals("firebase", ignoreCase = true)) {
        createFirebaseStorage()
    } else {
        null
    }

    override fun store(kind: String, originalName: String?, contentType: String?, bytes: ByteArray): String {
        val safeKind = validateKind(kind)
        val extension = originalName?.substringAfterLast('.', "")?.let { if (it.isBlank()) "" else ".${it.lowercase()}" } ?: ""
        val fileName = "${UUID.randomUUID()}$extension"
        val objectName = "$safeKind/$fileName"
        val resolvedContentType = contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"

        if (firebaseStorage != null) {
            val blobInfo = BlobInfo.newBuilder(BlobId.of(configuredBucket, objectName))
                .setContentType(resolvedContentType)
                .build()
            firebaseStorage.create(blobInfo, bytes)
        } else {
            val target = localPath(objectName)
            Files.createDirectories(target.parent)
            Files.write(target, bytes)
        }

        logger.info("Stored {} upload as {} using {} storage", kind, objectName, provider)
        return "/files/$objectName"
    }

    override fun read(path: String, kind: String): StoredMedia {
        val objectName = objectName(path, kind)
        val bytes = if (firebaseStorage != null) {
            firebaseStorage.get(BlobId.of(configuredBucket, objectName))?.getContent()
                ?: error("Media object not found: $objectName")
        } else {
            Files.readAllBytes(localPath(objectName))
        }
        val fileName = objectName.substringAfterLast('/')
        return StoredMedia(bytes, fileName, contentType(fileName, kind))
    }

    private fun createFirebaseStorage(): Storage {
        require(configuredBucket.isNotBlank()) { "FIREBASE_STORAGE_BUCKET is required when MEDIA_STORAGE_PROVIDER=firebase" }
        require(serviceAccountJson.isNotBlank()) { "FIREBASE_SERVICE_ACCOUNT_JSON is required when MEDIA_STORAGE_PROVIDER=firebase" }
        val credentials = GoogleCredentials.fromStream(ByteArrayInputStream(serviceAccountJson.toByteArray()))
        return StorageOptions.newBuilder()
            .setCredentials(credentials)
            .build()
            .service
    }

    private fun objectName(path: String, kind: String): String {
        val safeKind = validateKind(kind)
        val cleanPath = path.substringBefore('?').substringAfter("/files/").trim('/')
        require(cleanPath.startsWith("$safeKind/")) { "Media path does not match its type" }
        val name = cleanPath.removePrefix("$safeKind/")
        require(name.isNotBlank() && !name.contains("..") && !name.contains('/')) { "Invalid media path" }
        return "$safeKind/$name"
    }

    private fun localPath(objectName: String): Path =
        Paths.get(localBasePath).toAbsolutePath().normalize().resolve(objectName).normalize().also {
            require(it.startsWith(Paths.get(localBasePath).toAbsolutePath().normalize())) { "Invalid media path" }
        }

    private fun validateKind(kind: String): String =
        kind.takeIf { it == "images" || it == "audio" } ?: error("Unsupported media type")

    private fun contentType(fileName: String, kind: String): String = when {
        kind == "images" && fileName.endsWith(".png", true) -> "image/png"
        kind == "images" && fileName.endsWith(".webp", true) -> "image/webp"
        kind == "audio" && fileName.endsWith(".mp3", true) -> "audio/mpeg"
        kind == "audio" && fileName.endsWith(".wav", true) -> "audio/wav"
        else -> if (kind == "images") "image/jpeg" else "audio/mp4"
    }
}
