package com.example.kartavya.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.IssueStatus
import com.example.kartavya.model.UserProfile
import com.example.kartavya.data.IssueRepository
import com.example.kartavya.R
import com.google.firebase.auth.FirebaseUser
import androidx.compose.foundation.BorderStroke
import coil.compose.AsyncImage
import java.util.Locale
import com.example.kartavya.core.utils.getImageModel
import com.example.kartavya.presentation.components.*

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
                                text = "���� Member since Aug 2026",
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

// ������������������������������������������������������������������������������������������������������������������������������������������������������������������
// Helper Components for Current Reports & Profile
// ������������������������������������������������������������������������������������������������������������������������������������������������������������������
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
