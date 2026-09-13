package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.AiSafetyAnalysis
import com.example.data.network.AssistantChatMessage
import com.example.ui.components.AiAnalysisCard
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardianPurple
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SafetyGreen

@Composable
fun AiSituationScreen(
    latestAiAnalysis: AiSafetyAnalysis?,
    isClassifying: Boolean,
    chatMessages: List<AssistantChatMessage> = emptyList(),
    isAssistantThinking: Boolean = false,
    onSendChatMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onAnalyzeSituation: (String) -> Unit,
    onTriggerSosWithAi: (String) -> Unit,
    onShareAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAiModeTab by remember { mutableIntStateOf(0) } // 0: Interactive Assistant, 1: Threat Classifier

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // AI Safety Header Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryRose, GuardianPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "AI Safety",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Safety Intel & Assistant",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SafetyGreen.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyGreen,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Interactive Open Assistant & Multimodal Threat Assessment",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Tabs: 0 -> Astra AI Assistant, 1 -> Threat Classifier
        TabRow(
            selectedTabIndex = selectedAiModeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryRose,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAiModeTab]),
                    color = PrimaryRose
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedAiModeTab == 0,
                onClick = { selectedAiModeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Astra Assistant", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                modifier = Modifier.testTag("tab_astra_assistant")
            )
            Tab(
                selected = selectedAiModeTab == 1,
                onClick = { selectedAiModeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Threat Classifier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                modifier = Modifier.testTag("tab_threat_classifier")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedAiModeTab == 0) {
            // ==========================================
            // Interactive Open-Source AI Safety Assistant
            // ==========================================
            InteractiveAssistantChatView(
                chatMessages = chatMessages,
                isThinking = isAssistantThinking,
                onSendMessage = onSendChatMessage,
                onClearChat = onClearChat,
                onTriggerSosWithAi = onTriggerSosWithAi
            )
        } else {
            // ==========================================
            // Threat Classifier & Situation Analyzer
            // ==========================================
            ThreatClassifierView(
                latestAiAnalysis = latestAiAnalysis,
                isClassifying = isClassifying,
                onAnalyzeSituation = onAnalyzeSituation,
                onTriggerSosWithAi = onTriggerSosWithAi,
                onShareAlert = onShareAlert
            )
        }
    }
}

@Composable
private fun InteractiveAssistantChatView(
    chatMessages: List<AssistantChatMessage>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onTriggerSosWithAi: (String) -> Unit
) {
    var userDraft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "Someone is following me",
        "Cab driver changed route",
        "Harassment on crowded bus",
        "Night walking safety tips",
        "My legal rights in emergency"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick suggestions bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickQuestions) { prompt ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSendMessage(prompt) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = PrimaryRose, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(prompt, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                if (msg.isUser) {
                    // User Message (Right aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            color = PrimaryRose,
                            contentColor = Color.White,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.message,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    // Assistant Message (Left aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(GuardianPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.widthIn(max = 290.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Astra AI Safety Guardian",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = GuardianPurple
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.message,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (msg.isEmergencyActionable) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { onTriggerSosWithAi(msg.message) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CriticalRed,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Rounded.Emergency, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🚨 Trigger Instant SOS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(GuardianPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp),
                                color = GuardianPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Astra is analyzing safety response...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Input Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = userDraft,
                    onValueChange = { userDraft = it },
                    placeholder = { Text("Ask Astra about your safety...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("astra_chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRose,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (userDraft.isNotBlank() && !isThinking) {
                            onSendMessage(userDraft)
                            userDraft = ""
                        }
                    },
                    enabled = userDraft.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (userDraft.isNotBlank()) PrimaryRose else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("send_astra_chat_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (userDraft.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreatClassifierView(
    latestAiAnalysis: AiSafetyAnalysis?,
    isClassifying: Boolean,
    onAnalyzeSituation: (String) -> Unit,
    onTriggerSosWithAi: (String) -> Unit,
    onShareAlert: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("Someone is following me from the metro station.") }
    var isSimulatingVoice by remember { mutableStateOf(false) }

    val exampleScenarios = listOf(
        "Someone is following me",
        "Cab driver changed route to an unlit alley",
        "Group of men harassing near bus terminal",
        "Feeling dizzy and in physical danger",
        "Suspicious vehicle tailgating my scooter"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Quick Situation Samples (Tap to Load):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(exampleScenarios) { scenario ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { inputText = scenario }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = PrimaryRose,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scenario,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Situation Input Box with Voice Mic Simulation
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Describe What is Happening (Text or Voice):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("e.g. Someone is following me, two men at corner...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("situation_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    isSimulatingVoice = !isSimulatingVoice
                                    if (isSimulatingVoice) {
                                        inputText = "Voice input: Cab driver took a dark turn away from GPS route. Please track me!"
                                    }
                                },
                                modifier = Modifier.testTag("voice_input_mic_btn")
                            ) {
                                Icon(
                                    imageVector = if (isSimulatingVoice) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                                    contentDescription = "Voice Input Simulation",
                                    tint = if (isSimulatingVoice) CriticalRed else PrimaryRose
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAnalyzeSituation(inputText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GuardianPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isClassifying && inputText.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_ai_classify_btn")
                        ) {
                            if (isClassifying) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze with AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { onTriggerSosWithAi(inputText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CriticalRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_sos_from_ai_btn")
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SOS with AI", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // AI Intel Result Card
        if (latestAiAnalysis != null) {
            item {
                AiAnalysisCard(
                    analysis = latestAiAnalysis,
                    onShareAlert = onShareAlert
                )
            }
        }
    }
}
