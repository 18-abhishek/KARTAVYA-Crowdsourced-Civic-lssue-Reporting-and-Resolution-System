package com.example.kartavya.presentation.screens

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import com.example.kartavya.data.UserRepository
import android.graphics.Bitmap
import com.example.kartavya.core.utils.loadDownsampledBitmap
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import androidx.core.content.ContextCompat
import com.example.kartavya.core.location.fetchDeviceLocation
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.data.IssueRepository
import com.example.kartavya.data.SupabaseStorageRepository
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.IssueStatus
import com.example.kartavya.model.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID

@Composable
fun ReportIssueFlow(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    onCancel: () -> Unit
) {
    // Steps: 1: Media, 2: Location, 3: AI Processing (Screen 3A), 4: Report Preview (Screen 3B), 5: Success Screen
    var step by remember { mutableIntStateOf(1) }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var locationName by remember { mutableStateOf("Kothri Kalan, Bhopal") }
    var geoLat by remember { mutableStateOf<Double?>(23.26112) }
    var geoLng by remember { mutableStateOf<Double?>(77.32645) }

    // Screen 3A AI Processing State
    var currentTimelineStep by remember { mutableIntStateOf(1) } // 1..5
    var aiProcessResult by remember { mutableStateOf<com.example.kartavya.data.AiProcessResult?>(null) }
    var uploadedImagePath by remember { mutableStateOf<String?>(null) }
    var uploadedAudioPath by remember { mutableStateOf<String?>(null) }
    var aiProcessingError by remember { mutableStateOf<String?>(null) }

    // Rejection Flow State
    var showRejectionDialog by remember { mutableStateOf(false) }
    var rejectionReasonText by remember { mutableStateOf("") }

    // Screen 3B Final Submission State
    var isSubmittingToFirestore by remember { mutableStateOf(false) }
    var submissionTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Warm up the Render server as soon as the user opens the reporting flow
    LaunchedEffect(Unit) {
        SupabaseStorageRepository.warmUpServer(maxAttempts = 6, delayMs = 5000L)
    }

    // Real Backend AI Pipeline Execution function
    fun runBackendAiPipeline() {
        coroutineScope.launch {
            aiProcessingError = null
            currentTimelineStep = 1 // 1: Uploading photo/audio

            val uid = currentUser?.uid
            if (uid.isNullOrBlank()) {
                aiProcessingError = "User not authenticated. Please sign in to submit reports."
                return@launch
            }

            if (photoUri == null || audioFile == null || !audioFile!!.exists()) {
                aiProcessingError = "Both a photo and a voice recording are required to report an issue."
                return@launch
            }

            val issueId = SupabaseStorageRepository.generateIssueId()

            // 0. Warm up the Render server (free plan sleeps after 15 min inactivity).
            //    This ensures the JVM is fully started before we upload files and call AI.
            SupabaseStorageRepository.warmUpServer(maxAttempts = 6, delayMs = 5000L)

            // 1. Upload Image to POST /upload/image
            val imgResult = SupabaseStorageRepository.uploadIssueImage(
                uid = uid,
                issueId = issueId,
                imageUri = photoUri!!,
                context = context
            )

            val imagePath = imgResult.getOrElse { e ->
                aiProcessingError = e.message ?: "Image upload failed."
                return@launch
            }
            uploadedImagePath = imagePath

            // 2. Upload Audio to POST /upload/audio (if recorded)
            var audioPath: String? = null
            if (audioFile != null && audioFile!!.exists()) {
                val audioResult = SupabaseStorageRepository.uploadIssueAudio(uid, issueId, audioFile!!)
                audioPath = audioResult.getOrElse { e ->
                    aiProcessingError = e.message ?: "Audio upload failed."
                    return@launch
                }
            }
            uploadedAudioPath = audioPath

            // Timeline Step 2: Verifying civic issue with Gemini
            currentTimelineStep = 2
            delay(500)

            // Timeline Step 3: Understanding voice (Sarvam STT) if audio recorded
            if (audioPath != null) {
                currentTimelineStep = 3
                delay(500)
            }

            // Timeline Step 4: Creating report summary with Gemini
            currentTimelineStep = 4

            // 3. Send AI Process request to POST /ai/process-complaint
            val aiResult = SupabaseStorageRepository.processComplaintWithAi(
                issueId = issueId,
                userId = uid,
                imageUrl = imagePath,
                audioUrl = audioPath,
                reporterName = userProfile?.name ?: currentUser.displayName ?: "Citizen",
                latitude = geoLat,
                longitude = geoLng,
                address = locationName,
                routingTo = "NDMC Authority"
            )

            aiResult.onSuccess { res ->
                if (!res.approved) {
                    // Image rejected by Gemini AI - do NOT continue to report preview & do NOT write to Firestore
                    rejectionReasonText = if (res.reason.isNotBlank()) res.reason else "Photo could not be verified as a civic issue by Gemini AI."
                    showRejectionDialog = true
                } else {
                    // AI pipeline approved! Advance timeline to 5 and transition to Screen 3B Report Preview
                    currentTimelineStep = 5
                    delay(300)
                    aiProcessResult = res
                    step = 4 // Screen 3B — Final Report Preview
                }
            }.onFailure { e ->
                aiProcessingError = e.message ?: "AI Processing error."
            }
        }
    }

    // Trigger AI pipeline when entering step 3 (Screen 3A)
    LaunchedEffect(step) {
        if (step == 3) {
            runBackendAiPipeline()
        }
    }

    // Rejection Dialog Flow
    if (showRejectionDialog) {
        AlertDialog(
            onDismissRequest = { showRejectionDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = CardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFEBEE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Couldn’t verify this issue", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
                }
            },
            text = {
                Column {
                    Text(
                        text = if (rejectionReasonText.isNotBlank()) rejectionReasonText else "Gemini AI could not verify the uploaded photo as a valid civic issue.",
                        fontSize = 14.sp,
                        color = TextDark,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Please retake a clear photo of the street or public issue.",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectionDialog = false
                        step = 1 // Go back to Step 1 Media to retake photo
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = TextDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retake Photo", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectionDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = TextGray)
                }
            }
        )
    }

    if (step == 5) {
        // Step 5: Full-Screen Green Gradient Celebration & Report Summary
        StepFiveSuccess(
            aiResult = aiProcessResult ?: com.example.kartavya.data.AiProcessResult(
                success = true,
                approved = true,
                category = "Road Damage",
                summary = "Potholes with Standing Water on Road"
            ),
            locationName = locationName,
            lat = geoLat,
            lng = geoLng,
            submissionTime = submissionTimestamp,
            onBackToHome = onCancel
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top Header Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CardBg, CircleShape)
                        .border(1.dp, LightGrayBorder, CircleShape)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextGray)
                }

                // Step dots indicator
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(4) { index ->
                        val dotStep = index + 1
                        val isActive = dotStep == step
                        val isPast = dotStep < step
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isActive) 24.dp else if (isPast) 16.dp else 8.dp)
                                .background(
                                    color = if (isActive) PrimaryYellow else if (isPast) TextDark else LightGrayBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Content Area
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                    }
                ) { targetStep ->
                    when (targetStep) {
                        1 -> StepOneMedia(
                            photoUri = photoUri,
                            onPhotoCaptured = { uri -> photoUri = uri },
                            audioFile = audioFile,
                            onAudioRecorded = { file -> audioFile = file },
                            onLocationCaptured = { loc, lat, lng ->
                                locationName = loc
                                geoLat = lat
                                geoLng = lng
                            }
                        )
                        2 -> StepTwoLocation(
                            locationName = locationName,
                            lat = geoLat,
                            lng = geoLng,
                            onLocationChanged = { newLoc -> locationName = newLoc }
                        )
                        3 -> StepThreeAiProcessing(
                            photoUri = photoUri,
                            currentTimelineStep = currentTimelineStep,
                            errorMessage = aiProcessingError,
                            onRetry = { runBackendAiPipeline() },
                            onCancel = onCancel
                        )
                        4 -> StepFourReportPreview(
                            photoUri = photoUri,
                            aiResult = aiProcessResult ?: com.example.kartavya.data.AiProcessResult(
                                success = true,
                                approved = true,
                                category = "Road Damage",
                                summary = "Potholes with Standing Water on Road",
                                description = "Multiple potholes filled with standing water detected on the road surface.",
                                priority = "Moderate"
                            ),
                            locationName = locationName,
                            lat = geoLat,
                            lng = geoLng,
                            audioFile = audioFile,
                            isSubmitting = isSubmittingToFirestore,
                            onSubmit = {
                                val uid = currentUser?.uid ?: return@StepFourReportPreview
                                val res = aiProcessResult ?: return@StepFourReportPreview
                                val imgPath = uploadedImagePath ?: return@StepFourReportPreview

                                isSubmittingToFirestore = true
                                coroutineScope.launch {
                                    try {
                                        val issueToSave = CivicIssue(
                                            userId = uid,
                                            reporterName = userProfile?.name ?: currentUser.displayName ?: "Citizen",
                                            title = res.summary.ifBlank { "Civic issue reported" },
                                            description = res.description.ifBlank { res.summary },
                                            category = res.category.ifBlank { "Civic Issue" },
                                            imageUrls = listOf(imgPath),
                                            audioUrl = uploadedAudioPath ?: "",
                                            latitude = geoLat,
                                            longitude = geoLng,
                                            address = locationName,
                                            status = com.example.kartavya.model.IssueStatus.REPORTED.name,
                                            routingTo = "NDMC Authority",
                                            priority = res.priority.ifBlank { "Moderate" }
                                        )

                                        val writeResult = IssueRepository.createIssue(issueToSave)
                                        writeResult.onSuccess {
                                            UserRepository.incrementOnReport(uid)
                                            submissionTimestamp = System.currentTimeMillis()
                                            isSubmittingToFirestore = false
                                            step = 5 // Step 5 — Success Screen
                                        }.onFailure { e ->
                                            isSubmittingToFirestore = false
                                            Toast.makeText(context, "Firestore submission failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        isSubmittingToFirestore = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Bottom CTA Button for Steps 1 and 2 (Steps 3, 4, 5 render their own bottom CTAs)
            if (step in 1..2) {
                val isMediaValid = photoUri != null && audioFile != null && audioFile!!.exists()
                val isStepEnabled = if (step == 1) isMediaValid else true

                Button(
                    enabled = isStepEnabled,
                    onClick = {
                        if (step == 1 && !isMediaValid) {
                            Toast.makeText(context, "Please capture both a photo and voice recording to continue.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        step += 1
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(bottom = 4.dp),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = TextDark,
                        disabledContainerColor = LightGrayBorder,
                        disabledContentColor = TextGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(text = "Continue", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
}
}

// ──────────────────────────────────────────────────────
// Step 1 — Media Capture
// ──────────────────────────────────────────────────────
@Composable
fun StepOneMedia(
    photoUri: Uri?,
    onPhotoCaptured: (Uri?) -> Unit,
    audioFile: File?,
    onAudioRecorded: (File?) -> Unit,
    onLocationCaptured: (String, Double?, Double?) -> Unit
) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(audioFile) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun createTempPictureUri(): Uri {
        val tempFile = File.createTempFile("civic_camera_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    val grabLocation = {
        fetchDeviceLocation(context) { name, lat, lng ->
            onLocationCaptured(name, lat, lng)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            grabLocation()
        }
    }

    val requestLocationAndCapture = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            grabLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            onPhotoCaptured(tempUri)
            requestLocationAndCapture()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempPictureUri()
            tempUri = uri
            cameraLauncher.launch(uri)
            requestLocationAndCapture()
        }
    }

    val launchCamera = {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val uri = createTempPictureUri()
            tempUri = uri
            cameraLauncher.launch(uri)
            requestLocationAndCapture()
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    fun startRecordingAudio() {
        try {
            val audioOutput = File.createTempFile("voice_note_", ".m4a", context.cacheDir)
            currentAudioFile = audioOutput
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioOutput.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    fun stopRecordingAudio() {
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            isRecording = false
            onAudioRecorded(currentAudioFile)
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startRecordingAudio()
    }

    val toggleRecording = {
        if (isRecording) {
            stopRecordingAudio()
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecordingAudio()
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val playAudio = {
        currentAudioFile?.let { file ->
            if (isPlayingAudio) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                isPlayingAudio = false
            } else {
                try {
                    val player = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                        setOnCompletionListener { isPlayingAudio = false }
                        start()
                    }
                    mediaPlayer = player
                    isPlayingAudio = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
                mediaPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    val bitmapState = remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        if (photoUri != null) {
            bitmapState.value = loadDownsampledBitmap(context, photoUri)
        } else {
            bitmapState.value = null
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(text = "Report an", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark, lineHeight = 40.sp)
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 24.dp)
                    .background(PrimaryYellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
            Text(text = "Issue", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark, lineHeight = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
        }
        Text(text = "Help make your city better in 5 min.", fontSize = 16.sp, color = TextGray, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Camera Option
                Row(verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(48.dp).background(Color(0xFFFFF9E6), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color(0xFFFFB300))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Take a Photo", fontWeight = FontWeight.Bold, color = TextDark)
                        Text(
                            text = if (photoUri != null) "Live photo + Geo-Tag recorded! Tap to retake." else "Capture the issue clearly with automatic GPS coordinates.",
                            fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(top = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(if (photoUri != null) 160.dp else 96.dp)
                                .background(BgCream, RoundedCornerShape(16.dp))
                                .border(1.dp, if (photoUri != null) PrimaryYellow else LightGrayBorder, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { launchCamera() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (bitmapState.value != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        bitmap = bitmapState.value!!.asImageBitmap(),
                                        contentDescription = "Captured Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.CameraAlt, contentDescription = "Retake", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retake", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TextGray)
                                    Text("Tap to open camera & GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = LightGrayBorder)

                // Voice Option
                Row(verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(48.dp).background(Color(0xFFE6F4FF), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color(0xFF0088FF))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice Summary", fontWeight = FontWeight.Bold, color = TextDark)
                        Text(
                            text = if (isRecording) "Recording audio note..." else if (currentAudioFile != null) "Voice summary recorded!" else "Describe what you see in speech.",
                            fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(top = 4.dp)
                        )

                        if (currentAudioFile != null && !isRecording) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(Color(0xFFE6F4FF), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { playAudio() }) {
                                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF0088FF), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isPlayingAudio) Icons.Rounded.Close else Icons.Rounded.PlayArrow,
                                            contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(if (isPlayingAudio) "Playing Note..." else "Voice Note", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0088FF))
                                        Text("Tap to listen", fontSize = 10.sp, color = TextGray)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .clickable { currentAudioFile = null; onAudioRecorded(null) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Re-record", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                            }
                        } else {
                            Button(
                                onClick = { toggleRecording() },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecording) Color(0xFFFFEBEE) else Color(0xFFE6F4FF),
                                    contentColor = if (isRecording) Color(0xFFD32F2F) else Color(0xFF0088FF)
                                )
                            ) {
                                if (isRecording) {
                                    Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Recording (${recordingSeconds}s) • Tap to Stop", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Rounded.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tap to record speech", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ──────────────────────────────────────────────────────
// Step 2 — Location
// ──────────────────────────────────────────────────────
@Composable
fun StepTwoLocation(
    locationName: String,
    lat: Double?,
    lng: Double?,
    onLocationChanged: (String) -> Unit
) {
    Column {
        Text(text = "Pin the\nLocation", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark, lineHeight = 40.sp)
        Text(text = "We grabbed your exact GPS coords.", fontSize = 16.sp, color = TextGray, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 32.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp).background(Color(0xFFE8E5DF), RoundedCornerShape(24.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 40.dp.toPx()
                    for (x in 0 until size.width.toInt() step step.toInt()) {
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                    }
                    for (y in 0 until size.height.toInt() step step.toInt()) {
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                    }
                }

                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.background(TextDark, CircleShape).padding(16.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.width(24.dp).height(6.dp).background(Color.Black.copy(alpha = 0.2f), CircleShape).blur(4.dp))
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CURRENT LOCATION (GPS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                        Text(text = locationName, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextDark)
                        if (lat != null && lng != null) {
                            Text(text = "GPS: %.5f, %.5f".format(lat, lng), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenStatus, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    Box(modifier = Modifier.background(BgCream, CircleShape).padding(8.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Screen 3A — AI Processing / Waiting Screen
// ──────────────────────────────────────────────────────
@Composable
fun StepThreeAiProcessing(
    photoUri: Uri?,
    currentTimelineStep: Int,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val bitmapState = remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        if (photoUri != null) {
            bitmapState.value = loadDownsampledBitmap(context, photoUri)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Analyzing your report",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Our AI is checking the issue and preparing your report.",
            fontSize = 14.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Photo Thumbnail preview with sparkle decorations
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Color(0xFFFFF9E6), CircleShape)
            )

            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = PrimaryYellow,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = 12.dp)
            )

            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = PrimaryYellow,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-12).dp, y = (-12).dp)
            )

            Box(
                modifier = Modifier
                    .size(width = 150.dp, height = 110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCream)
                    .border(2.dp, PrimaryYellow, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmapState.value != null) {
                    Image(
                        bitmap = bitmapState.value!!.asImageBitmap(),
                        contentDescription = "Captured Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vertical Real-Time Processing Timeline Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                TimelineItem(
                    stepNumber = 1,
                    currentStep = currentTimelineStep,
                    title = "Uploading photo",
                    subtitle = "Photo uploaded successfully"
                )
                TimelineItem(
                    stepNumber = 2,
                    currentStep = currentTimelineStep,
                    title = "Verifying civic issue",
                    subtitle = "Image verified as a civic issue"
                )
                TimelineItem(
                    stepNumber = 3,
                    currentStep = currentTimelineStep,
                    title = "Understanding your voice",
                    subtitle = "Transcribing your voice description"
                )
                TimelineItem(
                    stepNumber = 4,
                    currentStep = currentTimelineStep,
                    title = "Creating report summary",
                    subtitle = "Summarizing the issue with AI"
                )
                TimelineItem(
                    stepNumber = 5,
                    currentStep = currentTimelineStep,
                    title = "Finalizing report",
                    subtitle = "Preparing final report for you",
                    isLast = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!errorMessage.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Processing", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Indeterminate Progress Indicator (Smooth Visual Pulse - NO percentage text claimed)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, LightGrayBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Please don't close the app",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(BgCream, RoundedCornerShape(5.dp))
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pulseAnim)
                                .fillMaxHeight()
                                .background(PrimaryYellow, RoundedCornerShape(5.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reassuring Shield Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
            border = BorderStroke(1.dp, PrimaryYellow.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryYellow.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your report is secure and will only be submitted after your confirmation.",
                    fontSize = 12.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TimelineItem(
    stepNumber: Int,
    currentStep: Int,
    title: String,
    subtitle: String,
    isLast: Boolean = false
) {
    val isCompleted = currentStep > stepNumber
    val isCurrent = currentStep == stepNumber
    val isPending = currentStep < stepNumber

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = GreenStatus,
                    modifier = Modifier.size(22.dp)
                )
            } else if (isCurrent) {
                CircularProgressIndicator(
                    color = PrimaryYellow,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = LightGrayBorder,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(if (isCompleted) GreenStatus.copy(alpha = 0.5f) else LightGrayBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 10.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Medium,
                color = if (isPending) TextGray else TextDark
            )
            if (isCompleted || isCurrent) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isCompleted) GreenStatus else TextGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Screen 3B — Final AI Report Preview Screen
// ──────────────────────────────────────────────────────
@Composable
fun StepFourReportPreview(
    photoUri: Uri?,
    aiResult: com.example.kartavya.data.AiProcessResult,
    locationName: String,
    lat: Double?,
    lng: Double?,
    audioFile: File?,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val bitmapState = remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        if (photoUri != null) {
            bitmapState.value = loadDownsampledBitmap(context, photoUri)
        }
    }

    var isPlayingAudio by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun playPauseAudio() {
        if (audioFile == null || !audioFile.exists()) return
        if (isPlayingAudio) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlayingAudio = false
        } else {
            try {
                val player = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    setOnCompletionListener { isPlayingAudio = false }
                    start()
                }
                mediaPlayer = player
                isPlayingAudio = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    val priorityColor = when (aiResult.priority.lowercase(Locale.US)) {
        "high", "critical" -> Color(0xFFD32F2F)
        "low" -> GreenStatus
        else -> OrangeStatus
    }

    val priorityBg = when (aiResult.priority.lowercase(Locale.US)) {
        "high", "critical" -> Color(0xFFFFEBEE)
        "low" -> Color(0xFFE6F9F0)
        else -> Color(0xFFFFF8E7)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Your report is ready!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = GreenStatus,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Here's what our AI understood from your report.",
            fontSize = 14.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 18.dp)
        )

        // Main Report Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Top Row: Captured Photo & Category/Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(125.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(BgCream),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmapState.value != null) {
                            Image(
                                bitmap = bitmapState.value!!.asImageBitmap(),
                                contentDescription = "Captured Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TextGray, modifier = Modifier.size(32.dp))
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Issue Category",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(PrimaryYellow.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = TextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = aiResult.category.ifBlank { "Civic Issue" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextDark
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LightGrayBorder)

                        Column {
                            Text(
                                text = "Priority",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(priorityBg, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(priorityColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = aiResult.priority.ifBlank { "Moderate" },
                                        color = priorityColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location Details Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCream, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE6F9F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = GreenStatus,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Location",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                        Text(
                            text = locationName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        if (lat != null && lng != null) {
                            Text(
                                text = "GPS: %.5f, %.5f".format(lat, lng),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenStatus,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // AI Summary & Description Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCream, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFEBF4FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Summary",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = aiResult.summary.ifBlank { "Civic issue reported near location" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )

                    if (aiResult.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = aiResult.description,
                            fontSize = 12.sp,
                            color = TextDark.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )
                    }
                }

                // Audio Recording Player (If voice recorded)
                if (audioFile != null && audioFile.exists()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgCream, RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFFE6F4FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = null,
                                    tint = Color(0xFF0088FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Voice Description",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE6F4FF), RoundedCornerShape(14.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF0088FF), CircleShape)
                                    .clickable { playPauseAudio() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val heights = listOf(12, 20, 14, 26, 18, 30, 22, 16, 28, 14, 22, 18, 10, 24, 16, 28, 12, 20)
                                heights.forEach { height ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(height.dp)
                                            .background(
                                                color = if (isPlayingAudio) Color(0xFF0088FF) else Color(0xFF0088FF).copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (aiResult.transcript.isNotBlank()) "Transcribed" else "Voice Note",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0088FF)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ONE Primary Submit Report Button (NO edit report button beside or below)
        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryYellow,
                contentColor = TextDark
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = TextDark,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Submitting Report...", fontSize = 17.sp, fontWeight = FontWeight.Black)
            } else {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Submit Report", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ──────────────────────────────────────────────────────
// Screen 5 — Full Screen Success Screen with Staggered Entrance Animation
// ──────────────────────────────────────────────────────
@Composable
fun StepFiveSuccess(
    aiResult: com.example.kartavya.data.AiProcessResult,
    locationName: String,
    lat: Double? = null,
    lng: Double? = null,
    submissionTime: Long = System.currentTimeMillis(),
    onBackToHome: () -> Unit
) {
    // Intercept hardware/gesture back press to return directly to Home screen
    BackHandler {
        onBackToHome()
    }

    val density = LocalDensity.current.density

    val formattedTime = remember(submissionTime) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(submissionTime))
    }

    // Staggered Animation States (approx 1 - 1.2s total)
    val checkmarkScale = remember { Animatable(0.4f) }
    val checkmarkAlpha = remember { Animatable(0f) }

    val confettiScale = remember { Animatable(0.2f) }
    val confettiAlpha = remember { Animatable(0f) }

    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(35f) }

    val subtitleAlpha = remember { Animatable(0f) }

    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(45f) }

    val buttonAlpha = remember { Animatable(0f) }
    val buttonOffsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        // 1. Checkmark & Rings appearance
        launch {
            checkmarkAlpha.animateTo(1f, tween(300))
        }
        launch {
            checkmarkScale.animateTo(1.15f, tween(400, easing = FastOutSlowInEasing))
            checkmarkScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }

        // 2. Confetti elements appearance
        launch {
            delay(200)
            confettiAlpha.animateTo(1f, tween(300))
            confettiScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }

        // 3. "Report Submitted!" title slide & fade up
        launch {
            delay(300)
            titleAlpha.animateTo(1f, tween(400))
        }
        launch {
            delay(300)
            titleOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        }

        // 4. Thank you subtitle fade in
        launch {
            delay(450)
            subtitleAlpha.animateTo(1f, tween(400))
        }

        // 5. Report details card slide & fade up
        launch {
            delay(580)
            cardAlpha.animateTo(1f, tween(450))
        }
        launch {
            delay(580)
            cardOffsetY.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }

        // 6. "Back to Home" button slide & fade up
        launch {
            delay(780)
            buttonAlpha.animateTo(1f, tween(400))
        }
        launch {
            delay(780)
            buttonOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2CB856), // Vibrant modern top green
                        Color(0xFF0F6E31)  // Rich deep bottom green
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Checkmark Circle + Concentric Rings + Celebratory Confetti
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Concentric Micro-Rings
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    )

                    // Staggered Confetti Particles floating around
                    val confettiList = listOf(
                        Triple(Offset(-80f, -80f), -25f, Color.White),
                        Triple(Offset(85f, -70f), 30f, Color(0xFFA7F3D0)),
                        Triple(Offset(-100f, 20f), 45f, Color(0xFF86EFAC)),
                        Triple(Offset(95f, 30f), -40f, Color.White),
                        Triple(Offset(-60f, 95f), 15f, Color(0xFFA7F3D0)),
                        Triple(Offset(60f, 90f), -20f, Color.White),
                        Triple(Offset(0f, -105f), 0f, Color(0xFF86EFAC)),
                        Triple(Offset(-35f, -100f), 50f, Color.White)
                    )

                    confettiList.forEach { (offset, rot, color) ->
                        Box(
                            modifier = Modifier
                                .offset(x = offset.x.dp, y = offset.y.dp)
                                .graphicsLayer {
                                    scaleX = confettiScale.value
                                    scaleY = confettiScale.value
                                    alpha = confettiAlpha.value
                                    rotationZ = rot
                                }
                                .size(width = 12.dp, height = 6.dp)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                    }

                    // Main Circular Checkmark Badge
                    Box(
                        modifier = Modifier
                            .size(115.dp)
                            .graphicsLayer {
                                scaleX = checkmarkScale.value
                                scaleY = checkmarkScale.value
                                alpha = checkmarkAlpha.value
                            }
                            .background(Color.White.copy(alpha = 0.22f), CircleShape)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Success Checkmark",
                            tint = Color.White,
                            modifier = Modifier.size(62.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title: "Report Submitted!"
                Text(
                    text = "Report\nSubmitted!",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffsetY.value * density
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle: "Thank you for helping make your city better."
                Text(
                    text = "Thank you for helping make\nyour city better.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = subtitleAlpha.value
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Translucent Report Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = cardAlpha.value
                            translationY = cardOffsetY.value * density
                        },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // 1. Issue Category
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Issue Category",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = aiResult.category.ifBlank { "Road Damage" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )

                        // 2. Summary
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Summary",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = aiResult.summary.ifBlank { "Potholes with Standing Water on Road" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )

                        // 3. Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Location",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = locationName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                if (lat != null && lng != null) {
                                    Text(
                                        text = "GPS: %.5f, %.5f".format(lat, lng),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )

                        // 4. Submitted On
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Event,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Submitted On",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = formattedTime,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large White Rounded Button: "Back to Home"
            Button(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .graphicsLayer {
                        alpha = buttonAlpha.value
                        translationY = buttonOffsetY.value * density
                    },
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F6E31)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = Color(0xFF0F6E31),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Back to Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
