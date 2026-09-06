package com.kartavya.server.controller

import com.kartavya.server.model.UploadResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID

@RestController
class FileUploadController(
    @Value("\${app.storage.base-path:./data/uploads}") private val storageBasePath: String
) {
    private val logger = LoggerFactory.getLogger(FileUploadController::class.java)

    @PostMapping("/upload/image")
    fun uploadImage(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadResponse> {
        return handleUpload(file, "images", ".jpg")
    }

    @PostMapping("/upload/audio")
    fun uploadAudio(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadResponse> {
        return handleUpload(file, "audio", ".m4a")
    }

    private fun handleUpload(file: MultipartFile, subDirectory: String, defaultExt: String): ResponseEntity<UploadResponse> {
        if (file.isEmpty) {
            logger.warn("Upload failed: File is empty")
            return ResponseEntity.badRequest().body(
                UploadResponse(success = false, error = "Uploaded file cannot be empty")
            )
        }

        try {
            val targetDir = File(storageBasePath, subDirectory)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val originalName = file.originalFilename?.trim()
            val safeName = if (!originalName.isNullOrBlank()) {
                // Keep alphanumeric, dash, underscore, dot
                originalName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            } else {
                "file_${UUID.randomUUID()}$defaultExt"
            }

            val targetFile = File(targetDir, safeName)
            file.transferTo(targetFile.toPath())

            val relativePath = "/files/$subDirectory/$safeName"
            logger.info("Successfully saved upload to $relativePath (size: ${file.size} bytes)")

            return ResponseEntity.ok(
                UploadResponse(
                    success = true,
                    path = relativePath
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to save uploaded file", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                UploadResponse(
                    success = false,
                    error = "Failed to store file: ${e.message}"
                )
            )
        }
    }
}
