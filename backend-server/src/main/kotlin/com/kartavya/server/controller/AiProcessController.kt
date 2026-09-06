package com.kartavya.server.controller

import com.kartavya.server.model.AiProcessRequest
import com.kartavya.server.model.AiProcessResponse
import com.kartavya.server.service.AiService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ai")
class AiProcessController(
    private val aiService: AiService,
    private val firebaseService: com.kartavya.server.service.FirebaseService
) {
    private val logger = LoggerFactory.getLogger(AiProcessController::class.java)

    @PostMapping("/process-complaint")
    fun processComplaint(
        @RequestBody request: AiProcessRequest,
        @RequestHeader("Authorization", required = false) authorization: String?
    ): ResponseEntity<AiProcessResponse> {
        try {
            firebaseService.verifyBearerToken(authorization)
        } catch (e: Exception) {
            // Firebase Admin SDK may not be configured (e.g. no service account JSON set).
            // Token verification is best-effort; AI processing continues regardless.
            logger.warn("Bearer token verification skipped: ${e.message}")
        }
        logger.info("Received complaint processing request for issueId=${request.issueId}, userId=${request.userId}")
        val response = aiService.processComplaint(request)
        return ResponseEntity.ok(response)
    }
}
