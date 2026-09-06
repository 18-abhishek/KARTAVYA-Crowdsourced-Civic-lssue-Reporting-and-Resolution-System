# KARTAVYA Backend Server (Spring Boot / Kotlin / Java 21)

This is the backend server microservice for the **KARTAVYA Crowdsourced Civic Issue Reporting and Resolution System**. It serves both the **Android User Application** and provides static assets / AI validation capabilities.

---

## Features

- **Executable Fat JAR**: Built with Spring Boot 3 & Kotlin on Java 21, packing all dependencies into a standalone `.jar`.
- **Multipart Uploads**: High-speed image and audio endpoints:
  - `POST /upload/image` -> returns `{"success": true, "path": "/files/images/<filename>"}`
  - `POST /upload/audio` -> returns `{"success": true, "path": "/files/audio/<filename>"}`
- **Static Asset Serving**: Serves files directly under `/files/**` (e.g. `/files/images/...`, `/files/audio/...`).
- **AI Processing Pipeline**:
  - `POST /ai/process-complaint`: Integrates with Google Gemini 1.5 Flash Vision for civic image authenticity and classification, Sarvam STT for audio transcription, and contains a reliable fallback civic categorization engine.
- **Health Checks**:
  - `GET /`: returns status `ok`
  - `GET /health`: returns status `UP`
- **Render Ready**: Multi-stage `Dockerfile` that binds dynamically to Render's injected `$PORT` environment variable (`-Dserver.port=${PORT:-8080}`).

---

## 1. Local Development & Building the Fat JAR

### Requirements
- JDK 21+ (`java -version`)
- Windows PowerShell or Bash

### Build the Fat JAR
From the `backend-server/` directory:

```powershell
# On Windows:
.\gradlew.bat bootJar

# On Linux/macOS:
chmod +x gradlew
./gradlew bootJar
```

The output fat JAR will be located at:
```
backend-server/build/libs/kartavya-backend-server.jar
```

### Run the Fat JAR Locally
```powershell
java -jar build/libs/kartavya-backend-server.jar
```
By default, the server runs on port `8080` (accessible at `http://localhost:8080`).

To run on a custom port:
```powershell
java -Dserver.port=9000 -jar build/libs/kartavya-backend-server.jar
```

---

## 2. Docker Deployment

### Build the Docker Image
```bash
docker build -t kartavya-backend-server .
```

### Run the Docker Container
```bash
docker run -p 8080:8080 -e PORT=8080 kartavya-backend-server
```

---

## 3. Deploying to Render (Step-by-Step)

### Option A: Via Render Web Dashboard (Recommended)
1. **Push your code** to GitHub.
2. Log into [Render Dashboard](https://dashboard.render.com).
3. Click **New +** -> **Web Service**.
4. Connect your GitHub repository: `KARTAVYA-Crowdsourced-Civic-lssue-Reporting-and-Resolution-System`.
5. Configure the service settings:
   - **Name**: `kartavya-backend-server`
   - **Language / Runtime**: `Docker`
   - **Root Directory**: `backend-server`
   - **Dockerfile Path**: `./Dockerfile` (or leave default since root dir is set)
   - **Instance Type**: `Free`
6. (Optional) Under **Environment Variables**, add:
   - `GEMINI_API_KEY`: *(Your Google Gemini API key)*
   - `SARVAM_API_KEY`: *(Your Sarvam AI API key)*
   - *Note: Render automatically injects `PORT` (usually 10000); our Docker container handles this automatically!*
7. Click **Create Web Service**.
8. Once deployed, Render will provide a public URL, for example:
   ```
   https://kartavya-backend-server.onrender.com
   ```

### Option B: Via `render.yaml` Blueprint
Render will automatically detect `backend-server/render.yaml` if you choose **New +** -> **Blueprint**.

---

## 4. Connecting the Android App to your Render Server

1. Open `User Application/app/src/main/java/com/example/kartavya/config/AppConfig.kt`.
2. Update `BACKEND_BASE_URL` with your Render service URL:

```kotlin
package com.example.kartavya.config

object AppConfig {
    // Replace with your Render URL (no trailing slash)
    const val BACKEND_BASE_URL: String = "https://kartavya-backend-server.onrender.com"
}
```

3. Build and run the Android app! The app will now automatically upload images and trigger AI processing via your Render backend.

---

## API Reference

### Health Check
- `GET /health`
  - Response:
    ```json
    { "status": "UP", "service": "kartavya-backend-server", "timestamp": 1725667200000 }
    ```

### Image Upload
- `POST /upload/image`
  - Content-Type: `multipart/form-data`
  - Body: `file` (binary JPEG/PNG)
  - Response:
    ```json
    { "success": true, "path": "/files/images/issue_xxx_image.jpg" }
    ```

### Audio Upload
- `POST /upload/audio`
  - Content-Type: `multipart/form-data`
  - Body: `file` (binary M4A/AAC/WAV/MP3)
  - Response:
    ```json
    { "success": true, "path": "/files/audio/issue_xxx_voice.m4a" }
    ```

### Process Complaint (AI)
- `POST /ai/process-complaint`
  - Content-Type: `application/json`
  - Request Body:
    ```json
    {
      "issueId": "uuid",
      "userId": "user-uid",
      "imageUrl": "/files/images/issue_xxx_image.jpg",
      "audioUrl": "/files/audio/issue_xxx_voice.m4a",
      "address": "Sector 14, Gurugram",
      "routingTo": "Municipal Corporation"
    }
    ```
  - Response:
    ```json
    {
      "success": true,
      "approved": true,
      "category": "Road Damage",
      "reason": "Civic condition validated: authentic public utility damage verified.",
      "ai": {
        "category": "Road Damage",
        "summary": "Potholes with Standing Water on Road",
        "description": "Multiple potholes filled with standing water detected on the road surface.",
        "priority": "Moderate",
        "reason": "Civic condition validated: authentic public utility damage verified.",
        "transcript": ""
      },
      "imageVerification": {
        "approved": true,
        "category": "Road Damage",
        "reason": "Civic condition validated: authentic public utility damage verified."
      },
      "firestore": {
        "written": false,
        "issueId": "uuid"
      }
    }
    ```
