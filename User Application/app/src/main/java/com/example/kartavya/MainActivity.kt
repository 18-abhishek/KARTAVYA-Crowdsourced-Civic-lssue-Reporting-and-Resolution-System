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
import com.example.kartavya.presentation.screens.UserProfileScreen
import com.example.kartavya.presentation.screens.NotificationsScreen
import com.example.kartavya.presentation.screens.ReportDetailsScreen
import com.example.kartavya.presentation.screens.ReportHistoryScreen
import com.example.kartavya.presentation.screens.mockNotifications
import com.example.kartavya.presentation.screens.CurrentReportCard
import com.example.kartavya.presentation.components.StatusBadgePill
import com.example.kartavya.presentation.components.StatusProgressTracker
import com.example.kartavya.presentation.components.getStatusExplanation
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


// ──────────────────────────────────────────────────────
// Report Details Screen
// ──────────────────────────────────────────────────────
}
