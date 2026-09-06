package com.kartavya.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.kartavya.server.model.AiProcessRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class KartavyaServerApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun contextLoads() {
    }

    @Test
    fun testHealthEndpoint() {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    fun testRootEndpoint() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ok"))
    }

    @Test
    fun testImageUpload() {
        val sampleFile = MockMultipartFile(
            "file",
            "test_image.jpg",
            "image/jpeg",
            "dummy image bytes".toByteArray()
        )

        mockMvc.perform(multipart("/upload/image").file(sampleFile))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.path").value(org.hamcrest.Matchers.matchesPattern("/files/images/[0-9a-f-]+\\.jpg")))
            .andExpect(jsonPath("$.filename").isNotEmpty)
    }

    @Test
    fun testAiProcessComplaint() {
        val request = AiProcessRequest(
            issueId = "test-issue-123",
            userId = "test-user-456",
            imageUrl = "/files/images/test_image.jpg",
            address = "MG Road, Sector 14",
            routingTo = "NDMC Authority"
        )

        mockMvc.perform(
            post("/ai/process-complaint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.approved").value(true))
            .andExpect(jsonPath("$.category").isNotEmpty)
            .andExpect(jsonPath("$.ai.summary").isNotEmpty)
            .andExpect(jsonPath("$.firestore.issueId").value("test-issue-123"))
    }
}
