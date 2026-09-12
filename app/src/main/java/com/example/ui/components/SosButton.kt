package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SosEmergencyDark
import com.example.ui.theme.SosEmergencyGlow
import com.example.ui.theme.SosEmergencyRed

@Composable
fun SosButton(
    isProcessing: Boolean,
    isActiveAlert: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulseTransition")

    // Ripple radius animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActiveAlert) 1.35f else 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActiveAlert) 750 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActiveAlert) 750 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(260.dp)
    ) {
        // Outer Concentric Radar Ripples
        Canvas(modifier = Modifier.size(250.dp)) {
            val center = this.center
            val baseRadius = size.minDimension / 2.3f

            // Outer soft ring
            drawCircle(
                color = SosEmergencyGlow.copy(alpha = pulseAlpha * 0.6f),
                radius = baseRadius * pulseScale,
                center = center
            )

            // Mid ring
            drawCircle(
                color = SosEmergencyRed.copy(alpha = pulseAlpha),
                radius = baseRadius * (1f + (pulseScale - 1f) * 0.5f),
                center = center
            )
        }

        // Inner Tactile SOS Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(190.dp)
                .shadow(
                    elevation = if (isActiveAlert) 20.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = SosEmergencyRed,
                    spotColor = SosEmergencyGlow
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isActiveAlert) {
                            listOf(SosEmergencyGlow, SosEmergencyRed, SosEmergencyDark)
                        } else {
                            listOf(Color(0xFFFF3D57), SosEmergencyRed, Color(0xFF9E0B1C))
                        }
                    )
                )
                .testTag("sos_emergency_button")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick
                )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isActiveAlert) Icons.Filled.Warning else Icons.Rounded.Security,
                        contentDescription = "SOS Shield Icon",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isActiveAlert) "ALERT ACTIVE" else "NEED\nEMERGENCY HELP",
                        color = Color.White,
                        fontSize = if (isActiveAlert) 15.sp else 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = if (isActiveAlert) "RESCUE DISPATCHED" else "ONE-TAP INSTANT",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
