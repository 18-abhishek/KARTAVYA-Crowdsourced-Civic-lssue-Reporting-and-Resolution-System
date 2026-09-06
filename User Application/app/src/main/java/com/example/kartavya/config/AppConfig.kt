package com.example.kartavya.config

/**
 * ⚡ KARTAVYA APP CONFIGURATION ⚡
 *
 * Configure the deployed Render backend URL here in ONE line.
 */
object AppConfig {

    // =========================================================================
    // 🌐 DEPLOYED BACKEND BASE URL
    // =========================================================================
    // Local Emulator: "http://10.0.2.2:8080"
    const val BACKEND_URL = "https://kartavya-backend-server.onrender.com"

    /**
     * Clean backend base URL without any trailing slash.
     */
    val BACKEND_BASE_URL: String
        get() = BACKEND_URL.trim().removeSuffix("/")
}
