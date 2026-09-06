package com.kartavya.server.controller

import com.kartavya.server.service.MediaStorage
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/files")
class MediaController(private val mediaStorage: MediaStorage) {
    @GetMapping("/images/{filename}")
    fun image(@PathVariable filename: String): ResponseEntity<ByteArray> = serve("images", filename)

    @GetMapping("/audio/{filename}")
    fun audio(@PathVariable filename: String): ResponseEntity<ByteArray> = serve("audio", filename)

    private fun serve(kind: String, filename: String): ResponseEntity<ByteArray> {
        val media = mediaStorage.read("/files/$kind/$filename", kind)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(media.contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${media.fileName}\"")
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
            .body(media.bytes)
    }
}
