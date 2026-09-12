package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPolice
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.GuardianPurple
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SafetyTeal

@Composable
fun ProfileScreen(
    currentProfile: UserProfileEntity?,
    onSaveProfile: (UserProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = currentProfile ?: UserProfileEntity()

    var fullName by remember(currentProfile) { mutableStateOf(initial.fullName) }
    var phoneNumber by remember(currentProfile) { mutableStateOf(initial.phoneNumber) }
    var age by remember(currentProfile) { mutableStateOf(initial.age) }
    var bloodGroup by remember(currentProfile) { mutableStateOf(initial.bloodGroup) }
    var allergies by remember(currentProfile) { mutableStateOf(initial.allergies) }
    var medicalNotes by remember(currentProfile) { mutableStateOf(initial.medicalNotes) }
    var policeEmergencyNumber by remember(currentProfile) { mutableStateOf(initial.policeEmergencyNumber) }
    var safeHomeAddress by remember(currentProfile) { mutableStateOf(initial.safeHomeAddress) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Avatar Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryRose, GuardianPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (fullName.isNotBlank()) fullName else "Emergency Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Confidential Safety ID • Shared only upon SOS trigger",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Basic Identification
        Text(
            text = "BASIC IDENTIFICATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = PrimaryRose
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                        modifier = Modifier
                            .weight(1.8f)
                            .testTag("profile_phone_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_age_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional Medical Information
        Text(
            text = "OPTIONAL MEDICAL INFORMATION (CRITICAL FOR FIRST RESPONDERS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = PrimaryRose
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group (e.g. O+, B+, A-, AB+)") },
                    leadingIcon = { Icon(Icons.Rounded.Bloodtype, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_blood_group_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Known Allergies (Medications / Food)") },
                    placeholder = { Text("e.g. Penicillin, Peanuts, None") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = medicalNotes,
                    onValueChange = { medicalNotes = it },
                    label = { Text("Medical Conditions / Special Notes") },
                    placeholder = { Text("e.g. Asthmatic, Diabetic, Inhaler in backpack") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency & Safe Locations
        Text(
            text = "EMERGENCY POLICE & SAFE LOCATIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = PrimaryRose
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = policeEmergencyNumber,
                    onValueChange = { policeEmergencyNumber = it },
                    label = { Text("Police / Emergency Dispatch Number") },
                    leadingIcon = { Icon(Icons.Rounded.LocalPolice, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = safeHomeAddress,
                    onValueChange = { safeHomeAddress = it },
                    label = { Text("Safe Home / Landmark Address") },
                    placeholder = { Text("e.g. Apartment, Sector, City") },
                    leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                val updated = UserProfileEntity(
                    id = 1,
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    age = age,
                    bloodGroup = bloodGroup,
                    allergies = allergies,
                    medicalNotes = medicalNotes,
                    policeEmergencyNumber = policeEmergencyNumber,
                    safeHomeAddress = safeHomeAddress,
                    isRegistered = true
                )
                onSaveProfile(updated)
                saveSuccessMessage = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRose,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_profile_button")
        ) {
            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Emergency Profile", fontWeight = FontWeight.Bold)
        }

        if (saveSuccessMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✓ Emergency Profile saved securely in local Room storage.",
                fontSize = 12.sp,
                color = SafetyTeal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
