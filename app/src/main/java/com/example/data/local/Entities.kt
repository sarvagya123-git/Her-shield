package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Priya Sharma",
    val phoneNumber: String = "+91 98765 43210",
    val age: String = "23",
    val bloodGroup: String = "B+",
    val allergies: String = "Penicillin, Dust",
    val medicalNotes: String = "Asthmatic (inhaler usually in bag)",
    val policeEmergencyNumber: String = "112",
    val safeHomeAddress: String = "B-42, Metro Green Enclave, Sector 18",
    val isRegistered: Boolean = true
)

@Entity(tableName = "trusted_contacts")
data class TrustedContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val priorityOrder: Int = 1,
    val isPrimary: Boolean = false
)

@Entity(tableName = "emergency_alerts")
data class EmergencyAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val situationDescription: String,
    val threatType: String = "General Emergency",
    val riskLevel: String = "HIGH", // CRITICAL, HIGH, MEDIUM
    val generatedAlertMessage: String,
    val responderSummary: String,
    val recommendedActions: String,
    val contactsNotifiedCount: Int = 0,
    val policeCalled: Boolean = false,
    val status: String = "ACTIVE" // ACTIVE, RESOLVED
)
