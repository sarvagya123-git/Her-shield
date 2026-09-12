package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HerShieldDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Trusted Contacts
    @Query("SELECT * FROM trusted_contacts ORDER BY priorityOrder ASC, id ASC")
    fun getTrustedContacts(): Flow<List<TrustedContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<TrustedContactEntity>)

    @Update
    suspend fun updateContact(contact: TrustedContactEntity)

    @Query("DELETE FROM trusted_contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)

    // Emergency Alerts History & Active
    @Query("SELECT * FROM emergency_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<EmergencyAlertEntity>>

    @Query("SELECT * FROM emergency_alerts WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    fun getActiveAlert(): Flow<EmergencyAlertEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: EmergencyAlertEntity): Long

    @Query("UPDATE emergency_alerts SET status = 'RESOLVED' WHERE id = :id")
    suspend fun markAlertResolved(id: Long)

    @Query("UPDATE emergency_alerts SET status = 'RESOLVED'")
    suspend fun markAllAlertsResolved()

    @Query("DELETE FROM emergency_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)
}
