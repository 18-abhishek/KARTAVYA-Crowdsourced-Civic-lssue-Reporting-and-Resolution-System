package com.kartavya.server.controller

import com.kartavya.server.model.AiProcessRequest
import com.kartavya.server.model.AiProcessResponse
import com.kartavya.server.service.AiService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus

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
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        val uid = httpRequest.getAttribute("uid") as String?
        if (firebaseService.isEnabled && uid != null && uid != request.userId) {
            logger.warn("IDOR attempt detected: Authenticated UID $uid attempted to process issue for userId ${request.userId}")
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: UID mismatch")
        }

        logger.info("Received complaint processing request for issueId=${request.issueId}, userId=${request.userId}")
        val response = aiService.processComplaint(request)
        return ResponseEntity.ok(response)
    }
}
