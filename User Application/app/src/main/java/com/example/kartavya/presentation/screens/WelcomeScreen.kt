package com.example.kartavya.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kartavya.core.ui.theme.BgCream
import com.example.kartavya.core.ui.theme.GreenStatus
import com.example.kartavya.core.ui.theme.LightGrayBorder
import com.example.kartavya.core.ui.theme.PrimaryYellow
import com.example.kartavya.core.ui.theme.TextDark
import com.example.kartavya.core.ui.theme.TextGray

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
