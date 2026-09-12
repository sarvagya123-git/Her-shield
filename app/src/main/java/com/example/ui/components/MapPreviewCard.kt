package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.UserLocationResult
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardianIndigo
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SafetyTeal
import com.example.ui.theme.SosEmergencyGlow
import com.example.ui.theme.SosEmergencyRed

@Composable
fun MapPreviewCard(
    location: UserLocationResult?,
    onOpenExternalMap: (Double, Double) -> Unit,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier,
    isEmergencyMode: Boolean = false
) {
    val lat = location?.latitude ?: 28.6315
    val lng = location?.longitude ?: 77.2167
    val address = location?.address ?: "Capturing GPS satellites..."
    val isRealGps = location?.isRealGps ?: false

    val infiniteTransition = rememberInfiniteTransition(label = "RadarMapScan")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BeaconPulse"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_preview_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with GPS Status and Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "GPS Pin",
                        tint = if (isEmergencyMode) CriticalRed else PrimaryRose,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEmergencyMode) "LIVE DISTRESS BEACON" else "CURRENT LOCATION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (isEmergencyMode) CriticalRed else PrimaryRose
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRealGps) SafetyTeal.copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isRealGps) "REAL GPS" else "TEST PROXIMITY",
                            color = if (isRealGps) SafetyTeal else Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onRefreshLocation,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stylized Tactical Map Graphic Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isEmergencyMode) {
                                listOf(Color(0xFF2C1014), Color(0xFF19090C))
                            } else {
                                listOf(Color(0xFF1E2638), Color(0xFF111722))
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isEmergencyMode) SosEmergencyRed.copy(alpha = 0.5f) else Color(0xFF37474F),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Tactical vector roads and radar grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)

                    // Road lines (tactile layout)
                    val roadColor = Color(0x33FFFFFF)
                    val mainHighway = Color(0x55FFFFFF)

                    // Roads
                    drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 3f)
                    drawLine(mainHighway, Offset(0f, h * 0.65f), Offset(w, h * 0.65f), strokeWidth = 6f)
                    drawLine(roadColor, Offset(w * 0.28f, 0f), Offset(w * 0.28f, h), strokeWidth = 3f)
                    drawLine(mainHighway, Offset(w * 0.72f, 0f), Offset(w * 0.72f, h), strokeWidth = 5f)
                    // Diagonal avenue
                    drawLine(roadColor, Offset(0f, h), Offset(w, 0f), strokeWidth = 2f)

                    // Concentric radar scan rings around beacon
                    val pulseColor = if (isEmergencyMode) SosEmergencyGlow else Color(0xFF64B5F6)
                    drawCircle(
                        color = pulseColor.copy(alpha = (1f - (beaconPulse / 42f)).coerceIn(0f, 0.8f)),
                        radius = beaconPulse,
                        center = center
                    )
                    drawCircle(
                        color = pulseColor.copy(alpha = (1f - (beaconPulse / 42f) * 0.5f).coerceIn(0f, 0.4f)),
                        radius = beaconPulse * 1.5f,
                        center = center
                    )

                    // Center beacon solid circle
                    drawCircle(
                        color = if (isEmergencyMode) CriticalRed else PrimaryRose,
                        radius = 8f,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = center
                    )
                }

                // Coordinate overlay badge inside map
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "%.4f° N, %.4f° E".format(lat, lng),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Compass / Live Tracking Icon overlay
                Surface(
                    shape = CircleShape,
                    color = (if (isEmergencyMode) CriticalRed else GuardianIndigo).copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Navigation,
                            contentDescription = "Direction",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEmergencyMode) "SOS ACTIVE" else "MONITORED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Address display
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Open in Google Maps
            ElevatedButton(
                onClick = { onOpenExternalMap(lat, lng) },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_external_map_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open in Google Maps / Navigation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
