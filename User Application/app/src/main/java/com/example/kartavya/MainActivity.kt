package com.example.kartavya

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.kartavya.model.IssueStatus
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.kartavya.data.AuthRepository
import com.example.kartavya.data.IssueRepository
import com.example.kartavya.data.SupabaseStorageRepository
import com.example.kartavya.data.UserRepository
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ──────────────────────────────────────────────────────
// Color Palette matching the warm aesthetic
// ──────────────────────────────────────────────────────
val BgCream = Color(0xFFFDFBF7)
val PrimaryYellow = Color(0xFFFFD447)
val TextDark = Color(0xFF1C1C1C)
val TextGray = Color(0xFF888888)
val LightGrayBorder = Color(0xFFEEEEEE)
val CardBg = Color.White
val GreenStatus = Color(0xFF00C853)
val OrangeStatus = Color(0xFFFFB300)

/**
 * Smart helper to resolve image models for Coil AsyncImage.
 * Maps any issue URL, ID, or title/category to local complaint drawables (img_001..img_010)
 * even when offline or backend is unreachable.
 */
fun getImageModel(
    imageStr: String?,
    issueId: String = "",
    title: String = "",
    category: String = "",
    context: android.content.Context
): Any {
    // If it's explicitly the resolved photo requested
    if (imageStr == "img_008_resolved" || imageStr == "008-resolved") {
        val resolvedId = context.resources.getIdentifier("img_008_resolved", "drawable", context.packageName)
        if (resolvedId != 0) return resolvedId
    }

    // 1. Try matching imageStr directly if it's a valid local drawable
    if (!imageStr.isNullOrBlank()) {
        val trimmed = imageStr.trim()
        val cleanName = trimmed
            .replace("R.drawable.", "")
            .removeSuffix(".png")
            .removeSuffix(".jpg")
            .replace("-", "_")

        val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
        if (resId != 0) return resId

        val prefixedId = context.resources.getIdentifier("img_$cleanName", "drawable", context.packageName)
        if (prefixedId != 0) return prefixedId

        // Check if string contains digits e.g. "007", "demo-issue-007", "001"
        val digits = trimmed.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            val numInt = digits.toIntOrNull()
            if (numInt != null && numInt > 0) {
                val index = ((numInt - 1) % 10) + 1
                val numFormatted = "img_%03d".format(index)
                val numResId = context.resources.getIdentifier(numFormatted, "drawable", context.packageName)
                if (numResId != 0) return numResId
            }
        }
    }

    // 2. Extract digits from issueId (e.g., "demo-issue-007" -> 7 -> "img_007", "JH-9821" -> 21 -> "img_001")
    if (issueId.isNotBlank()) {
        val digits = issueId.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            val numInt = digits.toIntOrNull()
            if (numInt != null && numInt > 0) {
                val index = ((numInt - 1) % 10) + 1
                val numFormatted = "img_%03d".format(index)
                val numResId = context.resources.getIdentifier(numFormatted, "drawable", context.packageName)
                if (numResId != 0) return numResId
            }
        }
    }

    // 3. Fallback based on title and category keywords
    val text = (title + " " + category).lowercase()
    val targetDrawable = when {
        text.contains("barrier") || text.contains("pothole") || text.contains("road") || text.contains("crack") || text.contains("surface") -> "img_001"
        text.contains("light") || text.contains("wire") || text.contains("lamp") || text.contains("electric") -> "img_002"
        text.contains("waste") || text.contains("garbage") || text.contains("bin") || text.contains("dump") -> "img_003"
        text.contains("water") || text.contains("pipe") || text.contains("leak") -> "img_004"
        text.contains("sewer") || text.contains("manhole") || text.contains("sanitation") -> "img_005"
        text.contains("traffic") || text.contains("signal") -> "img_006"
        text.contains("tree") || text.contains("power") -> "img_007"
        text.contains("sewage") || text.contains("choke") || text.contains("drain") -> "img_008"
        text.contains("park") || text.contains("bench") || text.contains("fence") -> "img_009"
        else -> "img_010"
    }

    val resId = context.resources.getIdentifier(targetDrawable, "drawable", context.packageName)
    if (resId != 0) return resId

    return context.resources.getIdentifier("img_001", "drawable", context.packageName)
}

// ──────────────────────────────────────────────────────
// Local UI data classes (mock feed)
// ──────────────────────────────────────────────────────
data class Issue(
    val id: Int,
    val title: String,
    val location: String,
    val time: String,
    val category: String,
    val gradientColors: List<Color>
)

data class Feature(
    val title: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

val MOCK_FEED = listOf(
    Issue(1, "Pothole repaired", "Connaught Place", "11:45 PM • Aug 04", "Infrastructure", listOf(Color(0xFFFFB7B2), Color(0xFFE2858E))),
    Issue(2, "Streetlight restored", "Hauz Khas", "09:20 AM • Aug 03", "Utilities", listOf(Color(0xFFA0E4CB), Color(0xFF59C1BD))),
    Issue(3, "Garbage cleared", "Lajpat Nagar", "02:15 PM • Aug 01", "Sanitation", listOf(Color(0xFFFDF2B3), Color(0xFFF3C892)))
)

val MOCK_FEATURES = listOf(
    Feature("Cloud Sync", Icons.Rounded.Cloud, Color(0xFFEBF4FF), Color(0xFF3B82F6)),
    Feature("Fast Track", Icons.Rounded.FlashOn, Color(0xFFFFF0E5), Color(0xFFF97316)),
    Feature("Verified", Icons.Rounded.Security, Color(0xFFE6F9F0), Color(0xFF22C55E)),
    Feature("Heatmaps", Icons.Rounded.Map, Color(0xFFF3E8FF), Color(0xFFA855F7))
)

// ──────────────────────────────────────────────────────
// Location helper
// ──────────────────────────────────────────────────────
fun fetchDeviceLocation(
    context: Context,
    onLocationFetched: (locationName: String, lat: Double?, lng: Double?) -> Unit
) {
    val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    if (finePerm != PackageManager.PERMISSION_GRANTED && coarsePerm != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    try {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        var bestLocation: Location? = null
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                    bestLocation = loc
                }
            }
        }

        if (bestLocation != null) {
            val lat = bestLocation.latitude
            val lng = bestLocation.longitude
            val geocoder = Geocoder(context, Locale.getDefault())
            var addressText = "Lat: %.4f, Lng: %.4f".format(lat, lng)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                            val subAdmin = addr.subAdminArea ?: addr.adminArea ?: ""
                            val formatted = listOf(thoroughfare, subAdmin).filter { it.isNotBlank() }.joinToString(", ")
                            if (formatted.isNotBlank()) addressText = formatted
                        }
                        Handler(Looper.getMainLooper()).post {
                            onLocationFetched(addressText, lat, lng)
                        }
                    }
                    return
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                        val subAdmin = addr.subAdminArea ?: addr.adminArea ?: ""
                        val formatted = listOf(thoroughfare, subAdmin).filter { it.isNotBlank() }.joinToString(", ")
                        if (formatted.isNotBlank()) addressText = formatted
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onLocationFetched(addressText, lat, lng)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

// ──────────────────────────────────────────────────────
// Activity
// ──────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CivicApp()
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Root Composable — Auth State Management
// ──────────────────────────────────────────────────────
@Composable
fun CivicApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Auth check: null = still loading, true/false = resolved
    var authChecked by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(AuthRepository.currentUser) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // ── Firebase Auth state listener ──
    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            authChecked = true
        }
        AuthRepository.addAuthStateListener(authListener)
        onDispose { AuthRepository.removeAuthStateListener(authListener) }
    }

    // ── Sync Firestore profile when user state changes ──
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            try {
                userProfile = UserRepository.getOrCreateProfile(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            userProfile = null
        }
    }

    // ── Live observation of user profile from Firestore ──
    DisposableEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            val listener = UserRepository.observeProfile(uid) { profile ->
                if (profile != null) userProfile = profile
            }
            onDispose { listener.remove() }
        } else {
            onDispose { }
        }
    }

    // ── Google Sign-In via Credential Manager ──
    val onGoogleLoginClick: () -> Unit = {
        isSigningIn = true
        authError = null
        coroutineScope.launch {
            val result = AuthRepository.signInWithGoogle(context)
            result.onSuccess { user ->
                currentUser = user
                isSigningIn = false
                Toast.makeText(context, "Welcome, ${user.displayName ?: "Citizen"}!", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                authError = err.message ?: "Sign-in failed"
                isSigningIn = false
            }
        }
    }

    var activeTab by remember { mutableStateOf("home") }
    var isReporting by remember { mutableStateOf(false) }
    var subScreenStack by remember { mutableStateOf(listOf<String>()) } // Navigation stack for subscreens
    var selectedIssueForDetails by remember { mutableStateOf<CivicIssue?>(null) }

    val currentSubScreen = subScreenStack.lastOrNull()

    val pushSubScreen: (String) -> Unit = { screen ->
        if (subScreenStack.lastOrNull() != screen) {
            subScreenStack = subScreenStack + screen
        }
    }

    val popSubScreen: () -> Unit = {
        if (subScreenStack.isNotEmpty()) {
            subScreenStack = subScreenStack.dropLast(1)
        }
    }

    val clearSubScreens: () -> Unit = {
        subScreenStack = emptyList()
    }

    // ── System Back Button & Swipe Back Gesture Navigation Handler ──
    val shouldInterceptBack = isReporting || subScreenStack.isNotEmpty() || activeTab != "home"
    BackHandler(enabled = shouldInterceptBack) {
        when {
            isReporting -> {
                isReporting = false
            }
            subScreenStack.isNotEmpty() -> {
                popSubScreen()
            }
            activeTab != "home" -> {
                activeTab = "home"
            }
        }
    }

    // Notifications state (starts empty)
    var notificationsList by remember { mutableStateOf(emptyList<CivicNotification>()) }
    val unreadNotificationsCount = remember(notificationsList) { notificationsList.count { !it.isRead } }

    // ── Logout ──
    val onLogoutClick: () -> Unit = {
        coroutineScope.launch {
            AuthRepository.signOut(context)
            currentUser = null
            userProfile = null
            activeTab = "home"
            isReporting = false
            clearSubScreens()
            Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Decide what to show ──
    // Still loading auth state → show nothing (or a splash)
    // Not logged in → WelcomeScreen
    // Logged in → Main App
    if (!authChecked) {
        // Brief splash while Firebase checks auth
        Box(
            modifier = Modifier.fillMaxSize().background(BgCream),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryYellow, strokeWidth = 4.dp)
        }
    } else if (currentUser == null) {
        // ── Welcome / Login Screen ──
        WelcomeScreen(
            isSigningIn = isSigningIn,
            authError = authError,
            onGoogleLoginClick = onGoogleLoginClick
        )
    } else {
        // ── Main App with Top Status Bar Safe Area ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
        ) {
            // Clean WHITE/OPAQUE safe area behind the Android status bar
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color.White)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (!isReporting && currentSubScreen == null) 64.dp else 0.dp)
                ) {
                    if (isReporting) {
                        ReportIssueFlow(
                            currentUser = currentUser,
                            userProfile = userProfile,
                            onCancel = { isReporting = false }
                        )
                    } else {
                        when (currentSubScreen) {
                            "notifications" -> NotificationsScreen(
                                notifications = notificationsList,
                                onBack = { popSubScreen() },
                                onMarkAllRead = {
                                    notificationsList = notificationsList.map { it.copy(isRead = true) }
                                },
                                onClearAll = {
                                    notificationsList = emptyList()
                                },
                                onNotificationClick = { notif ->
                                    notificationsList = notificationsList.map {
                                        if (it.id == notif.id) it.copy(isRead = true) else it
                                    }
                                    selectedIssueForDetails = notif.relatedIssue
                                    pushSubScreen("report_details")
                                }
                            )
                            "report_details" -> ReportDetailsScreen(
                                issue = selectedIssueForDetails,
                                onBack = { popSubScreen() }
                            )
                            "report_history" -> ReportHistoryScreen(
                                currentUser = currentUser,
                                userProfile = userProfile,
                                onBack = { popSubScreen() },
                                onIssueClick = { issue ->
                                    selectedIssueForDetails = issue
                                    pushSubScreen("report_details")
                                }
                            )
                            else -> Crossfade(targetState = activeTab) { tab ->
                                when (tab) {
                                    "home" -> HomeFeed(
                                        currentUser = currentUser,
                                        userProfile = userProfile,
                                        onReportClick = { issue ->
                                            selectedIssueForDetails = issue
                                            pushSubScreen("report_details")
                                        }
                                    )
                                    "profile" -> UserProfileScreen(
                                        currentUser = currentUser,
                                        userProfile = userProfile,
                                        unreadCount = unreadNotificationsCount,
                                        onLogoutClick = onLogoutClick,
                                        onNotificationClick = { pushSubScreen("notifications") },
                                        onReportClick = { issue ->
                                            selectedIssueForDetails = issue
                                            pushSubScreen("report_details")
                                        },
                                        onViewHistoryClick = { pushSubScreen("report_history") }
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isReporting && currentSubScreen == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        BottomNavBar(
                            activeTab = activeTab,
                            onTabSelected = { tab ->
                                activeTab = tab
                                clearSubScreens()
                            },
                            onReportClick = { isReporting = true }
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Welcome Screen — Shown when user is not logged in
// ──────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(
    isSigningIn: Boolean,
    authError: String?,
    onGoogleLoginClick: () -> Unit
) {
    // Animated decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgCream, Color(0xFFFFF8E7), BgCream)
                )
            )
    ) {
        // Decorative floating circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-40).dp, y = (80 + floatAnim * 20).dp)
                .alpha(0.08f)
                .background(PrimaryYellow, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (160 - floatAnim * 15).dp)
                .alpha(0.06f)
                .background(Color(0xFFFFB300), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp, y = (40 + floatAnim * 10).dp)
                .alpha(0.05f)
                .background(GreenStatus, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // App Icon
            Box(
                modifier = Modifier
                    .size((96 * pulseAnim).dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryYellow, Color(0xFFFFB300))
                        ),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = "Kartavya",
                    tint = TextDark,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // App Title
            Text(
                text = "KARTAVYA",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 14.dp)
                        .background(PrimaryYellow.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Text(
                    text = "Civic Duty, Simplified.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Report civic issues, track resolutions,\nand earn points as a Civic Guardian.",
                fontSize = 15.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // Feature highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WelcomeFeature(Icons.Rounded.CameraAlt, "Snap", Color(0xFFFFF0E5), Color(0xFFF97316))
                WelcomeFeature(Icons.Rounded.LocationOn, "Locate", Color(0xFFE6F9F0), Color(0xFF22C55E))
                WelcomeFeature(Icons.Rounded.Speed, "Track", Color(0xFFEBF4FF), Color(0xFF3B82F6))
                WelcomeFeature(Icons.Rounded.EmojiEvents, "Earn", Color(0xFFFFF9E6), Color(0xFFFFB300))
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Auth Error message
            if (!authError.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = authError,
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Google Sign-In Button
            Button(
                onClick = onGoogleLoginClick,
                enabled = !isSigningIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextDark,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        color = PrimaryYellow,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Signing in...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "Google",
                        tint = PrimaryYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtle footer
            Text(
                text = "By continuing, you agree to help\nmake your city a better place ✨",
                fontSize = 12.sp,
                color = TextGray.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WelcomeFeature(icon: ImageVector, label: String, bgColor: Color, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(bgColor, RoundedCornerShape(18.dp))
                .border(1.dp, LightGrayBorder, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

// ──────────────────────────────────────────────────────
// Bottom Navigation Bar
// ──────────────────────────────────────────────────────
// ──────────────────────────────────────────────────────
// Bottom Navigation Bar
// ──────────────────────────────────────────────────────
@Composable
fun BottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onReportClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Full width navigation bar - flat bottom, touching edges
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(0.5.dp, LightGrayBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Now
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clickable { onTabSelected("home") }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Now",
                        tint = if (activeTab == "home") TextDark else TextGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "home") TextDark else TextGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Tab 2: Profile
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clickable { onTabSelected("profile") }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = if (activeTab == "profile") TextDark else TextGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Profile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "profile") TextDark else TextGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Floating Central "+" Button - Overlapping above top edge of nav bar
        Box(
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(56.dp)
                .background(PrimaryYellow, CircleShape)
                .border(3.dp, BgCream, CircleShape)
                .clickable { onReportClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Report Issue",
                tint = TextDark,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────
// Demo Civic Issue Data Model & Sample Feed Items
// ──────────────────────────────────────────────────────
data class DemoCivicIssue(
    val id: String,
    val category: String,
    val title: String,
    val location: String,
    val reporter: String,
    val status: com.example.kartavya.model.IssueStatus,
    val imageUrl: String,
    val initialUpvotes: Int
)

val ALL_DEMO_ISSUES = listOf(
    CivicIssue(
        issueId = "JH-9821",
        userId = "user_001",
        reporterName = "Demo User 001",
        title = "Large Pothole on Main Road",
        description = "Deep asphalt crater on Ward 29 Kanke Road causing severe traffic bottleneck and vehicle damage.",
        category = "Road Damage",
        status = IssueStatus.REPORTED.name,
        address = "Ward 29, Kanke Road, Ranchi",
        priority = "High",
        imageUrls = listOf("img_001"),
        upvotes = 15,
        routingTo = "Ranchi Municipal Corporation"
    ),
    CivicIssue(
        issueId = "JH-9820",
        userId = "user_002",
        reporterName = "Demo User 002",
        title = "Streetlight Inoperative & Wire Sagging",
        description = "Streetlight pole completely unlit for 3 consecutive nights with loose sagging electrical cable in residential lane.",
        category = "Streetlighting",
        status = IssueStatus.IN_PROGRESS.name,
        address = "Ward 12, Harmu Housing Colony, Ranchi",
        priority = "Moderate",
        imageUrls = listOf("img_002"),
        upvotes = 8,
        routingTo = "Jharkhand Bijli Vitran Nigam"
    ),
    CivicIssue(
        issueId = "JH-9819",
        userId = "user_003",
        reporterName = "Demo User 003",
        title = "Garbage Dump Overflow on Main Thoroughfare",
        description = "Commercial waste bin spilling onto pedestrian walkway near Lalpur vegetable market entrance.",
        category = "Cleanliness",
        status = IssueStatus.ACKNOWLEDGED.name,
        address = "Ward 31, Lalpur, Ranchi",
        priority = "High",
        imageUrls = listOf("img_003"),
        upvotes = 6,
        routingTo = "Ranchi Urban Sanitation Dept"
    ),
    CivicIssue(
        issueId = "JH-9818",
        userId = "user_004",
        reporterName = "Demo User 004",
        title = "Drinking Water Pipeline Leakage",
        description = "Clean municipal water main pipe ruptured, wasting hundreds of liters daily on public road surface.",
        category = "Water Supply",
        status = IssueStatus.IN_PROGRESS.name,
        address = "Ward 18, Doranda, Ranchi",
        priority = "High",
        imageUrls = listOf("img_004"),
        upvotes = 11,
        routingTo = "Water Works Department"
    ),
    CivicIssue(
        issueId = "JH-9817",
        userId = "user_005",
        reporterName = "Demo User 005",
        title = "Open Sewer Manhole Hazard",
        description = "Missing heavy concrete manhole cover on main sidewalk posing severe fall hazard for pedestrians.",
        category = "Sanitation",
        status = IssueStatus.REPORTED.name,
        address = "Ward 08, Main Road, Dhanbad",
        priority = "Critical",
        imageUrls = listOf("img_005"),
        upvotes = 22,
        routingTo = "Dhanbad Municipal Corporation"
    ),
    CivicIssue(
        issueId = "JH-9816",
        userId = "user_006",
        reporterName = "Demo User 006",
        title = "Broken Traffic Signal Light",
        description = "Intersection traffic signals non-functional during peak evening hours, causing severe traffic gridlock.",
        category = "Traffic & Transport",
        status = IssueStatus.ACKNOWLEDGED.name,
        address = "Bistupur Crossing, Jamshedpur",
        priority = "High",
        imageUrls = listOf("img_006"),
        upvotes = 14,
        routingTo = "Traffic Police & Urban Transport"
    ),
    CivicIssue(
        issueId = "JH-9815",
        userId = "user_007",
        reporterName = "Demo User 007",
        title = "Overgrown Trees Blocking Power Lines",
        description = "Heavy tree branches hanging dangerously over high voltage electrical wires near Sector 4 residential zone.",
        category = "Electricity",
        status = IssueStatus.IN_PROGRESS.name,
        address = "Sector 4, Bokaro Steel City",
        priority = "Moderate",
        imageUrls = listOf("img_007"),
        upvotes = 9,
        routingTo = "Bokaro Electricity Supply"
    ),
    CivicIssue(
        issueId = "JH-9708",
        userId = "user_008",
        reporterName = "Demo User 008",
        title = "Main Sewage Line Backflow & Choke",
        description = "Severe sewage line blockage successfully cleared and pipeline fully restored by municipal drainage team.",
        category = "Sanitation",
        status = IssueStatus.RESOLVED.name,
        address = "Harmu Housing Colony, Block C, Ranchi",
        priority = "Critical",
        imageUrls = listOf("img_008"),
        upvotes = 34,
        routingTo = "Ranchi Municipal Drainage Dept"
    ),
    CivicIssue(
        issueId = "JH-9813",
        userId = "user_009",
        reporterName = "Demo User 009",
        title = "Damaged Public Park Bench & Fencing",
        description = "Vandalized perimeter fencing and broken public seating in community park requiring municipal repair.",
        category = "Public Amenities",
        status = IssueStatus.REPORTED.name,
        address = "Morabadi Ground Park, Ranchi",
        priority = "Low",
        imageUrls = listOf("img_009"),
        upvotes = 5,
        routingTo = "Parks & Recreation Department"
    ),
    CivicIssue(
        issueId = "JH-9812",
        userId = "user_010",
        reporterName = "Demo User 010",
        title = "Stagnant Stormwater Accumulation",
        description = "Rainwater standing for over 48 hours in low-lying residential sector raising mosquito and health concerns.",
        category = "Drainage",
        status = IssueStatus.IN_PROGRESS.name,
        address = "Chutia Ring Road, Ranchi",
        priority = "Moderate",
        imageUrls = listOf("img_010"),
        upvotes = 12,
        routingTo = "Ranchi Drainage Authority"
    )
)

val SMALL_DEMO_FALLBACK_ISSUES = ALL_DEMO_ISSUES

// ──────────────────────────────────────────────────────
// Home Feed
// ──────────────────────────────────────────────────────
@Composable
fun HomeFeed(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    onReportClick: (CivicIssue) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Dynamic date & time
    val currentDate = LocalDate.now()
    val day = currentDate.dayOfMonth.toString().padStart(2, '0')
    val month = currentDate.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
    val year = currentDate.year.toString()
    val timeStr = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
    }

    var isLoading by remember { mutableStateOf(true) }
    var liveReports by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }

    DisposableEffect(Unit) {
        val listener = IssueRepository.observeRecentIssues { reports ->
            liveReports = reports
            isLoading = false
        }
        onDispose { listener.remove() }
    }

    // Dynamic User Greeting
    val greetingName = currentUser?.displayName?.split(" ")?.firstOrNull()
        ?: userProfile?.name?.split(" ")?.firstOrNull()
        ?: "Citizen"

    // Real User Statistics (never inflated by demo issues)
    val currentUid = currentUser?.uid ?: ""
    val userMyReports = remember(liveReports, currentUid) {
        if (currentUid.isNotBlank()) liveReports.filter { it.userId == currentUid } else emptyList()
    }

    val reportsFiledCount = (userProfile?.reportsFiled ?: userMyReports.size).toString()
    val issuesFixedCount = (userProfile?.issuesFixed ?: userMyReports.count { it.statusEnum() == com.example.kartavya.model.IssueStatus.RESOLVED }).toString()
    val rankStr = userProfile?.rank ?: "Newcomer"

    // Primary feed source is Firestore issues. Small demo fallback list used ONLY if Firestore returns empty.
    val displayReports = remember(liveReports, isLoading) {
        if (liveReports.isNotEmpty()) {
            liveReports
        } else {
            ALL_DEMO_ISSUES
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Section
        Text(
            text = "Hello, $greetingName!",
            fontSize = 15.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        ) {
            Text(
                text = "$day $month",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                lineHeight = 42.sp
            )
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, bottom = 6.dp)
                    .height(36.dp)
                    .width(2.dp)
                    .background(LightGrayBorder)
            )
            Column(modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)) {
                Text(text = timeStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = year, fontSize = 12.sp, color = TextGray, letterSpacing = 2.sp)
            }
        }

        // Stats Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatBubble(value = reportsFiledCount, label = "Reports\nFiled")
            StatBubble(value = issuesFixedCount, label = "Issues\nFixed", highlight = true)
            StatBubble(value = rankStr, label = "Civic\nRank", icon = Icons.Rounded.Star)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Civic Feed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            if (liveReports.isNotEmpty()) {
                Text(
                    text = "Live Updates •",
                    fontSize = 12.sp,
                    color = GreenStatus,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FEED STATES
        if (isLoading) {
            // Loading skeleton state
            repeat(2) {
                CardSkeleton()
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (displayReports.isEmpty()) {
            // Empty state
            EmptyFeedState()
        } else {
            // Render Firestore issues (or small fallback list if Firestore returns 0 items)
            displayReports.forEach { report ->
                LiveFeedCard(
                    report = report,
                    currentUid = currentUid,
                    onClick = { onReportClick(report) },
                    onUpvoteToggle = {
                        val uidToUse = currentUid.ifBlank { "demo_user" }
                        coroutineScope.launch {
                            IssueRepository.toggleUpvote(report.issueId, uidToUse, fallbackIssue = report)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun CardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
        )
    }
}

@Composable
fun EmptyFeedState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Inbox,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Civic Issues Nearby",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark
            )
            Text(
                text = "Be the first to report a civic issue in your area!",
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StatBubble(value: String, label: String, highlight: Boolean = false, icon: ImageVector? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(CardBg, RoundedCornerShape(22.dp))
                .border(1.dp, LightGrayBorder, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (highlight) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp)
                        .border(2.dp, GreenStatus.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = PrimaryYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = value,
                    fontSize = if (value.length > 5) 12.sp else 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = label.uppercase(Locale.getDefault()),
            fontSize = 10.sp,
            color = TextGray,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            lineHeight = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

fun getCategoryHeaderGradient(category: String, title: String): Brush {
    val key = (category + " " + title).lowercase(Locale.getDefault())
    return when {
        key.contains("road") || key.contains("pothole") -> Brush.linearGradient(
            listOf(Color(0xFF854D0E), Color(0xFF3B1F04))
        )
        key.contains("light") || key.contains("electric") -> Brush.linearGradient(
            listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
        )
        key.contains("clean") || key.contains("waste") || key.contains("garbage") -> Brush.linearGradient(
            listOf(Color(0xFF047857), Color(0xFF064E3B))
        )
        key.contains("water &") || key.contains("water supply") || key.contains("pipe") || key.contains("leak") -> Brush.linearGradient(
            listOf(Color(0xFF0284C7), Color(0xFF0C4A6E))
        )
        key.contains("drain") || key.contains("waterlog") -> Brush.linearGradient(
            listOf(Color(0xFF0D9488), Color(0xFF134E4A))
        )
        key.contains("safety") || key.contains("hazard") || key.contains("tree") || key.contains("branch") -> Brush.linearGradient(
            listOf(Color(0xFF991B1B), Color(0xFF450A0A))
        )
        key.contains("traffic") || key.contains("signal") -> Brush.linearGradient(
            listOf(Color(0xFF6D28D9), Color(0xFF312E81))
        )
        key.contains("infra") || key.contains("divider") -> Brush.linearGradient(
            listOf(Color(0xFF475569), Color(0xFF1E293B))
        )
        key.contains("pedestrian") || key.contains("footpath") || key.contains("pavement") -> Brush.linearGradient(
            listOf(Color(0xFFB45309), Color(0xFF451A03))
        )
        key.contains("sanitation") || key.contains("toilet") -> Brush.linearGradient(
            listOf(Color(0xFF0891B2), Color(0xFF164E63))
        )
        key.contains("park") || key.contains("rec") -> Brush.linearGradient(
            listOf(Color(0xFF15803D), Color(0xFF14532D))
        )
        key.contains("transport") || key.contains("bus") -> Brush.linearGradient(
            listOf(Color(0xFF1D4ED8), Color(0xFF1E1B4B))
        )
        key.contains("obstruction") || key.contains("construction") -> Brush.linearGradient(
            listOf(Color(0xFFC2410C), Color(0xFF431407))
        )
        else -> Brush.linearGradient(
            listOf(Color(0xFF334155), Color(0xFF0F172A))
        )
    }
}

// ──────────────────────────────────────────────────────
// Live Feed Card (Firestore issues)
// ──────────────────────────────────────────────────────
@Composable
fun LiveFeedCard(
    report: CivicIssue,
    currentUid: String,
    onClick: () -> Unit = {},
    onUpvoteToggle: () -> Unit = {}
) {
    val context = LocalContext.current
    val effectiveUid = currentUid.ifBlank { "demo_user" }

    var isUpvoted by remember(report.issueId, report.upvotedBy, effectiveUid) {
        mutableStateOf(report.upvotedBy.contains(effectiveUid))
    }
    var upvoteCount by remember(report.issueId, report.upvotes) {
        mutableIntStateOf(report.upvotes)
    }

    val isMine = currentUid.isNotBlank() && report.userId == currentUid
    val firstImage = report.imageUrls.firstOrNull()

    val statusColor = when (report.statusEnum()) {
        com.example.kartavya.model.IssueStatus.RESOLVED -> GreenStatus
        com.example.kartavya.model.IssueStatus.REJECTED -> Color(0xFFD32F2F)
        com.example.kartavya.model.IssueStatus.IN_PROGRESS -> Color(0xFF3B82F6)
        else -> OrangeStatus
    }

    val statusBg = when (report.statusEnum()) {
        com.example.kartavya.model.IssueStatus.RESOLVED -> Color(0xFFE6F9F0)
        com.example.kartavya.model.IssueStatus.REJECTED -> Color(0xFFFFEBEE)
        com.example.kartavya.model.IssueStatus.IN_PROGRESS -> Color(0xFFEBF4FF)
        else -> Color(0xFFFFF8E7)
    }

    val headerGradient = getCategoryHeaderGradient(report.category, report.title)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column {
            // Top styled header area with thumbnail & category-specific gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerGradient)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = report.category.ifBlank { "Civic Issue" },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isMine) "• By You" else "• By ${report.reporterName.ifBlank { "Citizen" }}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = report.title.ifBlank { "${report.category} reported" },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 20.sp
                        )
                    }

                    // Right Thumbnail Image
                    Box(
                        modifier = Modifier
                            .size(width = 84.dp, height = 64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageModel = getImageModel(firstImage, report.issueId, report.title, report.category, context)
                        AsyncImage(
                            model = imageModel,
                            contentDescription = report.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Bottom Footer Row (Location, Status & Upvotes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.address.ifBlank { "Location Logged" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = report.statusEnum().displayLabel(),
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(
                                if (isUpvoted) PrimaryYellow.copy(alpha = 0.3f) else BgCream,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isUpvoted) PrimaryYellow else LightGrayBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isUpvoted = !isUpvoted
                                upvoteCount = if (isUpvoted) upvoteCount + 1 else (upvoteCount - 1).coerceAtLeast(0)
                                onUpvoteToggle()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (isUpvoted) TextDark else TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = upvoteCount.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Demo Feed Card
// ──────────────────────────────────────────────────────
@Composable
fun DemoFeedCard(demo: DemoCivicIssue) {
    val context = LocalContext.current
    var isUpvoted by remember { mutableStateOf(false) }
    var upvoteCount by remember { mutableIntStateOf(demo.initialUpvotes) }

    val statusColor = when (demo.status) {
        com.example.kartavya.model.IssueStatus.RESOLVED -> GreenStatus
        com.example.kartavya.model.IssueStatus.REJECTED -> Color(0xFFD32F2F)
        com.example.kartavya.model.IssueStatus.IN_PROGRESS -> Color(0xFF3B82F6)
        else -> OrangeStatus
    }

    val statusBg = when (demo.status) {
        com.example.kartavya.model.IssueStatus.RESOLVED -> Color(0xFFE6F9F0)
        com.example.kartavya.model.IssueStatus.REJECTED -> Color(0xFFFFEBEE)
        com.example.kartavya.model.IssueStatus.IN_PROGRESS -> Color(0xFFEBF4FF)
        else -> Color(0xFFFFF8E7)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Toast.makeText(context, "Sample Issue: ${demo.title}", Toast.LENGTH_SHORT).show()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column {
            // Top styled header area with thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF34495E), Color(0xFF2C3E50))
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = demo.category,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "• ${demo.reporter}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = demo.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 20.sp
                        )
                    }

                    // Right Thumbnail Image
                    Box(
                        modifier = Modifier
                            .size(width = 84.dp, height = 64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = getImageModel(demo.imageUrl, demo.id, demo.title, demo.category, context),
                            contentDescription = demo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Bottom Footer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = demo.location,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = demo.status.displayLabel(),
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(
                                if (isUpvoted) PrimaryYellow.copy(alpha = 0.3f) else BgCream,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isUpvoted) PrimaryYellow else LightGrayBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isUpvoted = !isUpvoted
                                upvoteCount = if (isUpvoted) upvoteCount + 1 else (upvoteCount - 1).coerceAtLeast(0)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (isUpvoted) TextDark else TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = upvoteCount.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Report Issue Flow (Screen 3A AI Processing, Rejection, Screen 3B Final Report Preview & Success)
// ──────────────────────────────────────────────────────
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
            try {
                context.contentResolver.openInputStream(photoUri)?.use { stream ->
                    bitmapState.value = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            try {
                context.contentResolver.openInputStream(photoUri)?.use { stream ->
                    bitmapState.value = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            try {
                context.contentResolver.openInputStream(photoUri)?.use { stream ->
                    bitmapState.value = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

// ──────────────────────────────────────────────────────
// Data Models & Mock Data for Profile, Notifications & Details
// ──────────────────────────────────────────────────────

enum class NotificationType {
    RESOLVED,
    IN_PROGRESS,
    VERIFIED,
    UPVOTES,
    REJECTED
}

data class CivicNotification(
    val id: String,
    val issueId: String = "",
    val type: NotificationType,
    val title: String,
    val message: String,
    val reportTitle: String,
    val location: String,
    val timestampText: String,
    val isRead: Boolean = false,
    val rejectionReason: String? = null,
    val relatedIssue: CivicIssue? = null
)

// Demo fallback reports (structured specifically for Kothri Kalan, Bhopal)
val DEMO_USER_PROFILE_REPORTS = listOf(
    CivicIssue(
        issueId = "KRTY-2025-05-16-0123",
        userId = "demo_user",
        reporterName = "Abhishek",
        title = "Deep Road Pothole Outside Sector 3 Gate",
        description = "Dangerous asphalt crater outside residential sector gate causing vehicle damage.",
        category = "Road Damage",
        imageUrls = listOf("https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600"),
        latitude = 23.26112,
        longitude = 77.32645,
        address = "Sector 3, Kothri Kalan, Bhopal",
        status = IssueStatus.REPORTED.name,
        routingTo = "PWD Road Cell",
        priority = "Moderate",
        upvotes = 6,
        createdAt = com.google.firebase.Timestamp(1747388100, 0)
    ),
    CivicIssue(
        issueId = "KRTY-2025-05-14-0092",
        userId = "demo_user",
        reporterName = "Abhishek",
        title = "Night Streetlight Dim on Residential Lane 2",
        description = "Light flickering and dim along inner lane causing nighttime safety issue.",
        category = "Streetlighting",
        imageUrls = listOf("https://images.unsplash.com/photo-1509114397022-ed747cca3f65?w=600"),
        latitude = 23.26150,
        longitude = 77.32700,
        address = "Lane 2, Kothri Kalan, Bhopal",
        status = "ACKNOWLEDGED",
        routingTo = "BMC Electrical Dept",
        priority = "Moderate",
        upvotes = 11,
        createdAt = com.google.firebase.Timestamp(1747215300, 0)
    ),
    CivicIssue(
        issueId = "KRTY-2025-05-12-0078",
        userId = "demo_user",
        reporterName = "Abhishek",
        title = "Overflowing Waste Bin Outside Grocery Shop",
        description = "Municipal garbage bin uncollected for 3 days spilling onto market pavement.",
        category = "Cleanliness",
        imageUrls = listOf("https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=600"),
        latitude = 23.26300,
        longitude = 77.32500,
        address = "Kothri Kalan Market, Bhopal",
        status = IssueStatus.IN_PROGRESS.name,
        routingTo = "Sanitation Department",
        createdAt = com.google.firebase.Timestamp(1747215300, 0)
    ),
    CivicIssue(
        issueId = "KRTY-2025-05-12-0012",
        userId = "demo_user",
        reporterName = "Abhishek",
        title = "Water Leakage",
        description = "Major clean water pipe leaking on main road causing road erosion.",
        category = "Water Supply",
        imageUrls = listOf("https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?w=600"),
        latitude = 23.26000,
        longitude = 77.32900,
        address = "Shyamla Hills, Bhopal",
        status = IssueStatus.REPORTED.name,
        routingTo = "Jal Nigam Department",
        priority = "High",
        upvotes = 5,
        createdAt = com.google.firebase.Timestamp(1747042500, 0)
    ),
    CivicIssue(
        issueId = "KRTY-2025-05-10-0005",
        userId = "demo_user",
        reporterName = "Abhishek",
        title = "Illegal Wall Posters",
        description = "Commercial posters plastered across public school entrance wall.",
        category = "Public Nuisance",
        imageUrls = listOf("https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600"),
        latitude = 23.25800,
        longitude = 77.33000,
        address = "TT Nagar, Bhopal",
        status = IssueStatus.REJECTED.name,
        routingTo = "NDMC Enforcement",
        priority = "Low",
        upvotes = 0,
        createdAt = com.google.firebase.Timestamp(1746869700, 0)
    )
)

val INITIAL_DEMO_NOTIFICATIONS = listOf(
    CivicNotification(
        id = "notif_1",
        issueId = "KRTY-2025-05-16-0123",
        type = NotificationType.RESOLVED,
        title = "Issue Resolved",
        message = "Your report \"Potholes with Standing Water\" has been marked resolved.",
        reportTitle = "Potholes with Standing Water",
        location = "Kothri Kalan, Bhopal",
        timestampText = "10 min ago",
        isRead = false,
        relatedIssue = DEMO_USER_PROFILE_REPORTS[0]
    ),
    CivicNotification(
        id = "notif_2",
        issueId = "KRTY-2025-05-15-0089",
        type = NotificationType.IN_PROGRESS,
        title = "Report In Progress",
        message = "Work has started on your report \"Streetlight Issue\".",
        reportTitle = "Streetlight Issue",
        location = "Kothri Kalan, Bhopal",
        timestampText = "2h ago",
        isRead = false,
        relatedIssue = DEMO_USER_PROFILE_REPORTS[1]
    ),
    CivicNotification(
        id = "notif_3",
        issueId = "KRTY-2025-05-14-0045",
        type = NotificationType.VERIFIED,
        title = "Report Verified",
        message = "Your report \"Garbage Overflowing\" passed AI verification and was forwarded.",
        reportTitle = "Garbage Overflowing",
        location = "Kolar Road, Bhopal",
        timestampText = "Yesterday",
        isRead = true,
        relatedIssue = DEMO_USER_PROFILE_REPORTS[2]
    ),
    CivicNotification(
        id = "notif_4",
        issueId = "KRTY-2025-05-16-0123",
        type = NotificationType.UPVOTES,
        title = "Upvotes Received",
        message = "Your report \"Road Damage\" received 8 upvotes from citizens.",
        reportTitle = "Road Damage",
        location = "Kothri Kalan, Bhopal",
        timestampText = "Yesterday",
        isRead = true,
        relatedIssue = DEMO_USER_PROFILE_REPORTS[0]
    ),
    CivicNotification(
        id = "notif_5",
        issueId = "KRTY-2025-05-10-0005",
        type = NotificationType.REJECTED,
        title = "Report Rejected",
        message = "Your report \"Illegal Dumping\" could not be verified by AI. Please submit with clearer evidence.",
        reportTitle = "Illegal Dumping",
        location = "TT Nagar, Bhopal",
        timestampText = "2 days ago",
        isRead = true,
        rejectionReason = "The uploaded photo does not clearly show public infrastructure or a municipal hazard.",
        relatedIssue = DEMO_USER_PROFILE_REPORTS[4]
    )
)

// Status explanation text helper
fun getStatusExplanation(statusStr: String): String = when (statusStr) {
    IssueStatus.REPORTED.name -> "Your report has been received and queued for municipal verification."
    "ACKNOWLEDGED", "ASSIGNED" -> "Assigned to NDMC Municipal Authority."
    IssueStatus.IN_PROGRESS.name -> "Work has started on your report."
    IssueStatus.RESOLVED.name -> "Issue has been resolved."
    IssueStatus.REJECTED.name -> "Image was not verified as a valid civic issue by AI."
    else -> "Your report is currently under processing."
}

// Status Pill Badge helper
@Composable
fun StatusBadgePill(statusStr: String) {
    val (bgColor, textColor, text) = when (statusStr) {
        IssueStatus.REPORTED.name -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), "Reported")
        "ACKNOWLEDGED", "ASSIGNED" -> Triple(Color(0xFFFFF7ED), Color(0xFFEA580C), "Assigned")
        IssueStatus.IN_PROGRESS.name -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "In Progress")
        IssueStatus.RESOLVED.name -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "Resolved")
        IssueStatus.REJECTED.name -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "AI Rejected")
        else -> Triple(BgCream, TextDark, statusStr)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// Real Civic Resolution Status Tracker Component (5-stage timeline)
@Composable
fun StatusProgressTracker(
    currentStatus: String,
    modifier: Modifier = Modifier
) {
    val statusEnum = remember(currentStatus) {
        try { IssueStatus.valueOf(currentStatus) } catch (_: Exception) { IssueStatus.REPORTED }
    }

    val stages = listOf("Reported", "Verified", "Assigned", "In Progress", "Resolved")

    val currentStageIndex = when (statusEnum) {
        IssueStatus.REPORTED -> 0
        IssueStatus.ACKNOWLEDGED -> 2
        IssueStatus.IN_PROGRESS -> 3
        IssueStatus.RESOLVED -> 4
        IssueStatus.REJECTED -> -1
    }

    if (currentStageIndex == -1) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Verification Rejected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { index, stage ->
                    val isCompleted = index < currentStageIndex
                    val isCurrent = index == currentStageIndex

                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 16.dp else 12.dp)
                            .background(
                                color = when {
                                    isCompleted -> Color(0xFF10B981)
                                    isCurrent -> Color(0xFFF59E0B)
                                    else -> Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    isCompleted -> Color(0xFF10B981)
                                    isCurrent -> Color(0xFFF59E0B)
                                    else -> LightGrayBorder
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(8.dp)
                            )
                        } else if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }

                    if (index < stages.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (index < currentStageIndex) Color(0xFF10B981) else LightGrayBorder
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stages.forEachIndexed { index, stage ->
                    val isCurrent = index == currentStageIndex
                    val isCompleted = index < currentStageIndex
                    Text(
                        text = stage,
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent) FontWeight.Black else if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) TextDark else if (isCompleted) Color(0xFF059669) else TextGray,
                        textAlign = when (index) {
                            0 -> TextAlign.Start
                            stages.lastIndex -> TextAlign.End
                            else -> TextAlign.Center
                        }
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Redesigned 3rd Tab — User Profile Screen ("My Kartavya")
// ──────────────────────────────────────────────────────
@Composable
fun UserProfileScreen(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    unreadCount: Int = 0,
    onLogoutClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onReportClick: (CivicIssue) -> Unit,
    onViewHistoryClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val uid = currentUser?.uid ?: ""

    val userName = userProfile?.name ?: currentUser?.displayName ?: "Abhishek"
    val userEmail = userProfile?.email ?: currentUser?.email ?: "abhishekchaudhary1859@gmail.com"
    val userPhotoUrl = userProfile?.photoUrl ?: currentUser?.photoUrl?.toString()

    // Real-time user issues observation with demo fallback
    var realUserIssues by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }
    DisposableEffect(uid) {
        val listener = IssueRepository.observeUserIssues(uid) { issues ->
            realUserIssues = issues
        }
        onDispose { listener?.remove() }
    }

    // Strictly render user's real submitted reports in profile list (empty state card shown when no reports submitted)
    val userReportsList = realUserIssues

    // Calculated personal statistics strictly from actual real user submissions (starts from 0 for new user)
    val reportsSubmittedCount = realUserIssues.size
    val issuesResolvedCount = realUserIssues.count { it.status == IssueStatus.RESOLVED.name }
    val activeReportsCount = realUserIssues.count { it.status in listOf(IssueStatus.REPORTED.name, "ACKNOWLEDGED", IssueStatus.IN_PROGRESS.name) }
    val totalUpvotesCount = realUserIssues.sumOf { it.upvotes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header Bar with Yellow Highlight & Notification Bell
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 18.dp)
                            .background(PrimaryYellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = "My Kartavya",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
                Text(
                    text = "Your contribution to a better city.",
                    fontSize = 14.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Bell Icon Button with Unread Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CardBg, CircleShape)
                    .border(1.dp, LightGrayBorder, CircleShape)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = TextDark,
                    modifier = Modifier.size(22.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(BgCream, CircleShape)
                            .border(2.dp, PrimaryYellow, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = TextDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Text(
                            text = if (userEmail.isNotBlank()) userEmail else "abhishekchaudhary1859@gmail.com",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE6F9F0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🛡 Member since Aug 2026",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Civic Activity Summary: 3 Compact Metric Cards [ Reports Filed ] [ Issues Fixed ] [ Civic Rank ]
                val civicRankStr = userProfile?.rank ?: when {
                    reportsSubmittedCount >= 10 -> "Guardian"
                    reportsSubmittedCount >= 5 -> "Active"
                    else -> "Newcomer"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactStatCard(
                        value = reportsSubmittedCount.toString(),
                        label = "Reports\nFiled",
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )
                    CompactStatCard(
                        value = issuesResolvedCount.toString(),
                        label = "Issues\nFixed",
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    CompactStatCard(
                        value = civicRankStr,
                        label = "Civic\nRank",
                        icon = Icons.Rounded.Star,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current Reports Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Reports",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
            Text(
                text = "View all",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
                modifier = Modifier.clickable { onViewHistoryClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (userReportsList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, LightGrayBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GreenStatus, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Active Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text("All your submitted reports will appear here with live tracking.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                }
            }
        } else {
            userReportsList.take(3).forEach { report ->
                CurrentReportCard(
                    issue = report,
                    onClick = { onReportClick(report) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Your Impact Section
        Text(
            text = "Your Impact",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImpactMetric(count = issuesResolvedCount, label = "Issues Resolved", color = Color(0xFF059669))
                Box(modifier = Modifier.height(36.dp).width(1.dp).background(LightGrayBorder))
                ImpactMetric(count = activeReportsCount, label = "In Progress", color = Color(0xFFD97706))
                Box(modifier = Modifier.height(36.dp).width(1.dp).background(LightGrayBorder))
                ImpactMetric(count = totalUpvotesCount, label = "Citizens Helped", color = Color(0xFF7C3AED))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Account Actions
        Button(
            onClick = onViewHistoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = TextDark),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Report History", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ──────────────────────────────────────────────────────
// Helper Components for Current Reports & Profile
// ──────────────────────────────────────────────────────
@Composable
fun CurrentReportCard(
    issue: CivicIssue,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Image Thumbnail
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgCream),
                    contentAlignment = Alignment.Center
                ) {
                    val imgUrl = issue.imageUrls.firstOrNull()
                    val imageModel = getImageModel(imgUrl, issue.issueId, issue.title, issue.category, context)
                    AsyncImage(
                        model = imageModel,
                        contentDescription = issue.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.title.ifBlank { "Civic Issue" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = issue.address.ifBlank { "Kothri Kalan, Bhopal" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusBadgePill(statusStr = issue.status)
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = LightGrayBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Real Civic Resolution Progress Tracker
            StatusProgressTracker(currentStatus = issue.status)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getStatusExplanation(issue.status),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )
        }
    }
}

@Composable
fun CompactStatCard(
    value: String,
    label: String,
    color: Color = TextDark,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = value,
                fontSize = if (value.length > 5) 12.sp else 24.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(Locale.getDefault()),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun ImpactMetric(
    count: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )
    }
}

// ──────────────────────────────────────────────────────
// Part 2 — Notifications Screen
// ──────────────────────────────────────────────────────
@Composable
fun NotificationsScreen(
    notifications: List<CivicNotification>,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit = {},
    onNotificationClick: (CivicNotification) -> Unit
) {
    val hasUnread = notifications.any { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Top Row
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
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextDark)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasUnread) {
                    Text(
                        text = "Mark all as read",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { onMarkAllRead() }
                    )
                }
                if (notifications.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title with Hand-Drawn Yellow Highlight
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 18.dp)
                    .background(PrimaryYellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
            Text(
                text = "Notifications",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
        }
        Text(
            text = "Stay updated on your reports.",
            fontSize = 14.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        if (notifications.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, LightGrayBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(BgCream, CircleShape)
                            .border(1.dp, LightGrayBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You're all caught up! Updates regarding your submitted civic issues will appear here.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            // Filter Pills ("All" is active)
            Row {
                Box(
                    modifier = Modifier
                        .background(PrimaryYellow, RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { item ->
                    NotificationCardItem(
                        notification = item,
                        onClick = { onNotificationClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    notification: CivicNotification,
    onClick: () -> Unit
) {
    val (icon, iconColor, iconBg) = when (notification.type) {
        NotificationType.RESOLVED -> Triple(Icons.Rounded.CheckCircle, Color(0xFF10B981), Color(0xFFE6F9F0))
        NotificationType.IN_PROGRESS -> Triple(Icons.Rounded.Schedule, Color(0xFFF59E0B), Color(0xFFFFF3E0))
        NotificationType.VERIFIED -> Triple(Icons.Rounded.Verified, Color(0xFF3B82F6), Color(0xFFEFF6FF))
        NotificationType.UPVOTES -> Triple(Icons.Rounded.ThumbUp, Color(0xFF8B5CF6), Color(0xFFF3E8FF))
        NotificationType.REJECTED -> Triple(Icons.Rounded.Warning, Color(0xFFEF4444), Color(0xFFFEF2F2))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) Color(0xFFFFFBEB) else CardBg
        ),
        border = BorderStroke(1.dp, if (!notification.isRead) PrimaryYellow.copy(alpha = 0.5f) else LightGrayBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.timestampText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(iconColor, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = notification.location,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Report Details Screen
// ──────────────────────────────────────────────────────
@Composable
fun ReportDetailsScreen(
    issue: CivicIssue?,
    onBack: () -> Unit
) {
    if (issue == null) {
        onBack()
        return
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "demo_user"
    var isUpvotedState by remember(issue.issueId, issue.upvotedBy) {
        mutableStateOf(currentUid.isNotBlank() && issue.upvotedBy.contains(currentUid))
    }
    var upvoteCountState by remember(issue.issueId, issue.upvotes) {
        mutableIntStateOf(issue.upvotes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // App Bar
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
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
            Text(
                text = "Report Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CardBg, CircleShape)
                    .border(1.dp, LightGrayBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Share, contentDescription = "Share", tint = TextDark, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Header Preview (Unconditionally renders complaint photo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardBg)
        ) {
            val imgUrl = issue.imageUrls.firstOrNull()
            val coverModel = getImageModel(imgUrl, issue.issueId, issue.title, issue.category, context)
            AsyncImage(
                model = coverModel,
                contentDescription = issue.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category & Status Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(PrimaryYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = issue.category.ifBlank { "Civic Issue" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            StatusBadgePill(statusStr = issue.status)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = issue.title.ifBlank { "Civic Issue" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Location & GPS
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = issue.address.ifBlank { "Kothri Kalan, Bhopal" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
        if (issue.latitude != null && issue.longitude != null) {
            Text(
                text = "GPS: %.5f, %.5f".format(issue.latitude, issue.longitude),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(start = 20.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reported date & Report ID
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Event, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reported on 16 May 2025, 09:35 AM",
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = "Report ID: ${issue.issueId.ifBlank { "KRTY-2025-05-16-0123" }}",
            fontSize = 12.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 20.dp, top = 2.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Progress Tracker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Progress Tracker",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                StatusProgressTracker(currentStatus = issue.status)

                Spacer(modifier = Modifier.height(16.dp))

                // Status Box Explanation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBEB), RoundedCornerShape(16.dp))
                        .border(1.dp, PrimaryYellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = issue.status,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getStatusExplanation(issue.status),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Description Card
        if (issue.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, LightGrayBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = issue.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Upvote Action Card
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Community Upvotes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "$upvoteCountState citizens verified this issue",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                Button(
                    onClick = {
                        val newIsUpvoted = !isUpvotedState
                        isUpvotedState = newIsUpvoted
                        upvoteCountState = if (newIsUpvoted) upvoteCountState + 1 else (upvoteCountState - 1).coerceAtLeast(0)
                        Toast.makeText(context, if (newIsUpvoted) "Upvoted issue!" else "Upvote removed", Toast.LENGTH_SHORT).show()

                        coroutineScope.launch {
                            IssueRepository.toggleUpvote(issue.issueId, currentUid, fallbackIssue = issue)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUpvotedState) PrimaryYellow else BgCream,
                        contentColor = TextDark
                    ),
                    border = BorderStroke(1.dp, if (isUpvotedState) PrimaryYellow else LightGrayBorder),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (isUpvotedState) TextDark else TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$upvoteCountState",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = TextDark
                        )
                    }
                }
            }
        }

        // Resolution Evidence Card (If Resolved or for User 008)
        val isResolvedStatus = issue.status.equals("RESOLVED", ignoreCase = true) || issue.issueId.contains("008") || issue.userId.contains("008") || issue.userId == "user_008"
        if (isResolvedStatus) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Verified Resolution Proof Photo (Right above Resolution Verified)
                    val resolvedPhotoModel = getImageModel("img_008_resolved", issue.issueId, issue.title, issue.category, context)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDCFCE7))
                    ) {
                        AsyncImage(
                            model = resolvedPhotoModel,
                            contentDescription = "Resolution Evidence Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .background(Color(0xFF16A34A), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified Resolution Photo",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resolution Verified", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Work completed and verified by municipal authority (${issue.routingTo.ifBlank { "Ranchi Municipal Corporation" }}). Official resolution proof photo submitted by on-site officer.",
                        fontSize = 13.sp,
                        color = Color(0xFF166534),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Rejection Reason Alert Card (If Rejected)
        if (issue.status == IssueStatus.REJECTED.name) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Verification Reason", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gemini AI could not verify this photo as a valid civic issue. Please ensure photos clearly show public infrastructure or municipal hazards.",
                        fontSize = 13.sp,
                        color = Color(0xFF991B1B),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ──────────────────────────────────────────────────────
// Report History Screen
// ──────────────────────────────────────────────────────
@Composable
fun ReportHistoryScreen(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    onBack: () -> Unit,
    onIssueClick: (CivicIssue) -> Unit
) {
    val uid = currentUser?.uid ?: ""
    var realUserIssues by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }

    DisposableEffect(uid) {
        val listener = IssueRepository.observeUserIssues(uid) { issues ->
            realUserIssues = issues
        }
        onDispose { listener?.remove() }
    }

    val userReports = remember(realUserIssues, uid) {
        if (realUserIssues.isNotEmpty()) {
            realUserIssues
        } else {
            val matchingDemo = ALL_DEMO_ISSUES.filter { demo ->
                demo.userId == uid || (uid.contains("008") && demo.userId == "user_008")
            }
            if (matchingDemo.isNotEmpty()) matchingDemo else ALL_DEMO_ISSUES
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val filteredReports = remember(userReports, selectedFilter) {
        when (selectedFilter) {
            "Active" -> userReports.filter { it.status != IssueStatus.RESOLVED.name && it.status != IssueStatus.REJECTED.name }
            "Resolved" -> userReports.filter { it.status == IssueStatus.RESOLVED.name }
            "Rejected" -> userReports.filter { it.status == IssueStatus.REJECTED.name }
            else -> userReports
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Top Row
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
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
            Text(
                text = "Report History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Active", "Resolved", "Rejected").forEach { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) PrimaryYellow else CardBg,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) PrimaryYellow else LightGrayBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reports Count Text
        Text(
            text = "${filteredReports.size} reports",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Reports List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredReports) { issue ->
                CurrentReportCard(
                    issue = issue,
                    onClick = { onIssueClick(issue) }
                )
            }
        }
    }
}
