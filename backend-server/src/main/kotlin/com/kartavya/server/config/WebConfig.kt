package com.kartavya.server.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebConfig(
    @Value("\${app.storage.base-path:./data/uploads}") private val storageBasePath: String
) : WebMvcConfigurer {

    @PostConstruct
    fun init() {
        val baseDir = File(storageBasePath)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        File(baseDir, "images").mkdirs()
        File(baseDir, "audio").mkdirs()
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadDir = File(storageBasePath).absolutePath.replace("\\", "/")
        val resourceLocation = if (uploadDir.endsWith("/")) "file:$uploadDir" else "file:$uploadDir/"
        registry.addResourceHandler("/files/**")
            .addResourceLocations(resourceLocation)
    }
}
