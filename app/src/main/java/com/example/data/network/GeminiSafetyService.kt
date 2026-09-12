package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiSafetyAnalysis(
    val threatType: String,
    val riskLevel: String, // CRITICAL, HIGH, MEDIUM, LOW
    val conciseAlertMessage: String,
    val responderSummary: String,
    val immediateSafetyActions: List<String>,
    val urgencyScore: Int
)

class GeminiSafetyService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeEmergencySituation(
        situationText: String,
        userName: String,
        locationAddress: String,
        medicalNotes: String = ""
    ): AiSafetyAnalysis = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("HerShieldAI", "Gemini API key is placeholder or empty. Using intelligent local safety classifier.")
            return@withContext fallbackAnalysis(situationText, userName, locationAddress, medicalNotes)
        }

        try {
            val prompt = """
                You are the AI Safety Engine for HerShield, a mission-critical women's personal safety platform.
                Analyze the following emergency situation reported by user "$userName":
                
                Situation description: "$situationText"
                Current Location: "$locationAddress"
                Medical Notes: "$medicalNotes"
                
                Evaluate and provide your response strictly as valid JSON with NO markdown formatting, with exactly these keys:
                {
                  "threatType": "Short category (e.g. Stalking / Being Followed, Route Deviation / Cab Danger, Physical Assault / Threat, Public Harassment, Medical Crisis, Suspicious Activity)",
                  "riskLevel": "CRITICAL" or "HIGH" or "MEDIUM",
                  "conciseAlertMessage": "One concise high-urgency SMS/notification message under 160 characters for trusted contacts including victim name, situation, and location",
                  "responderSummary": "A 2-sentence tactical briefing for police and trusted family responders explaining the danger and priority",
                  "immediateSafetyActions": ["Action 1 for victim to stay safe right now", "Action 2", "Action 3"],
                  "urgencyScore": integer between 50 and 100
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.1)
                    put("topP", 0.95)
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("HerShieldAI", "Gemini API error code: ${response.code}")
                return@withContext fallbackAnalysis(situationText, userName, locationAddress, medicalNotes)
            }

            val bodyString = response.body?.string() ?: ""
            val jsonRoot = JSONObject(bodyString)
            val candidates = jsonRoot.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Clean markdown code blocks if present
            val cleanedJsonText = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val resultJson = JSONObject(cleanedJsonText)
            val actionsArray = resultJson.optJSONArray("immediateSafetyActions")
            val actionsList = mutableListOf<String>()
            if (actionsArray != null) {
                for (i in 0 until actionsArray.length()) {
                    actionsList.add(actionsArray.getString(i))
                }
            }
            if (actionsList.isEmpty()) {
                actionsList.addAll(listOf("Stay in well-lit public area", "Keep emergency line open", "Alert people nearby"))
            }

            AiSafetyAnalysis(
                threatType = resultJson.optString("threatType", "High Threat Alert"),
                riskLevel = resultJson.optString("riskLevel", "HIGH"),
                conciseAlertMessage = resultJson.optString("conciseAlertMessage", "EMERGENCY: $userName needs urgent help at $locationAddress!"),
                responderSummary = resultJson.optString("responderSummary", "User triggered distress alert. Situation: $situationText"),
                immediateSafetyActions = actionsList,
                urgencyScore = resultJson.optInt("urgencyScore", 90)
            )
        } catch (e: Exception) {
            Log.e("HerShieldAI", "Failed to call Gemini API, falling back safely", e)
            fallbackAnalysis(situationText, userName, locationAddress, medicalNotes)
        }
    }

    /**
     * Resilient offline safety classification engine
     */
    fun fallbackAnalysis(
        situationText: String,
        userName: String,
        locationAddress: String,
        medicalNotes: String
    ): AiSafetyAnalysis {
        val lower = situationText.lowercase().trim()

        val (threat, risk, score, actions) = when {
            lower.contains("follow") || lower.contains("picha") || lower.contains("stalk") || lower.contains("chase") -> {
                ThreatAssessment(
                    threat = "Threat / Following (Stalking)",
                    risk = "CRITICAL",
                    score = 95,
                    actions = listOf(
                        "Head directly into the nearest open shop, metro station or crowded area.",
                        "Call trusted contacts or dial 112 loudly to let perpetrator hear help is arriving.",
                        "Do NOT take secluded alleys or dark shortcuts to home."
                    )
                )
            }
            lower.contains("cab") || lower.contains("taxi") || lower.contains("driver") || lower.contains("route") || lower.contains("auto") -> {
                ThreatAssessment(
                    threat = "Transit / Route Deviation Threat",
                    risk = "CRITICAL",
                    score = 92,
                    actions = listOf(
                        "Demand driver stop at the nearest crowded intersection immediately.",
                        "Turn on device live audio/video and inform driver your route is live-tracked by police.",
                        "Prepare to unlock door at red light or slow speed if safe."
                    )
                )
            }
            lower.contains("harass") || lower.contains("eve teasing") || lower.contains("molest") || lower.contains("touch") -> {
                ThreatAssessment(
                    threat = "Public Harassment / Assault Risk",
                    risk = "HIGH",
                    score = 88,
                    actions = listOf(
                        "Raise alarm loudly: 'Help! Stay away from me!' to draw public attention.",
                        "Move towards security personnel, shopkeepers, or female bystanders.",
                        "Keep phone recording and activate emergency siren."
                    )
                )
            }
            lower.contains("medical") || lower.contains("breath") || lower.contains("faint") || lower.contains("pain") || lower.contains("dizzy") || lower.contains("asthma") -> {
                ThreatAssessment(
                    threat = "Medical Crisis Alert",
                    risk = "HIGH",
                    score = 85,
                    actions = listOf(
                        if (medicalNotes.isNotBlank()) "Notice: $medicalNotes" else "Sit down in safe position to prevent fall injury.",
                        "Ask nearby individuals for immediate ambulance support.",
                        "Keep airways unobstructed and breathe steadily."
                    )
                )
            }
            lower.isNotBlank() -> {
                ThreatAssessment(
                    threat = "Distress Threat: $situationText",
                    risk = "HIGH",
                    score = 89,
                    actions = listOf(
                        "Stay alert and remain in visible, illuminated spaces.",
                        "Maintain active connection with emergency contacts.",
                        "Trust instincts and proceed toward safety hub."
                    )
                )
            }
            else -> {
                ThreatAssessment(
                    threat = "One-Tap Immediate SOS",
                    risk = "CRITICAL",
                    score = 95,
                    actions = listOf(
                        "One-tap distress signal broadcast to all trusted contacts.",
                        "Move towards a populated, secure perimeter immediately.",
                        "Keep phone battery active; live GPS beacon is transmitting."
                    )
                )
            }
        }

        val conciseMessage = "🚨 HerShield SOS: $userName needs URGENT help! Threat: $threat. Location: $locationAddress. Responders alerted."
        val summary = "Distress alert triggered by $userName at $locationAddress. Threat assessed as $threat ($risk priority). Immediate response advised."

        return AiSafetyAnalysis(
            threatType = threat,
            riskLevel = risk,
            conciseAlertMessage = conciseMessage,
            responderSummary = summary,
            immediateSafetyActions = actions,
            urgencyScore = score
        )
    }

    private data class ThreatAssessment(
        val threat: String,
        val risk: String,
        val score: Int,
        val actions: List<String>
    )
}
