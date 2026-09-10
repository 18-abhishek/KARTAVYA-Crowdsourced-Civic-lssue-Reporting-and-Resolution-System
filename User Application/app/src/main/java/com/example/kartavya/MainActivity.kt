package com.example.kartavya

import com.example.kartavya.core.location.fetchDeviceLocation
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
import com.example.kartavya.core.ui.theme.BgCream
import com.example.kartavya.core.ui.theme.CardBg
import com.example.kartavya.core.ui.theme.GreenStatus
import com.example.kartavya.core.ui.theme.LightGrayBorder
import com.example.kartavya.core.ui.theme.OrangeStatus
import com.example.kartavya.core.ui.theme.PrimaryYellow
import com.example.kartavya.core.ui.theme.TextDark
import com.example.kartavya.core.ui.theme.BgCream
import com.example.kartavya.core.ui.theme.CardBg
import com.example.kartavya.core.ui.theme.GreenStatus
import com.example.kartavya.core.ui.theme.LightGrayBorder
import com.example.kartavya.core.ui.theme.OrangeStatus
import com.example.kartavya.core.ui.theme.PrimaryYellow
import com.example.kartavya.core.ui.theme.TextDark
import com.example.kartavya.core.ui.theme.TextGray
import com.example.kartavya.core.utils.Feature
import com.example.kartavya.presentation.screens.WelcomeScreen
import com.example.kartavya.core.utils.Issue
import com.example.kartavya.core.utils.MOCK_FEATURES
import com.example.kartavya.core.utils.MOCK_FEED
import com.example.kartavya.core.utils.getImageModel
import com.example.kartavya.core.utils.loadDownsampledBitmap
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
import com.example.kartavya.presentation.screens.HomeFeed
import com.example.kartavya.presentation.screens.ReportIssueFlow
import com.example.kartavya.core.utils.ALL_DEMO_ISSUES
import com.example.kartavya.core.utils.DemoCivicIssue
import com.example.kartavya.core.utils.SMALL_DEMO_FALLBACK_ISSUES









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
