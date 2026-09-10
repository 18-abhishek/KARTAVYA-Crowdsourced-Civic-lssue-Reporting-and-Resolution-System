package com.example.kartavya.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.example.kartavya.data.IssueRepository
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.core.utils.Issue
import com.example.kartavya.core.utils.getImageModel
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.IssueStatus
import com.example.kartavya.presentation.components.StatusBadgePill
import com.example.kartavya.presentation.components.StatusProgressTracker
import com.example.kartavya.presentation.components.getStatusExplanation

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

