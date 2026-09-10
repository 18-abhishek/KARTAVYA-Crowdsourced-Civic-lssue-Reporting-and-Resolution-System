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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.example.kartavya.data.models.UserProfile
import com.example.kartavya.data.repository.IssueRepository
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.core.utils.Issue
import com.example.kartavya.core.utils.ALL_DEMO_ISSUES
import com.example.kartavya.core.utils.getImageModel
import com.example.kartavya.model.CivicIssue
import com.example.kartavya.model.IssueStatus
import com.example.kartavya.presentation.components.StatusBadgePill

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

