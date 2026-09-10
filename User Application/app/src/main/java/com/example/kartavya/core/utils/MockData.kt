package com.example.kartavya.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9821",
        userId = "user_001",
        reporterName = "Demo User 001",
        title = "Large Pothole on Main Road",
        description = "Deep asphalt crater on Ward 29 Kanke Road causing severe traffic bottleneck and vehicle damage.",
        category = "Road Damage",
        status = com.example.kartavya.model.IssueStatus.REPORTED.name,
        address = "Ward 29, Kanke Road, Ranchi",
        priority = "High",
        imageUrls = listOf("img_001"),
        upvotes = 15,
        routingTo = "Ranchi Municipal Corporation"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9820",
        userId = "user_002",
        reporterName = "Demo User 002",
        title = "Streetlight Inoperative & Wire Sagging",
        description = "Streetlight pole completely unlit for 3 consecutive nights with loose sagging electrical cable in residential lane.",
        category = "Streetlighting",
        status = com.example.kartavya.model.IssueStatus.IN_PROGRESS.name,
        address = "Ward 12, Harmu Housing Colony, Ranchi",
        priority = "Moderate",
        imageUrls = listOf("img_002"),
        upvotes = 8,
        routingTo = "Jharkhand Bijli Vitran Nigam"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9819",
        userId = "user_003",
        reporterName = "Demo User 003",
        title = "Garbage Dump Overflow on Main Thoroughfare",
        description = "Commercial waste bin spilling onto pedestrian walkway near Lalpur vegetable market entrance.",
        category = "Cleanliness",
        status = com.example.kartavya.model.IssueStatus.ACKNOWLEDGED.name,
        address = "Ward 31, Lalpur, Ranchi",
        priority = "High",
        imageUrls = listOf("img_003"),
        upvotes = 6,
        routingTo = "Ranchi Urban Sanitation Dept"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9818",
        userId = "user_004",
        reporterName = "Demo User 004",
        title = "Drinking Water Pipeline Leakage",
        description = "Clean municipal water main pipe ruptured, wasting hundreds of liters daily on public road surface.",
        category = "Water Supply",
        status = com.example.kartavya.model.IssueStatus.IN_PROGRESS.name,
        address = "Ward 18, Doranda, Ranchi",
        priority = "High",
        imageUrls = listOf("img_004"),
        upvotes = 11,
        routingTo = "Water Works Department"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9817",
        userId = "user_005",
        reporterName = "Demo User 005",
        title = "Open Sewer Manhole Hazard",
        description = "Missing heavy concrete manhole cover on main sidewalk posing severe fall hazard for pedestrians.",
        category = "Sanitation",
        status = com.example.kartavya.model.IssueStatus.REPORTED.name,
        address = "Ward 08, Main Road, Dhanbad",
        priority = "Critical",
        imageUrls = listOf("img_005"),
        upvotes = 22,
        routingTo = "Dhanbad Municipal Corporation"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9816",
        userId = "user_006",
        reporterName = "Demo User 006",
        title = "Broken Traffic Signal Light",
        description = "Intersection traffic signals non-functional during peak evening hours, causing severe traffic gridlock.",
        category = "Traffic & Transport",
        status = com.example.kartavya.model.IssueStatus.ACKNOWLEDGED.name,
        address = "Bistupur Crossing, Jamshedpur",
        priority = "High",
        imageUrls = listOf("img_006"),
        upvotes = 14,
        routingTo = "Traffic Police & Urban Transport"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9815",
        userId = "user_007",
        reporterName = "Demo User 007",
        title = "Overgrown Trees Blocking Power Lines",
        description = "Heavy tree branches hanging dangerously over high voltage electrical wires near Sector 4 residential zone.",
        category = "Electricity",
        status = com.example.kartavya.model.IssueStatus.IN_PROGRESS.name,
        address = "Sector 4, Bokaro Steel City",
        priority = "Moderate",
        imageUrls = listOf("img_007"),
        upvotes = 9,
        routingTo = "Bokaro Electricity Supply"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9708",
        userId = "user_008",
        reporterName = "Demo User 008",
        title = "Main Sewage Line Backflow & Choke",
        description = "Severe sewage line blockage successfully cleared and pipeline fully restored by municipal drainage team.",
        category = "Sanitation",
        status = com.example.kartavya.model.IssueStatus.RESOLVED.name,
        address = "Harmu Housing Colony, Block C, Ranchi",
        priority = "Critical",
        imageUrls = listOf("img_008"),
        upvotes = 34,
        routingTo = "Ranchi Municipal Drainage Dept"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9813",
        userId = "user_009",
        reporterName = "Demo User 009",
        title = "Damaged Public Park Bench & Fencing",
        description = "Vandalized perimeter fencing and broken public seating in community park requiring municipal repair.",
        category = "Public Amenities",
        status = com.example.kartavya.model.IssueStatus.REPORTED.name,
        address = "Morabadi Ground Park, Ranchi",
        priority = "Low",
        imageUrls = listOf("img_009"),
        upvotes = 5,
        routingTo = "Parks & Recreation Department"
    ),
    com.example.kartavya.model.CivicIssue(
        issueId = "JH-9812",
        userId = "user_010",
        reporterName = "Demo User 010",
        title = "Stagnant Stormwater Accumulation",
        description = "Rainwater standing for over 48 hours in low-lying residential sector raising mosquito and health concerns.",
        category = "Drainage",
        status = com.example.kartavya.model.IssueStatus.IN_PROGRESS.name,
        address = "Chutia Ring Road, Ranchi",
        priority = "Moderate",
        imageUrls = listOf("img_010"),
        upvotes = 12,
        routingTo = "Ranchi Drainage Authority"
    )
)

val SMALL_DEMO_FALLBACK_ISSUES = ALL_DEMO_ISSUES
