package com.kartavya.server.controller

import com.kartavya.server.model.HealthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/")
    fun root(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(HealthResponse(status = "ok"))
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(HealthResponse(status = "UP"))
    }
}
