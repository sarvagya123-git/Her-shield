package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AiSituationScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResponderDashboardScreen
import com.example.ui.screens.SosHomeScreen
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardianPurple
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SosEmergencyRed
import com.example.ui.viewmodel.HerShieldViewModel

enum class HerShieldTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SOS("SOS", Icons.Rounded.Security),
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    AI_INTEL("AI Intel", Icons.Rounded.AutoAwesome),
    CONTACTS("Contacts", Icons.Rounded.People),
    PROFILE("Profile", Icons.Rounded.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: HerShieldViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.refreshLocation()
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                var currentTab by remember { mutableStateOf(HerShieldTab.SOS) }

                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val trustedContacts by viewModel.trustedContacts.collectAsStateWithLifecycle()
                val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()
                val location by viewModel.currentLocation.collectAsStateWithLifecycle()
                val isSosProcessing by viewModel.isSosProcessing.collectAsStateWithLifecycle()
                val isSirenActive by viewModel.isSirenActive.collectAsStateWithLifecycle()
                val dispatchedLogs by viewModel.dispatchedLogs.collectAsStateWithLifecycle()
                val latestAiAnalysis by viewModel.latestAiAnalysis.collectAsStateWithLifecycle()
                val isAiClassifying by viewModel.isAiClassifying.collectAsStateWithLifecycle()
                val currentDemoStep by viewModel.currentDemoStep.collectAsStateWithLifecycle()

                val isEmergencyActive = activeAlert != null

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(PrimaryRose, GuardianPurple)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shield,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "HerShield",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (isEmergencyActive) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = CriticalRed
                                        ) {
                                            Text(
                                                text = "SOS",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.toggleSiren() },
                                    modifier = Modifier.testTag("appbar_siren_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isSirenActive) Icons.Rounded.NotificationsOff else Icons.Rounded.NotificationsActive,
                                        contentDescription = "Toggle Siren",
                                        tint = if (isSirenActive) CriticalRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            HerShieldTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        if (tab == HerShieldTab.DASHBOARD && isEmergencyActive) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = CriticalRed) {
                                                        Text("!", color = Color.White)
                                                    }
                                                }
                                            ) {
                                                Icon(tab.icon, contentDescription = tab.title)
                                            }
                                        } else {
                                            Icon(tab.icon, contentDescription = tab.title)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryRose,
                                        selectedTextColor = PrimaryRose,
                                        indicatorColor = PrimaryRose.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            HerShieldTab.SOS -> {
                                SosHomeScreen(
                                    userProfile = userProfile,
                                    activeAlert = activeAlert,
                                    location = location,
                                    isProcessing = isSosProcessing,
                                    isSirenActive = isSirenActive,
                                    currentDemoStep = currentDemoStep,
                                    onTriggerSos = { threatText ->
                                        viewModel.triggerEmergencySos(threatText)
                                    },
                                    onResolveAlert = { id -> viewModel.resolveActiveAlert(id) },
                                    onToggleSiren = { viewModel.toggleSiren() },
                                    onDialPolice = { viewModel.dialPolice() },
                                    onRefreshLocation = { viewModel.refreshLocation() },
                                    onOpenExternalMap = { lat, lng -> viewModel.openCurrentLocationOnMap(lat, lng) },
                                    onSelectDemoStep = { step ->
                                        viewModel.setDemoStep(step)
                                        when (step) {
                                            1 -> currentTab = HerShieldTab.PROFILE
                                            2 -> currentTab = HerShieldTab.CONTACTS
                                            3 -> currentTab = HerShieldTab.SOS
                                            4 -> currentTab = HerShieldTab.SOS
                                            5 -> currentTab = HerShieldTab.DASHBOARD
                                            6 -> currentTab = HerShieldTab.DASHBOARD
                                            7 -> currentTab = HerShieldTab.AI_INTEL
                                        }
                                    },
                                    onNavigateToAi = { currentTab = HerShieldTab.AI_INTEL },
                                    onNavigateToDashboard = { currentTab = HerShieldTab.DASHBOARD }
                                )
                            }

                            HerShieldTab.DASHBOARD -> {
                                ResponderDashboardScreen(
                                    userProfile = userProfile,
                                    activeAlert = activeAlert,
                                    latestAiAnalysis = latestAiAnalysis,
                                    location = location,
                                    dispatchedLogs = dispatchedLogs,
                                    onDialVictim = { phone -> viewModel.dialContact(phone) },
                                    onDialPolice = { viewModel.dialPolice() },
                                    onOpenMap = { lat, lng -> viewModel.openCurrentLocationOnMap(lat, lng) },
                                    onRefreshLocation = { viewModel.refreshLocation() },
                                    onShareAlert = { msg -> viewModel.shareAlert(msg) },
                                    onResolveAlert = { id -> viewModel.resolveActiveAlert(id) }
                                )
                            }

                            HerShieldTab.AI_INTEL -> {
                                AiSituationScreen(
                                    latestAiAnalysis = latestAiAnalysis,
                                    isClassifying = isAiClassifying,
                                    onAnalyzeSituation = { text -> viewModel.analyzeCustomSituation(text) },
                                    onTriggerSosWithAi = { text ->
                                        viewModel.triggerEmergencySos(text)
                                        currentTab = HerShieldTab.DASHBOARD
                                    },
                                    onShareAlert = { msg -> viewModel.shareAlert(msg) }
                                )
                            }

                            HerShieldTab.CONTACTS -> {
                                ContactsScreen(
                                    contacts = trustedContacts,
                                    onAddContact = { name, phone, rel, isPrimary ->
                                        viewModel.addContact(name, phone, rel, isPrimary)
                                    },
                                    onDeleteContact = { id -> viewModel.deleteContact(id) },
                                    onDialContact = { phone -> viewModel.dialContact(phone) }
                                )
                            }

                            HerShieldTab.PROFILE -> {
                                ProfileScreen(
                                    currentProfile = userProfile,
                                    onSaveProfile = { updated -> viewModel.saveProfile(updated) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
