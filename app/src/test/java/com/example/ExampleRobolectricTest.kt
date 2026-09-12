package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.network.GeminiSafetyService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HerShield", appName)
  }

  @Test
  fun `test fallback AI safety analysis for stalker threat`() {
    val safetyService = GeminiSafetyService()
    val analysis = safetyService.fallbackAnalysis(
      situationText = "Someone is following me from the metro station",
      userName = "Ananya Roy",
      locationAddress = "Metro Gate 3",
      medicalNotes = "Asthma"
    )

    assertNotNull(analysis)
    assertEquals("CRITICAL", analysis.riskLevel)
    assertTrue(analysis.threatType.contains("Following") || analysis.threatType.contains("Stalking"))
    assertTrue(analysis.conciseAlertMessage.contains("Ananya Roy"))
    assertTrue(analysis.immediateSafetyActions.isNotEmpty())
  }
}
