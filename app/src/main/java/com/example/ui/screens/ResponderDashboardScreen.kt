package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPolice
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyAlertEntity
import com.example.data.local.UserProfileEntity
import com.example.data.network.AiSafetyAnalysis
import com.example.service.DispatchedNotification
import com.example.service.UserLocationResult
import com.example.ui.components.AiAnalysisCard
import com.example.ui.components.MapPreviewCard
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardianPurple
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SafetyTeal
import com.example.ui.theme.SosEmergencyRed
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResponderDashboardScreen(
    userProfile: UserProfileEntity?,
    activeAlert: EmergencyAlertEntity?,
    latestAiAnalysis: AiSafetyAnalysis?,
    location: UserLocationResult?,
    dispatchedLogs: List<DispatchedNotification>,
    onDialVictim: (String) -> Unit,
    onDialPolice: () -> Unit,
    onOpenMap: (Double, Double) -> Unit,
    onRefreshLocation: () -> Unit,
    onShareAlert: (String) -> Unit,
    onResolveAlert: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAlertActive = activeAlert != null
    val profile = userProfile ?: UserProfileEntity()

    val timeFormat = SimpleDateFormat("hh:mm:ss a, dd MMM yyyy", Locale.getDefault())
    val alertTime = if (activeAlert != null) timeFormat.format(Date(activeAlert.timestamp)) else timeFormat.format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Responder Mode Simulation Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GuardianPurple.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GuardianPurple.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = "Authorized",
                    tint = GuardianPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authorized Contact Emergency Portal (Simulated Live View)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GuardianPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Emergency Alert Card (as requested in prompt)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAlertActive) CriticalRed else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("emergency_alert_dashboard_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAlertActive) Icons.Filled.Warning else Icons.Rounded.CheckCircle,
                            contentDescription = "Alert",
                            tint = if (isAlertActive) Color.White else SafetyTeal,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAlertActive) "🚨 EMERGENCY ALERT" else "ALL SAFE — MONITORING",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = if (isAlertActive) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAlertActive) "Immediate Response Requested" else "No active distress signal",
                                fontSize = 11.sp,
                                color = if (isAlertActive) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Risk Badge
                    val riskText = activeAlert?.riskLevel ?: latestAiAnalysis?.riskLevel ?: "HIGH"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAlertActive) Color.White else CriticalRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isAlertActive) "RISK: $riskText" else "NORMAL",
                            color = if (isAlertActive) CriticalRed else SafetyTeal,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Core Alert Fields as specified by prompt
                // User: [Name]
                // Time: [Time]
                // Situation: [AI classification]
                // Risk Level: High
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAlertActive) Color.Black.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        DetailRow(
                            label = "User",
                            value = "${profile.fullName} (Age: ${profile.age})",
                            isUrgent = isAlertActive
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DetailRow(
                            label = "Contact",
                            value = profile.phoneNumber,
                            isUrgent = isAlertActive
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DetailRow(
                            label = "Time",
                            value = alertTime,
                            isUrgent = isAlertActive
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DetailRow(
                            label = "Situation",
                            value = activeAlert?.threatType ?: latestAiAnalysis?.threatType ?: "Threat / Suspicious Distress",
                            isUrgent = isAlertActive
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DetailRow(
                            label = "Risk Level",
                            value = "${activeAlert?.riskLevel ?: latestAiAnalysis?.riskLevel ?: "HIGH"} PRIORITY",
                            isUrgent = isAlertActive
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        DetailRow(
                            label = "Medical Info",
                            value = "Blood: ${profile.bloodGroup} • ${profile.medicalNotes}",
                            isUrgent = isAlertActive
                        )
                    }
                }

                if (isAlertActive) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onDialVictim(profile.phoneNumber) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = CriticalRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Victim", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDialPolice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A237E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.LocalPolice, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call 112", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { activeAlert?.id?.let { onResolveAlert(it) } },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Safe", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Map Section (📍 Current Location: [Map])
        Text(
            text = "📍 LIVE RESCUE LOCATION",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = if (isAlertActive) CriticalRed else PrimaryRose
        )

        Spacer(modifier = Modifier.height(8.dp))

        MapPreviewCard(
            location = location,
            onOpenExternalMap = onOpenMap,
            onRefreshLocation = onRefreshLocation,
            isEmergencyMode = isAlertActive
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Threat Intel Card (if available)
        if (latestAiAnalysis != null) {
            AiAnalysisCard(
                analysis = latestAiAnalysis,
                onShareAlert = onShareAlert
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contact Dispatch Transmission Receipts
        Text(
            text = "📱 EMERGENCY BROADCAST LOG",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (dispatchedLogs.isEmpty()) {
                    Text(
                        text = "System is in standby mode. When ONE-TAP SOS is pressed, alert SMS and live location packets will be logged here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dispatchedLogs.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SafetyTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Email,
                                        contentDescription = null,
                                        tint = SafetyTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = log.recipientName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = log.phoneNumber,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SafetyTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = log.status,
                                    color = SafetyTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isUrgent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUrgent) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isUrgent) Color.White else MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
