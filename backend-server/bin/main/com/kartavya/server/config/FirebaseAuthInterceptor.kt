package com.kartavya.server.config

import com.kartavya.server.service.FirebaseService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class FirebaseAuthInterceptor(private val firebaseService: FirebaseService) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(FirebaseAuthInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // If Firebase is not configured (e.g. local dev), allow requests to pass.
        // We will enforce that production MUST have Firebase enabled.
        if (!firebaseService.isEnabled) {
            logger.warn("Firebase Auth is disabled. Allowing unauthenticated request to ${request.requestURI}")
            return true
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ", ignoreCase = true)) {
            logger.warn("Unauthorized access attempt to ${request.requestURI} - Missing or invalid Authorization header")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header")
            return false
        }

        return try {
            val token = firebaseService.verifyBearerToken(authHeader)
            if (token != null) {
                // Attach the UID to the request for authorization checks down the line
                request.setAttribute("uid", token.uid)
                true
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
                false
            }
        } catch (e: Exception) {
            logger.warn("Token verification failed for ${request.requestURI}: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token verification failed")
            false
        }
    }
}
