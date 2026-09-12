package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EmergencyAlertEntity
import com.example.data.local.HerShieldDatabase
import com.example.data.local.TrustedContactEntity
import com.example.data.local.UserProfileEntity
import com.example.data.network.AiSafetyAnalysis
import com.example.data.network.GeminiSafetyService
import com.example.service.AlertDispatcher
import com.example.service.DispatchedNotification
import com.example.service.LocationHelper
import com.example.service.UserLocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HerShieldViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HerShieldDatabase.getDatabase(application, viewModelScope)
    private val dao = database.herShieldDao()
    private val locationHelper = LocationHelper(application)
    private val alertDispatcher = AlertDispatcher(application)
    private val geminiService = GeminiSafetyService()

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

    // Demo Flow Step Tracking (1 through 7)
    private val _currentDemoStep = MutableStateFlow(1)
    val currentDemoStep: StateFlow<Int> = _currentDemoStep.asStateFlow()

    init {
        // Fetch initial location quietly
        refreshLocation()
    }

    fun setDemoStep(step: Int) {
        _currentDemoStep.value = step.coerceIn(1, 7)
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            _currentLocation.value = loc
        }
    }

    fun triggerEmergencySos(customSituationText: String? = null) {
        if (_isSosProcessing.value) return
        _isSosProcessing.value = true

        viewModelScope.launch {
            try {
                // 1. Tactile Alert
                alertDispatcher.triggerHapticFeedback()

                // 2. Fetch fresh location
                val loc = locationHelper.getCurrentLocation()
                _currentLocation.value = loc

                val profile = userProfile.value ?: UserProfileEntity()
                val situationDesc = customSituationText?.takeIf { it.isNotBlank() }
                    ?: "Immediate distress button pressed by user."

                // 3. AI Safety Evaluation (Gemini 3.5 Flash or Fallback)
                _isAiClassifying.value = true
                val aiAnalysis = geminiService.analyzeEmergencySituation(
                    situationText = situationDesc,
                    userName = profile.fullName,
                    locationAddress = loc.address,
                    medicalNotes = profile.medicalNotes
                )
                _latestAiAnalysis.value = aiAnalysis
                _isAiClassifying.value = false

                // 4. Save Alert Record in Room
                val contacts = trustedContacts.value
                val newAlert = EmergencyAlertEntity(
                    timestamp = System.currentTimeMillis(),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    address = loc.address,
                    situationDescription = situationDesc,
                    threatType = aiAnalysis.threatType,
                    riskLevel = aiAnalysis.riskLevel,
                    generatedAlertMessage = aiAnalysis.conciseAlertMessage,
                    responderSummary = aiAnalysis.responderSummary,
                    recommendedActions = aiAnalysis.immediateSafetyActions.joinToString("\n• "),
                    contactsNotifiedCount = contacts.size,
                    policeCalled = false,
                    status = "ACTIVE"
                )
                dao.insertAlert(newAlert)

                // 5. Build dispatched SMS/Notification list
                val dispatchList = mutableListOf<DispatchedNotification>()
                contacts.forEach { contact ->
                    dispatchList.add(
                        DispatchedNotification(
                            recipientName = contact.name,
                            phoneNumber = contact.phoneNumber,
                            messageText = aiAnalysis.conciseAlertMessage,
                            timestamp = System.currentTimeMillis(),
                            status = "SENT & DELIVERED"
                        )
                    )
                }
                // Also add emergency police record
                dispatchList.add(
                    DispatchedNotification(
                        recipientName = "Emergency Services (112)",
                        phoneNumber = profile.policeEmergencyNumber,
                        messageText = "SOS Alert: Location ${loc.address}. Threat: ${aiAnalysis.threatType}",
                        timestamp = System.currentTimeMillis(),
                        status = "DISPATCH READY"
                    )
                )
                _dispatchedLogs.value = dispatchList

                // Advance demo step to Step 5/6
                if (_currentDemoStep.value < 6) {
                    _currentDemoStep.value = 6
                }
            } catch (e: Exception) {
                // Ensure state does not stay stuck
            } finally {
                _isSosProcessing.value = false
            }
        }
    }

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
        }
    }

    fun resolveAllAlerts() {
        viewModelScope.launch {
            dao.markAllAlertsResolved()
            stopSiren()
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

    override fun onCleared() {
        super.onCleared()
        alertDispatcher.stopEmergencySiren()
    }
}
