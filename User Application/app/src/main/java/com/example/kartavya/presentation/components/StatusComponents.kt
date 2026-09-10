package com.example.kartavya.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kartavya.core.ui.theme.*
import com.example.kartavya.model.IssueStatus

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
