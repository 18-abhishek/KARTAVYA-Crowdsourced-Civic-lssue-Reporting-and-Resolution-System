package com.kartavya.server.controller

import com.kartavya.server.model.UploadResponse
import com.kartavya.server.service.MediaStorage
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class FileUploadController(
    private val mediaStorage: MediaStorage
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
            val originalName = file.originalFilename?.trim()?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                ?: "file$defaultExt"
            val relativePath = mediaStorage.store(subDirectory, originalName, file.contentType, file.bytes)
            val filename = relativePath.substringAfterLast('/')

            return ResponseEntity.ok(
                UploadResponse(
                    success = true,
                    path = relativePath,
                    filename = filename
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
