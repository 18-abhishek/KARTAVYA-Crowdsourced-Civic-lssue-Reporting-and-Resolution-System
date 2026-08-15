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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.kartavya.data.AuthRepository
import com.example.kartavya.data.IssueRepository
import com.example.kartavya.data.StorageRepository
import com.example.kartavya.data.UserRepository
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
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
    var activeTab by remember { mutableStateOf("home") }
    var isReporting by remember { mutableStateOf(false) }

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

    // ── Logout ──
    val onLogoutClick: () -> Unit = {
        coroutineScope.launch {
            AuthRepository.signOut(context)
            currentUser = null
            userProfile = null
            activeTab = "home"
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
        // ── Main App ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCream)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (!isReporting) 100.dp else 0.dp)
            ) {
                AnimatedContent(
                    targetState = isReporting,
                    transitionSpec = {
                        slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                                slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    }
                ) { reporting ->
                    if (reporting) {
                        ReportIssueFlow(
                            currentUser = currentUser,
                            userProfile = userProfile,
                            onCancel = { isReporting = false }
                        )
                    } else {
                        Crossfade(targetState = activeTab) { tab ->
                            when (tab) {
                                "home" -> HomeFeed(userProfile = userProfile)
                                "profile" -> UserProfileScreen(
                                    currentUser = currentUser,
                                    userProfile = userProfile,
                                    onLogoutClick = onLogoutClick
                                )
                            }
                        }
                    }
                }
            }

            if (!isReporting) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    BottomNavBar(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        onReportClick = { isReporting = true }
                    )
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
@Composable
fun BottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onReportClick: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(40.dp))
                .border(1.dp, LightGrayBorder, RoundedCornerShape(40.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected("home") }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (activeTab == "home") TextDark else TextGray,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Now",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTab == "home") TextDark else TextGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(64.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected("profile") }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = if (activeTab == "profile") TextDark else TextGray,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Profile",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTab == "profile") TextDark else TextGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(y = (-32).dp)
                .size(72.dp)
                .background(PrimaryYellow, CircleShape)
                .border(4.dp, BgCream, CircleShape)
                .clickable { onReportClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Report Issue",
                tint = TextDark,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────
// Home Feed
// ──────────────────────────────────────────────────────
@Composable
fun HomeFeed(userProfile: UserProfile?) {
    val scrollState = rememberScrollState()
    val currentDate = LocalDate.now()
    val day = currentDate.dayOfMonth.toString().padStart(2, '0')
    val month = currentDate.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
    val year = currentDate.year.toString()

    var liveReports by remember { mutableStateOf<List<CivicIssue>>(emptyList()) }

    DisposableEffect(Unit) {
        val listener = IssueRepository.observeRecentIssues { reports ->
            liveReports = reports
        }
        onDispose { listener.remove() }
    }

    val greetingName = userProfile?.name?.split(" ")?.firstOrNull() ?: "Citizen"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hello, $greetingName!",
            fontSize = 14.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        ) {
            Text(
                text = "$day $month",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                lineHeight = 44.sp
            )
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, bottom = 6.dp)
                    .height(40.dp)
                    .width(2.dp)
                    .background(LightGrayBorder)
            )
            Column(modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)) {
                Text(text = "10:45 AM", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = year, fontSize = 12.sp, color = TextGray, letterSpacing = 2.sp)
            }
        }

        // Stats
        val reportsCount = (userProfile?.reportsFiled ?: 0).toString()
        val fixedCount = (userProfile?.issuesFixed ?: 0).toString()
        val rankStr = userProfile?.rank ?: "Newcomer"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatBubble(value = reportsCount, label = "Reports\nFiled")
            StatBubble(value = fixedCount, label = "Issues\nFixed", highlight = true)
            StatBubble(value = rankStr, label = "Civic\nRank", icon = Icons.Rounded.Star)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Recent Civic Feed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Live Firestore issues
        if (liveReports.isNotEmpty()) {
            liveReports.forEach { report ->
                LiveFeedCard(report)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Mock Feed
        MOCK_FEED.forEach { issue ->
            FeedCard(issue)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatBubble(value: String, label: String, highlight: Boolean = false, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(CardBg, RoundedCornerShape(24.dp))
                .border(1.dp, LightGrayBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (highlight) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp)
                        .border(2.dp, GreenStatus.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = PrimaryYellow, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = value,
                    fontSize = if (icon != null) 14.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
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
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// ──────────────────────────────────────────────────────
// Live Feed Card (Firestore issues)
// ──────────────────────────────────────────────────────
@Composable
fun LiveFeedCard(report: CivicIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF6B7280), Color(0xFF374151))))
            ) {
                val firstImage = report.imageUrls.firstOrNull()
                if (!firstImage.isNullOrBlank()) {
                    AsyncImage(
                        model = firstImage,
                        contentDescription = report.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = report.category.ifBlank { "Civic Issue" },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "By ${report.reporterName.ifBlank { "Citizen" }}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = report.title.ifBlank { "${report.category} reported" },
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.address.ifBlank { "Location Logged" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                }
                val statusColor = when (report.statusEnum()) {
                    com.example.kartavya.model.IssueStatus.RESOLVED -> GreenStatus
                    com.example.kartavya.model.IssueStatus.REJECTED -> Color(0xFFD32F2F)
                    else -> OrangeStatus
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = report.statusEnum().displayLabel(),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Mock Feed Card
// ──────────────────────────────────────────────────────
@Composable
fun FeedCard(issue: Issue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LightGrayBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(issue.gradientColors))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = issue.category, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = issue.time, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(text = issue.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = issue.location, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Box(
                    modifier = Modifier
                        .background(GreenStatus.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Resolved", color = GreenStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────
// Report Issue Flow (with Firebase Storage & Firestore)
// ──────────────────────────────────────────────────────
@Composable
fun ReportIssueFlow(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    onCancel: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var locationName by remember { mutableStateOf("Connaught Place, Block C") }
    var geoLat by remember { mutableStateOf<Double?>(28.6315) }
    var geoLng by remember { mutableStateOf<Double?>(77.2167) }
    var selectedCategory by remember { mutableStateOf("Streetlighting") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(step) {
        if (step == 3) {
            isAnalyzing = true
            delay(1500)
            isAnalyzing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header bar
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

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    val isActive = index + 1 == step
                    val isPast = index + 1 < step
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

        Spacer(modifier = Modifier.height(32.dp))

        // Step Content
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
                    3 -> StepThreeReview(
                        isAnalyzing = isAnalyzing,
                        category = selectedCategory,
                        locationName = locationName,
                        photoUri = photoUri
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                if (step == 3 && !isAnalyzing) {
                    isSubmitting = true
                    uploadProgress = 0f
                    coroutineScope.launch {
                        try {
                            val uid = currentUser?.uid
                            if (uid.isNullOrBlank()) {
                                Toast.makeText(context, "Please sign in to submit reports", Toast.LENGTH_LONG).show()
                                isSubmitting = false
                                return@launch
                            }

                            val issueId = StorageRepository.generateIssueId()

                            // 1. Upload photo
                            val uploadedImageUrls = mutableListOf<String>()
                            if (photoUri != null) {
                                val imgResult = StorageRepository.uploadIssueImage(
                                    uid = uid,
                                    issueId = issueId,
                                    imageUri = photoUri!!,
                                    onProgress = { uploadProgress = it * 0.6f }
                                )
                                imgResult.onSuccess { url -> uploadedImageUrls.add(url) }
                                    .onFailure { e ->
                                        Toast.makeText(context, "Image upload: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            // 2. Upload audio
                            var uploadedAudioUrl: String? = null
                            if (audioFile != null) {
                                uploadProgress = 0.6f
                                val audioResult = StorageRepository.uploadIssueAudio(uid, issueId, audioFile!!)
                                audioResult.onSuccess { url -> uploadedAudioUrl = url }
                                    .onFailure { e ->
                                        Toast.makeText(context, "Audio upload: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            uploadProgress = 0.8f

                            // 3. Create Firestore issue document
                            val issue = CivicIssue(
                                issueId = issueId,
                                userId = uid,
                                reporterName = userProfile?.name ?: currentUser?.displayName ?: "Citizen",
                                title = "$selectedCategory issue near $locationName",
                                description = "",
                                category = selectedCategory,
                                imageUrls = uploadedImageUrls,
                                audioUrl = uploadedAudioUrl,
                                latitude = geoLat,
                                longitude = geoLng,
                                address = locationName,
                                routingTo = "NDMC Authority",
                                priority = "Moderate (48h target)"
                            )

                            val createResult = IssueRepository.createIssue(issue)
                            uploadProgress = 1f

                            createResult.onSuccess {
                                UserRepository.incrementOnReport(uid)
                                Toast.makeText(context, "Report submitted to Firebase!", Toast.LENGTH_LONG).show()
                            }.onFailure { e ->
                                Toast.makeText(context, "Submission error: ${e.message}", Toast.LENGTH_LONG).show()
                            }

                            isSubmitting = false
                            onCancel()
                        } catch (e: Exception) {
                            isSubmitting = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            onCancel()
                        }
                    }
                } else {
                    step = (step + 1).coerceAtMost(3)
                }
            },
            enabled = !(step == 3 && (isAnalyzing || isSubmitting)),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (step == 3 && (isAnalyzing || isSubmitting)) LightGrayBorder else PrimaryYellow,
                contentColor = if (step == 3 && (isAnalyzing || isSubmitting)) TextGray else TextDark
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = if (step == 3) {
                    if (isAnalyzing) "AI Analyzing..."
                    else if (isSubmitting) "Uploading (${(uploadProgress * 100).toInt()}%)..."
                    else "Submit Report"
                } else "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
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
// Step 3 — Review
// ──────────────────────────────────────────────────────
@Composable
fun StepThreeReview(
    isAnalyzing: Boolean,
    category: String = "Streetlighting",
    locationName: String = "Connaught Place, Block C",
    photoUri: Uri? = null
) {
    if (isAnalyzing) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(color = PrimaryYellow, strokeWidth = 6.dp, strokeCap = StrokeCap.Round, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("AI is thinking...", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text("Auto-classifying issue & routing to authority", fontSize = 16.sp, color = TextGray, modifier = Modifier.padding(top = 8.dp))
        }
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.size(64.dp).background(Color(0xFFE6F9F0), CircleShape).padding(16.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GreenStatus, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Ready to\nSubmit", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark, lineHeight = 40.sp)
            Text(text = "We've auto-filled the details and geo-tag.", fontSize = 16.sp, color = TextGray, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = CardBg), border = BorderStroke(1.dp, LightGrayBorder)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(PrimaryYellow.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).border(2.dp, PrimaryYellow, RoundedCornerShape(16.dp)).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column { Text("CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OrangeStatus, letterSpacing = 1.sp); Text(category, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextDark) }
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = OrangeStatus)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp); Text(locationName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("ROUTING TO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp); Text("NDMC Authority", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("PRIORITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp); Text("Moderate (48h target)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ──────────────────────────────────────────────────────
// User Profile Screen (user is always authenticated here)
// ──────────────────────────────────────────────────────
@Composable
fun UserProfileScreen(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val userName = userProfile?.name ?: currentUser?.displayName ?: "Citizen"
    val userEmail = userProfile?.email ?: currentUser?.email ?: ""
    val userPoints = userProfile?.civicPoints ?: 0
    val userPhotoUrl = userProfile?.photoUrl ?: currentUser?.photoUrl?.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Civic", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark, lineHeight = 40.sp)
            Box {
                Box(modifier = Modifier.matchParentSize().padding(top = 24.dp).background(PrimaryYellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                Text(text = "Guardian", fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextDark, lineHeight = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            Text(
                text = "Logged in with Firebase & Google",
                fontSize = 16.sp, color = TextGray, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
        }

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, LightGrayBorder)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).background(BgCream, CircleShape).border(4.dp, Color.White, CircleShape).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userPhotoUrl.isNullOrBlank()) {
                            AsyncImage(model = userPhotoUrl, contentDescription = userName, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = TextDark, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(userName, fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextDark)
                        Text(if (userEmail.isNotBlank()) userEmail else "Level: Civic Guardian", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(userPoints.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextDark)
                        Text(" pts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("Goal: 2000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(bottom = 6.dp))
                }

                val progressFraction = (userPoints.toFloat() / 2000f).coerceIn(0f, 1f)
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(16.dp).background(BgCream, RoundedCornerShape(8.dp))) {
                    Box(modifier = Modifier.fillMaxWidth(progressFraction).fillMaxHeight().background(PrimaryYellow, RoundedCornerShape(8.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Your Badges & Perks", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.heightIn(max = 400.dp),
            userScrollEnabled = false
        ) {
            items(MOCK_FEATURES) { feature ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, LightGrayBorder)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(modifier = Modifier.size(48.dp).background(feature.bgColor, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(feature.icon, contentDescription = null, tint = feature.iconColor)
                        }
                        Text(text = feature.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout button
        Button(
            onClick = { onLogoutClick() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "View Report History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
