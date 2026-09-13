package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhoneForwarded
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.service.CallChainState
import com.example.service.CallOutcome
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardianIndigo
import com.example.ui.theme.GuardianPurple
import com.example.ui.theme.SafetyTeal

@Composable
fun EmergencyCallChainCard(
    callChainState: CallChainState,
    onEscalateNext: () -> Unit,
    onMarkConnected: () -> Unit,
    onTogglePause: () -> Unit,
    onStopChain: () -> Unit,
    onStartChain: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!callChainState.isActive) {
        // Subtle trigger card if inactive
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag("call_chain_card_inactive")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GuardianIndigo.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.PhoneForwarded,
                                contentDescription = null,
                                tint = GuardianIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Emergency Calling Daisy Chain",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sequential auto-calling till someone answers",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onStartChain,
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("start_calling_chain_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Chain", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // Active Calling Chain Card
    val current = callChainState.currentContact
    val totalSeconds = 25f
    val remainingSeconds = callChainState.timeoutSecondsRemaining
    val progress = (remainingSeconds / totalSeconds).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (callChainState.isConnected) Color(0xFF0C2016) else Color(0xFF141926)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (callChainState.isConnected) SafetyTeal else GuardianIndigo.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_call_chain_card_active")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with cycle and queue badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneForwarded,
                        contentDescription = "Calling Chain",
                        tint = if (callChainState.isConnected) SafetyTeal else GuardianIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (callChainState.isConnected) "CALL CONNECTED" else "EMERGENCY CALLING CHAIN",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = if (callChainState.isConnected) SafetyTeal else Color(0xFF90CAF9)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GuardianPurple.copy(alpha = 0.25f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Loop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cycle #${callChainState.cycleNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current target contact row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Countdown circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { if (callChainState.isConnected) 1f else progress },
                        modifier = Modifier.size(54.dp),
                        strokeWidth = 4.dp,
                        color = if (callChainState.isConnected) SafetyTeal else GuardianIndigo,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                    Text(
                        text = if (callChainState.isConnected) "OK" else "${remainingSeconds}s",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = current?.name ?: "Emergency Contact",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${current?.relationship ?: "Contact"} • Priority #${callChainState.currentContactIndex + 1} of ${callChainState.totalContactsInQueue}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = current?.phoneNumber ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = GuardianIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live status message box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = callChainState.statusMessage,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!callChainState.isConnected) {
                    Button(
                        onClick = onMarkConnected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafetyTeal,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_call_connected_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Answered", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onEscalateNext,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GuardianIndigo,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("escalate_call_next_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Escalate Next", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onEscalateNext,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Call Next Contact", fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("toggle_pause_chain_btn")
                ) {
                    Icon(
                        imageVector = if (callChainState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = "Pause/Resume",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onStopChain,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CriticalRed.copy(alpha = 0.25f))
                        .testTag("stop_calling_chain_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CallEnd,
                        contentDescription = "End Chain",
                        tint = CriticalRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Call history attempts
            if (callChainState.callHistory.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Chain Escalation History:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                callChainState.callHistory.takeLast(3).reversed().forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${record.attemptNumber} ${record.contactName} (${record.relationship})",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (record.outcome) {
                                CallOutcome.ANSWERED_CONNECTED -> SafetyTeal.copy(alpha = 0.2f)
                                CallOutcome.DIALING -> GuardianIndigo.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                text = record.outcome.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (record.outcome) {
                                    CallOutcome.ANSWERED_CONNECTED -> SafetyTeal
                                    CallOutcome.DIALING -> GuardianIndigo
                                    else -> Color.White.copy(alpha = 0.7f)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
