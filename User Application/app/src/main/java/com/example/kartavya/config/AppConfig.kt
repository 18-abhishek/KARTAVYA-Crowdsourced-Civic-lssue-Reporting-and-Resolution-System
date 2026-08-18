package com.example.kartavya.config

/**
 * ⚡ KARTAVYA APP CONFIGURATION ⚡
 *
 * Update your Cloudflare Tunnel / Backend URL here in ONE line!
 */
object AppConfig {

    // =========================================================================
    // 🌐 CLOUDFLARE TUNNEL / BACKEND BASE URL
    // =========================================================================
    // Paste your new Cloudflare URL below whenever you restart your tunnel:
    // Example: "https://your-tunnel-name.trycloudflare.com"
    // Local Emulator: "http://10.0.2.2:8080"
    
    const val CLOUDFLARE_URL = "https://lovers-printers-imperial-narrow.trycloudflare.com"

    /**
     * Clean backend base URL without any trailing slash.
     */
    val BACKEND_BASE_URL: String
        get() = CLOUDFLARE_URL.trim().removeSuffix("/")
}
