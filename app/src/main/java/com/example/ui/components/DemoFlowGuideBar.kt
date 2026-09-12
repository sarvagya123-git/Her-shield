package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.SafetyTeal
import com.example.ui.theme.SosEmergencyRed

data class DemoStepItem(
    val stepNumber: Int,
    val title: String,
    val shortDesc: String
)

val DEMO_STEPS = listOf(
    DemoStepItem(1, "1. Profile", "Girl registration"),
    DemoStepItem(2, "2. Contacts", "Add trusted kin"),
    DemoStepItem(3, "3. SOS Trigger", "Press HELP button"),
    DemoStepItem(4, "4. Location", "GPS coordinates"),
    DemoStepItem(5, "5. Dispatch", "Alert sent"),
    DemoStepItem(6, "6. Dashboard", "Authorized view"),
    DemoStepItem(7, "7. AI Intel", "Threat prioritize")
)

@Composable
fun DemoFlowGuideBar(
    currentStep: Int,
    onSelectStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("demo_flow_guide_bar")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ElectricBolt,
                        contentDescription = "Demo Mode",
                        tint = PrimaryRose,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DEMO FLOW WALKTHROUGH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = PrimaryRose
                    )
                }

                Text(
                    text = "Step $currentStep of 7",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal scrolling pill tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEMO_STEPS.forEach { item ->
                    val isCompleted = currentStep > item.stepNumber
                    val isCurrent = currentStep == item.stepNumber

                    val bgBrush = when {
                        isCurrent -> Brush.horizontalGradient(listOf(PrimaryRose, SosEmergencyRed))
                        isCompleted -> Brush.horizontalGradient(listOf(SafetyTeal, SafetyTeal))
                        else -> Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgBrush)
                            .then(
                                if (!isCurrent && !isCompleted) {
                                    Modifier.background(MaterialTheme.colorScheme.surface)
                                } else Modifier
                            )
                            .clickable { onSelectStep(item.stepNumber) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium,
                                color = if (isCurrent || isCompleted) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
