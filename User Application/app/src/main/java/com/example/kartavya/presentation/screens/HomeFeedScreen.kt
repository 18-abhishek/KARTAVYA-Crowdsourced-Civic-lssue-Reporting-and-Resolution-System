package com.example.kartavya.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.core.utils.ALL_DEMO_ISSUES
import com.example.kartavya.core.utils.DemoCivicIssue
import com.example.kartavya.core.utils.getImageModel
import com.example.kartavya.data.IssueRepository
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
