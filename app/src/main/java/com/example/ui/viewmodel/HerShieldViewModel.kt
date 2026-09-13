package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EmergencyAlertEntity
import com.example.data.local.HerShieldDatabase
import com.example.data.local.TrustedContactEntity
import com.example.data.local.UserProfileEntity
import com.example.data.network.AiSafetyAnalysis
import com.example.data.network.AssistantChatMessage
import com.example.data.network.GeminiSafetyService
import com.example.service.AlertDispatcher
import com.example.service.AudioRecordState
import com.example.service.AudioSafetyRecorder
import com.example.service.CallChainState
import com.example.service.CallOutcome
import com.example.service.ChainCallRecord
import com.example.service.DispatchedNotification
import com.example.service.LocationHelper
import com.example.service.UserLocationResult
import com.example.util.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class HerShieldViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HerShieldDatabase.getDatabase(application, viewModelScope)
    private val dao = database.herShieldDao()
    private val locationHelper = LocationHelper(application)
    private val alertDispatcher = AlertDispatcher(application)
    private val geminiService = GeminiSafetyService()
    private val audioRecorder = AudioSafetyRecorder(application, viewModelScope)

    // SharedPreferences for Theme & Language Persistence
    private val prefs = application.getSharedPreferences("hershield_prefs", Context.MODE_PRIVATE)

    // Dark / Light Theme Mode
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Multi-Language Support
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("selected_language", "en") ?: "en")
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(
        !prefs.getBoolean("has_selected_language", false)
    )
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    // Instant Location Shared Notification
    private val _instantShareNotification = MutableStateFlow<String?>(null)
    val instantShareNotification: StateFlow<String?> = _instantShareNotification.asStateFlow()

    // Open-Source AI Safety Assistant Chat
    private val _chatMessages = MutableStateFlow<List<AssistantChatMessage>>(
        listOf(
            AssistantChatMessage(
                isUser = false,
                message = "Hello! I am Astra, your open-source AI Safety Guardian. You can ask me for urgent de-escalation tips, cab safety protocols, safe night routes, or your legal emergency rights. If you are in danger, tap One-Tap SOS immediately."
            )
        )
    )
    val chatMessages: StateFlow<List<AssistantChatMessage>> = _chatMessages.asStateFlow()

    private val _isAssistantThinking = MutableStateFlow(false)
    val isAssistantThinking: StateFlow<Boolean> = _isAssistantThinking.asStateFlow()

    // Room Database Flows
    val userProfile: StateFlow<UserProfileEntity?> = dao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val trustedContacts: StateFlow<List<TrustedContactEntity>> = dao.getTrustedContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlert: StateFlow<EmergencyAlertEntity?> = dao.getActiveAlert()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAlertsHistory: StateFlow<List<EmergencyAlertEntity>> = dao.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operational UI States
    private val _currentLocation = MutableStateFlow<UserLocationResult?>(null)
    val currentLocation: StateFlow<UserLocationResult?> = _currentLocation.asStateFlow()

    private val _isSosProcessing = MutableStateFlow(false)
    val isSosProcessing: StateFlow<Boolean> = _isSosProcessing.asStateFlow()

    private val _isSirenActive = MutableStateFlow(false)
    val isSirenActive: StateFlow<Boolean> = _isSirenActive.asStateFlow()

    private val _dispatchedLogs = MutableStateFlow<List<DispatchedNotification>>(emptyList())
    val dispatchedLogs: StateFlow<List<DispatchedNotification>> = _dispatchedLogs.asStateFlow()

    private val _latestAiAnalysis = MutableStateFlow<AiSafetyAnalysis?>(null)
    val latestAiAnalysis: StateFlow<AiSafetyAnalysis?> = _latestAiAnalysis.asStateFlow()

    private val _isAiClassifying = MutableStateFlow(false)
    val isAiClassifying: StateFlow<Boolean> = _isAiClassifying.asStateFlow()

    // 1-Minute Audio Recording State
    val audioRecordState: StateFlow<AudioRecordState> = audioRecorder.recordingState

    // Live Real-Time GPS Tracking State
    private var liveGpsJob: Job? = null
    private val _isLiveGpsTracking = MutableStateFlow(false)
    val isLiveGpsTracking: StateFlow<Boolean> = _isLiveGpsTracking.asStateFlow()

    private val _liveBreadcrumbs = MutableStateFlow<List<UserLocationResult>>(emptyList())
    val liveBreadcrumbs: StateFlow<List<UserLocationResult>> = _liveBreadcrumbs.asStateFlow()

    // Emergency Contact Calling Chain State
    private val _callChainState = MutableStateFlow(CallChainState())
    val callChainState: StateFlow<CallChainState> = _callChainState.asStateFlow()
    private var callChainTimerJob: Job? = null

    // Demo Flow Step Tracking (1 through 7)
    private val _currentDemoStep = MutableStateFlow(1)
    val currentDemoStep: StateFlow<Int> = _currentDemoStep.asStateFlow()

    init {
        // Fetch initial location quietly
        refreshLocation()
        // Start continuous live tracking
        startLiveLocationTracking()
    }

    fun setDemoStep(step: Int) {
        _currentDemoStep.value = step.coerceIn(1, 7)
    }

    // ==========================================
    // Real-Time Live GPS Functionality
    // ==========================================
    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            _currentLocation.value = loc
            val currentList = _liveBreadcrumbs.value.toMutableList()
            currentList.add(loc)
            _liveBreadcrumbs.value = currentList
        }
    }

    fun startLiveLocationTracking() {
        if (liveGpsJob?.isActive == true) return
        _isLiveGpsTracking.value = true

        liveGpsJob = viewModelScope.launch {
            try {
                locationHelper.getLiveLocationFlow().collect { loc ->
                    _currentLocation.value = loc
                    val list = _liveBreadcrumbs.value.toMutableList()
                    list.add(loc)
                    if (list.size > 50) list.removeAt(0)
                    _liveBreadcrumbs.value = list

                    // If an active alert is in progress, update its live coordinates in DB
                    activeAlert.value?.let { alert ->
                        dao.updateAlertLocation(alert.id, loc.latitude, loc.longitude, loc.address)
                    }
                }
            } catch (e: Exception) {
                Log.w("HerShieldVM", "Live tracking collection notice: ${e.message}")
            }
        }
    }

    fun stopLiveLocationTracking() {
        liveGpsJob?.cancel()
        liveGpsJob = null
        _isLiveGpsTracking.value = false
    }

    // ==========================================
    // Emergency Trigger with Auto Audio & Call Chain
    // ==========================================
    fun triggerEmergencySos(customSituationText: String? = null) {
        if (_isSosProcessing.value) return
        _isSosProcessing.value = true

        viewModelScope.launch {
            try {
                // 1. Tactile alert & ensure live GPS tracking is running
                alertDispatcher.triggerHapticFeedback()
                startLiveLocationTracking()

                val loc = _currentLocation.value ?: locationHelper.getCurrentLocation()
                _currentLocation.value = loc

                val profile = userProfile.value ?: UserProfileEntity()
                val situationDesc = customSituationText?.takeIf { it.isNotBlank() }
                    ?: "One-Tap SOS initiated. Automatic 1-min audio security analysis started."

                // 2. Initial AI Evaluation
                _isAiClassifying.value = true
                val initialAnalysis = geminiService.analyzeEmergencySituation(
                    situationText = situationDesc,
                    userName = profile.fullName,
                    locationAddress = loc.address,
                    medicalNotes = profile.medicalNotes
                )
                _latestAiAnalysis.value = initialAnalysis
                _isAiClassifying.value = false

                // 3. Save Alert Record in Room
                val contacts = trustedContacts.value
                val newAlert = EmergencyAlertEntity(
                    timestamp = System.currentTimeMillis(),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    address = loc.address,
                    situationDescription = situationDesc,
                    threatType = initialAnalysis.threatType,
                    riskLevel = initialAnalysis.riskLevel,
                    generatedAlertMessage = initialAnalysis.conciseAlertMessage,
                    responderSummary = initialAnalysis.responderSummary,
                    recommendedActions = initialAnalysis.immediateSafetyActions.joinToString("\n• "),
                    contactsNotifiedCount = contacts.size,
                    policeCalled = false,
                    status = "ACTIVE"
                )
                val insertedAlertId = dao.insertAlert(newAlert)

                // 3b. Instant Location Sharing with Primary Contact via Google Maps
                val primaryContact = contacts.firstOrNull { it.isPrimary } ?: contacts.firstOrNull() ?: getPrioritizedContactsList().firstOrNull()
                val googleMapsUrl = "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                val instantSmsBody = "🚨 HER SHIELD EMERGENCY SOS: ${profile.fullName} is in active distress!\nLive Google Maps location: $googleMapsUrl\nNear: ${loc.address}\nThreat Assessment: ${initialAnalysis.threatType}"

                if (primaryContact != null) {
                    alertDispatcher.sendEmergencySms(primaryContact.phoneNumber, instantSmsBody)
                    _instantShareNotification.value = "📍 Live Google Maps Location instantly shared with Primary Contact (${primaryContact.name}: ${primaryContact.phoneNumber})!"
                }

                // 4. Build initial dispatched SMS/Notification list
                val dispatchList = mutableListOf<DispatchedNotification>()
                contacts.forEach { contact ->
                    dispatchList.add(
                        DispatchedNotification(
                            recipientName = contact.name,
                            phoneNumber = contact.phoneNumber,
                            messageText = initialAnalysis.conciseAlertMessage,
                            timestamp = System.currentTimeMillis(),
                            status = "SENT & DELIVERED"
                        )
                    )
                }
                dispatchList.add(
                    DispatchedNotification(
                        recipientName = "Emergency Services (112)",
                        phoneNumber = profile.policeEmergencyNumber,
                        messageText = "SOS Alert: Location ${loc.address}. Threat: ${initialAnalysis.threatType}",
                        timestamp = System.currentTimeMillis(),
                        status = "DISPATCH READY"
                    )
                )
                _dispatchedLogs.value = dispatchList

                // 5. Automatic 1-Minute Voice Recording Feature
                startAutomaticVoiceRecording(insertedAlertId, profile, loc)

                // 6. Emergency Contacts Daisy Chain Feature
                startEmergencyCallingChain()

                if (_currentDemoStep.value < 6) {
                    _currentDemoStep.value = 6
                }
            } catch (e: Exception) {
                Log.e("HerShieldVM", "Trigger SOS failed: ${e.message}", e)
            } finally {
                _isSosProcessing.value = false
            }
        }
    }

    // ==========================================
    // Feature 1: Automatic 1-Minute Voice Recording & AI Situation Extraction
    // ==========================================
    private fun startAutomaticVoiceRecording(
        alertId: Long,
        profile: UserProfileEntity,
        loc: UserLocationResult
    ) {
        audioRecorder.startOneMinuteEmergencyRecording { recordedFile, durationSec, isSimulated ->
            viewModelScope.launch {
                handleAudioRecordingCompleted(alertId, recordedFile, durationSec, isSimulated, profile, loc)
            }
        }
    }

    fun startManualVoiceRecording() {
        val profile = userProfile.value ?: UserProfileEntity()
        val loc = _currentLocation.value ?: locationHelper.getSimulatedFallbackLocation()
        val activeId = activeAlert.value?.id ?: 0L

        audioRecorder.startOneMinuteEmergencyRecording { recordedFile, durationSec, isSimulated ->
            viewModelScope.launch {
                handleAudioRecordingCompleted(activeId, recordedFile, durationSec, isSimulated, profile, loc)
            }
        }
    }

    fun stopVoiceRecordingAndAnalyze() {
        val profile = userProfile.value ?: UserProfileEntity()
        val loc = _currentLocation.value ?: locationHelper.getSimulatedFallbackLocation()
        val activeId = activeAlert.value?.id ?: 0L

        audioRecorder.stopAndAnalyzeNow { recordedFile, durationSec, isSimulated ->
            viewModelScope.launch {
                handleAudioRecordingCompleted(activeId, recordedFile, durationSec, isSimulated, profile, loc)
            }
        }
    }

    fun cancelVoiceRecording() {
        audioRecorder.cancelRecording()
    }

    private suspend fun handleAudioRecordingCompleted(
        alertId: Long,
        recordedFile: File?,
        durationSec: Int,
        isSimulated: Boolean,
        profile: UserProfileEntity,
        loc: UserLocationResult
    ) {
        _isAiClassifying.value = true
        try {
            // Send audio file to Gemini AI to extract contextual situation
            val audioAnalysis = geminiService.analyzeAudioRecording(
                audioFile = recordedFile,
                recordedDurationSeconds = durationSec,
                isSimulated = isSimulated,
                userName = profile.fullName,
                locationAddress = loc.address,
                medicalNotes = profile.medicalNotes
            )
            _latestAiAnalysis.value = audioAnalysis

            // Update the Room database alert record with the newly extracted situational context
            if (alertId > 0) {
                val fullSituation = if (audioAnalysis.contextualSituation.isNotBlank()) {
                    "[Voice AI Analyzed (${durationSec}s)]: ${audioAnalysis.contextualSituation}"
                } else {
                    "Voice distress signal captured (${durationSec}s)."
                }

                dao.updateAlertSituationWithAi(
                    id = alertId,
                    situation = fullSituation,
                    threat = audioAnalysis.threatType,
                    risk = audioAnalysis.riskLevel,
                    summary = audioAnalysis.responderSummary,
                    alertMsg = audioAnalysis.conciseAlertMessage
                )
            }

            // Update SMS logs with new audio contextual dispatch
            val updatedLogs = _dispatchedLogs.value.map { log ->
                log.copy(
                    messageText = audioAnalysis.conciseAlertMessage,
                    status = "UPDATED (AUDIO CONTEXT VERIFIED)"
                )
            }
            _dispatchedLogs.value = updatedLogs
        } catch (e: Exception) {
            Log.e("HerShieldVM", "Audio context analysis failure: ${e.message}", e)
        } finally {
            _isAiClassifying.value = false
        }
    }

    // ==========================================
    // Feature 2: Emergency Contact Daisy Chain Calling
    // ==========================================
    fun startEmergencyCallingChain() {
        val contacts = getPrioritizedContactsList()
        if (contacts.isEmpty()) {
            _callChainState.value = CallChainState(
                isActive = false,
                statusMessage = "No emergency contacts configured."
            )
            return
        }

        callChainTimerJob?.cancel()
        val primaryContact = contacts.first()

        _callChainState.value = CallChainState(
            isActive = true,
            currentContactIndex = 0,
            currentContact = primaryContact,
            cycleNumber = 1,
            timeoutSecondsRemaining = 25,
            totalContactsInQueue = contacts.size,
            isConnected = false,
            isPaused = false,
            statusMessage = "Calling Primary: ${primaryContact.name} (${primaryContact.relationship})...",
            callHistory = listOf(
                ChainCallRecord(
                    contactName = primaryContact.name,
                    phoneNumber = primaryContact.phoneNumber,
                    relationship = primaryContact.relationship,
                    attemptNumber = 1,
                    cycleNumber = 1,
                    outcome = CallOutcome.DIALING
                )
            )
        )

        // Trigger phone dialer for the primary contact
        alertDispatcher.dialEmergencyContact(primaryContact.phoneNumber)

        // Launch the 25-second countdown timer for auto-escalation
        launchCallChainTimer()
    }

    private fun launchCallChainTimer() {
        callChainTimerJob?.cancel()
        callChainTimerJob = viewModelScope.launch {
            val totalSeconds = 25
            for (sec in totalSeconds downTo 1) {
                if (!isActive) break

                if (_callChainState.value.isPaused || _callChainState.value.isConnected) {
                    delay(1000L)
                    continue
                }

                _callChainState.value = _callChainState.value.copy(
                    timeoutSecondsRemaining = sec
                )
                delay(1000L)
            }

            // Timeout reached without answer: automatically advance to the next person in chain
            escalateToNextContact(CallOutcome.NO_ANSWER_ESCALATED)
        }
    }

    fun escalateToNextContact(outcome: CallOutcome = CallOutcome.NO_ANSWER_ESCALATED) {
        val contacts = getPrioritizedContactsList()
        if (contacts.isEmpty() || !_callChainState.value.isActive) return

        val currentState = _callChainState.value
        val currentIndex = currentState.currentContactIndex
        val currentContact = currentState.currentContact

        // Update outcome for previous contact
        val updatedHistory = currentState.callHistory.mapIndexed { idx, record ->
            if (idx == currentState.callHistory.lastIndex) {
                record.copy(outcome = outcome)
            } else record
        }.toMutableList()

        val nextIndexRaw = currentIndex + 1
        val (nextIndex, newCycleNumber, cycleMessage) = if (nextIndexRaw >= contacts.size) {
            // Queue used up! Loop back to primary contact (Index 0)
            val nextCycle = currentState.cycleNumber + 1
            Triple(0, nextCycle, "Queue completed! Looping back to Primary Contact (Cycle $nextCycle)...")
        } else {
            Triple(nextIndexRaw, currentState.cycleNumber, "No answer. Escalating to contact #${nextIndexRaw + 1}...")
        }

        val nextContact = contacts[nextIndex]
        val nextAttemptNumber = updatedHistory.size + 1

        updatedHistory.add(
            ChainCallRecord(
                contactName = nextContact.name,
                phoneNumber = nextContact.phoneNumber,
                relationship = nextContact.relationship,
                attemptNumber = nextAttemptNumber,
                cycleNumber = newCycleNumber,
                outcome = CallOutcome.DIALING
            )
        )

        _callChainState.value = currentState.copy(
            currentContactIndex = nextIndex,
            currentContact = nextContact,
            cycleNumber = newCycleNumber,
            timeoutSecondsRemaining = 25,
            isConnected = false,
            statusMessage = "$cycleMessage Calling ${nextContact.name} (${nextContact.relationship})",
            callHistory = updatedHistory
        )

        // Trigger phone dialer for next contact
        alertDispatcher.dialEmergencyContact(nextContact.phoneNumber)

        // Restart timer countdown for the new contact
        launchCallChainTimer()
    }

    fun markCurrentCallConnected() {
        callChainTimerJob?.cancel()
        val currentState = _callChainState.value
        val contactName = currentState.currentContact?.name ?: "Contact"

        val updatedHistory = currentState.callHistory.mapIndexed { idx, record ->
            if (idx == currentState.callHistory.lastIndex) {
                record.copy(outcome = CallOutcome.ANSWERED_CONNECTED)
            } else record
        }

        _callChainState.value = currentState.copy(
            isConnected = true,
            isPaused = true,
            statusMessage = "✅ Connected with $contactName! Calling chain paused.",
            callHistory = updatedHistory
        )
    }

    fun togglePauseCallingChain() {
        val currentState = _callChainState.value
        if (!currentState.isActive) return
        val newPaused = !currentState.isPaused
        _callChainState.value = currentState.copy(
            isPaused = newPaused,
            statusMessage = if (newPaused) "Calling chain paused by user" else "Resumed calling countdown..."
        )
    }

    fun stopEmergencyCallingChain() {
        callChainTimerJob?.cancel()
        callChainTimerJob = null
        val currentState = _callChainState.value
        val updatedHistory = currentState.callHistory.mapIndexed { idx, record ->
            if (idx == currentState.callHistory.lastIndex && record.outcome == CallOutcome.DIALING) {
                record.copy(outcome = CallOutcome.CANCELLED)
            } else record
        }
        _callChainState.value = currentState.copy(
            isActive = false,
            statusMessage = "Calling chain ended.",
            callHistory = updatedHistory
        )
    }

    private fun getPrioritizedContactsList(): List<TrustedContactEntity> {
        val userContacts = trustedContacts.value
        if (userContacts.isNotEmpty()) {
            return userContacts.sortedBy { it.priorityOrder }
        }
        // Fallback default responder chain if user hasn't added contacts yet
        val profile = userProfile.value ?: UserProfileEntity()
        return listOf(
            TrustedContactEntity(
                name = "Rajesh Sharma (Primary)",
                phoneNumber = "+91 98111 22334",
                relationship = "Father",
                priorityOrder = 1,
                isPrimary = true
            ),
            TrustedContactEntity(
                name = "Sunita Sharma",
                phoneNumber = "+91 98222 33445",
                relationship = "Mother",
                priorityOrder = 2,
                isPrimary = false
            ),
            TrustedContactEntity(
                name = "Police Dispatch (112)",
                phoneNumber = profile.policeEmergencyNumber,
                relationship = "Emergency Services",
                priorityOrder = 3,
                isPrimary = false
            )
        )
    }

    // ==========================================
    // Alerts, Siren, and Controls
    // ==========================================
    fun toggleSiren() {
        if (_isSirenActive.value) {
            alertDispatcher.stopEmergencySiren()
            _isSirenActive.value = false
        } else {
            alertDispatcher.startEmergencySiren()
            _isSirenActive.value = true
        }
    }

    fun stopSiren() {
        alertDispatcher.stopEmergencySiren()
        _isSirenActive.value = false
    }

    fun resolveActiveAlert(alertId: Long) {
        viewModelScope.launch {
            dao.markAlertResolved(alertId)
            stopSiren()
            stopEmergencyCallingChain()
            cancelVoiceRecording()
        }
    }

    fun resolveAllAlerts() {
        viewModelScope.launch {
            dao.markAllAlertsResolved()
            stopSiren()
            stopEmergencyCallingChain()
            cancelVoiceRecording()
        }
    }

    fun analyzeCustomSituation(text: String) {
        if (text.isBlank() || _isAiClassifying.value) return
        _isAiClassifying.value = true

        viewModelScope.launch {
            try {
                val profile = userProfile.value ?: UserProfileEntity()
                val loc = _currentLocation.value ?: locationHelper.getCurrentLocation()
                val analysis = geminiService.analyzeEmergencySituation(
                    situationText = text,
                    userName = profile.fullName,
                    locationAddress = loc.address,
                    medicalNotes = profile.medicalNotes
                )
                _latestAiAnalysis.value = analysis
                _currentDemoStep.value = 7
            } finally {
                _isAiClassifying.value = false
            }
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            dao.saveUserProfile(profile)
            if (_currentDemoStep.value == 1) {
                _currentDemoStep.value = 2
            }
        }
    }

    fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
        viewModelScope.launch {
            val count = trustedContacts.value.size
            val newContact = TrustedContactEntity(
                name = name,
                phoneNumber = phone,
                relationship = relationship,
                priorityOrder = count + 1,
                isPrimary = isPrimary
            )
            dao.insertContact(newContact)
            if (_currentDemoStep.value == 2) {
                _currentDemoStep.value = 3
            }
        }
    }

    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            dao.deleteContact(contactId)
        }
    }

    fun dialPolice() {
        val number = userProfile.value?.policeEmergencyNumber ?: "112"
        alertDispatcher.dialEmergencyContact(number)
    }

    fun dialContact(phoneNumber: String) {
        alertDispatcher.dialEmergencyContact(phoneNumber)
    }

    fun openCurrentLocationOnMap(lat: Double, lng: Double, label: String = "Distress Location") {
        alertDispatcher.openMapLocation(lat, lng, label)
    }

    fun shareAlert(message: String) {
        alertDispatcher.shareEmergencyAlert(message)
    }

    // ==========================================
    // Theme & Language Controls
    // ==========================================
    fun toggleDarkTheme() {
        val nextMode = !_isDarkTheme.value
        _isDarkTheme.value = nextMode
        prefs.edit().putBoolean("is_dark_theme", nextMode).apply()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun selectLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit()
            .putString("selected_language", language.code)
            .putBoolean("has_selected_language", true)
            .apply()
        _showLanguageDialog.value = false
    }

    fun openLanguageSelector() {
        _showLanguageDialog.value = true
    }

    fun dismissLanguageDialog() {
        _showLanguageDialog.value = false
        prefs.edit().putBoolean("has_selected_language", true).apply()
    }

    fun dismissInstantShareNotification() {
        _instantShareNotification.value = null
    }

    // ==========================================
    // Interactive Open-Source AI Safety Assistant
    // ==========================================
    fun sendAssistantMessage(userQuery: String) {
        val text = userQuery.trim()
        if (text.isBlank() || _isAssistantThinking.value) return

        val userMsg = AssistantChatMessage(isUser = true, message = text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAssistantThinking.value = true

        viewModelScope.launch {
            try {
                val profile = userProfile.value ?: UserProfileEntity()
                val loc = _currentLocation.value ?: locationHelper.getCurrentLocation()
                val response = geminiService.chatWithSafetyAssistant(
                    userMessage = text,
                    conversationHistory = _chatMessages.value,
                    userName = profile.fullName,
                    locationAddress = loc.address
                )
                val assistantMsg = AssistantChatMessage(
                    isUser = false,
                    message = response,
                    isEmergencyActionable = response.contains("One-Tap SOS", ignoreCase = true) || response.contains("112")
                )
                _chatMessages.value = _chatMessages.value + assistantMsg
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + AssistantChatMessage(
                    isUser = false,
                    message = "I encountered an error connecting to safety services. Stay in well-lit areas, keep your phone ready, and tap One-Tap SOS if threatened."
                )
            } finally {
                _isAssistantThinking.value = false
            }
        }
    }

    fun clearAssistantChat() {
        _chatMessages.value = listOf(
            AssistantChatMessage(
                isUser = false,
                message = "Chat reset. How can Astra assist your safety today?"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        alertDispatcher.stopEmergencySiren()
        audioRecorder.cancelRecording()
        callChainTimerJob?.cancel()
        liveGpsJob?.cancel()
    }
}

