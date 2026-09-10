package com.example.kartavya.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.core.utils.Issue
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.IssueStatus
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
