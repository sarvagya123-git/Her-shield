package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalPolice
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyAlertEntity
import com.example.data.local.UserProfileEntity
import com.example.data.network.AiSafetyAnalysis
import com.example.service.AudioRecordState
import com.example.service.CallChainState
import com.example.service.UserLocationResult
import com.example.ui.components.AudioRecordingCard
import com.example.ui.components.DemoFlowGuideBar
import com.example.ui.components.EmergencyCallChainCard
import com.example.ui.components.MapPreviewCard
import com.example.ui.components.SosButton
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardianPurple
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SafetyTeal
import com.example.ui.theme.SosEmergencyGlow
import com.example.ui.theme.SosEmergencyRed

import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import com.example.util.AppLanguage
import com.example.util.AppLocalization

@Composable
fun SosHomeScreen(
    userProfile: UserProfileEntity?,
    activeAlert: EmergencyAlertEntity?,
    location: UserLocationResult?,
    isProcessing: Boolean,
    isSirenActive: Boolean,
    currentDemoStep: Int,
    audioState: AudioRecordState = AudioRecordState.Idle,
    latestAiAnalysis: AiSafetyAnalysis? = null,
    callChainState: CallChainState = CallChainState(),
    isLiveGpsTracking: Boolean = true,
    language: AppLanguage = AppLanguage.ENGLISH,
    isDarkTheme: Boolean = false,
    instantShareNotification: String? = null,
    onDismissInstantShare: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onOpenLanguageSelector: () -> Unit = {},
    onTriggerSos: (String?) -> Unit,
    onResolveAlert: (Long) -> Unit,
    onToggleSiren: () -> Unit,
    onDialPolice: () -> Unit,
    onRefreshLocation: () -> Unit,
    onOpenExternalMap: (Double, Double) -> Unit,
    onSelectDemoStep: (Int) -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onStopAudioAndAnalyze: () -> Unit = {},
    onCancelAudioRecording: () -> Unit = {},
    onStartManualAudioRecording: () -> Unit = {},
    onEscalateCallChain: () -> Unit = {},
    onMarkCallConnected: () -> Unit = {},
    onTogglePauseCallChain: () -> Unit = {},
    onStopCallChain: () -> Unit = {},
    onStartCallChain: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = AppLocalization.getStrings(language)
    var selectedThreatScenario by remember { mutableStateOf<String?>(null) }
    val isEmergencyActive = activeAlert != null

    val presetThreats = listOf(
        "Someone is following me",
        "Cab driver changed route",
        "Harassment in public",
        "Unsafe person nearby"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Top Bar: Language & Theme Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryRose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "HerShield",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = strings.appTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.appSubtitle,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language Selection Quick Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenLanguageSelector() }
                        .testTag("home_language_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = language.flagEmoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = language.code.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Dark / Light Theme Quick Toggle
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("home_theme_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        contentDescription = "Theme Toggle",
                        tint = if (isDarkTheme) GuardianPurple else PrimaryRose,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Instant Location Shared Notification Banner
        AnimatedVisibility(visible = instantShareNotification != null) {
            instantShareNotification?.let { bannerText ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SafetyTeal.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("instant_share_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = SafetyTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Location Shared Instantly",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SafetyTeal
                            )
                            Text(
                                text = bannerText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onDismissInstantShare,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Demo Flow Guide Bar
        DemoFlowGuideBar(
            currentStep = currentDemoStep,
            onSelectStep = onSelectDemoStep
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Active Emergency Alert Banner (if alert triggered)
        AnimatedVisibility(visible = isEmergencyActive) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CriticalRed
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("active_emergency_banner")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Emergency Active",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY BROADCAST ACTIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        IconButton(
                            onClick = onToggleSiren,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("toggle_siren_banner_btn")
                        ) {
                            Icon(
                                imageVector = if (isSirenActive) Icons.Rounded.NotificationsOff else Icons.Rounded.NotificationsActive,
                                contentDescription = "Toggle Siren",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Contacts alerted with your live location. Situation: ${activeAlert?.threatType}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToDashboard,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = CriticalRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_live_dashboard_btn")
                        ) {
                            Text("Live Dashboard", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { activeAlert?.id?.let { onResolveAlert(it) } },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("resolve_alert_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("I am Safe", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Safety Status & Guardian Info Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryRose, GuardianPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "HerShield Active Protection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEmergencyActive) "Emergency Broadcast Mode" else "Ready: Tap SOS in danger",
                            fontSize = 11.sp,
                            color = if (isEmergencyActive) CriticalRed else SafetyTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Police Call button
                IconButton(
                    onClick = onDialPolice,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6))
                        .testTag("dial_police_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalPolice,
                        contentDescription = "Dial Police 112",
                        tint = GuardianPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Threat Scenario Tags (User can tap e.g. "Someone is following me")
        Text(
            text = "Select Situation or Tap Button Directly:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetThreats.take(2).forEach { threat ->
                val isSelected = selectedThreatScenario == threat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedThreatScenario = if (isSelected) null else threat
                    },
                    label = { Text(threat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRose,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetThreats.drop(2).forEach { threat ->
                val isSelected = selectedThreatScenario == threat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedThreatScenario = if (isSelected) null else threat
                    },
                    label = { Text(threat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRose,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Large Central ONE-TAP SOS Button
        SosButton(
            isProcessing = isProcessing,
            isActiveAlert = isEmergencyActive,
            onClick = {
                onTriggerSos(selectedThreatScenario)
            },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (selectedThreatScenario != null) {
                "Selected: \"$selectedThreatScenario\" — Tap SOS to dispatch alert!"
            } else {
                "ONE-TAP: Captures GPS, notifies trusted contacts & initiates emergency response"
            },
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Utility Actions: Call 112, Siren Alarm, AI Analyzer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Siren button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSirenActive) CriticalRed else MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("home_siren_card_btn")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    IconButton(onClick = onToggleSiren) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = "Siren",
                            tint = if (isSirenActive) Color.White else SosEmergencyRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = if (isSirenActive) "Stop Siren" else "Sound Siren",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSirenActive) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Call Police 112
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("home_call_police_btn")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    IconButton(onClick = onDialPolice) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = "Call 112",
                            tint = PrimaryRose,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Call Police (112)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // AI Situation Analyzer
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("home_ai_intel_btn")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    IconButton(onClick = onNavigateToAi) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = "AI Classifier",
                            tint = GuardianPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "AI Threat Intel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feature 1: Automatic 1-Minute Audio Recording & Context Extraction
        AudioRecordingCard(
            audioState = audioState,
            latestAiAnalysis = latestAiAnalysis,
            onStopAndAnalyze = onStopAudioAndAnalyze,
            onCancelRecording = onCancelAudioRecording,
            onStartManualRecording = onStartManualAudioRecording
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Feature 2: Emergency Contacts Daisy Chain Calling
        EmergencyCallChainCard(
            callChainState = callChainState,
            onEscalateNext = onEscalateCallChain,
            onMarkConnected = onMarkCallConnected,
            onTogglePause = onTogglePauseCallChain,
            onStopChain = onStopCallChain,
            onStartChain = onStartCallChain
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location Snapshot Map Card with Live GPS Streaming Status
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isLiveGpsTracking) SafetyTeal.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.size(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isLiveGpsTracking) SafetyTeal else Color.Gray)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLiveGpsTracking) "REAL-TIME GPS TRACKING ACTIVE" else "GPS STANDBY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = if (isLiveGpsTracking) SafetyTeal else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "High Precision Satellites",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            MapPreviewCard(
                location = location,
                onOpenExternalMap = onOpenExternalMap,
                onRefreshLocation = onRefreshLocation,
                isEmergencyMode = isEmergencyActive
            )
        }
    }
}
