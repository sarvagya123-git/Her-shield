package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        TrustedContactEntity::class,
        EmergencyAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HerShieldDatabase : RoomDatabase() {
    abstract fun herShieldDao(): HerShieldDao

    companion object {
        @Volatile
        private var INSTANCE: HerShieldDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HerShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HerShieldDatabase::class.java,
                    "hershield_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.herShieldDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: HerShieldDao) {
                // Prepopulate User Profile
                val defaultProfile = UserProfileEntity(
                    id = 1,
                    fullName = "Ananya Roy",
                    phoneNumber = "+91 98765 12345",
                    age = "24",
                    bloodGroup = "O+",
                    allergies = "None reported",
                    medicalNotes = "Asthma inhaler in purse",
                    policeEmergencyNumber = "112",
                    safeHomeAddress = "Apartment 402, Lotus Heights, Park Road",
                    isRegistered = true
                )
                dao.saveUserProfile(defaultProfile)

                // Prepopulate Trusted Contacts
                val defaultContacts = listOf(
                    TrustedContactEntity(
                        name = "Mom (Sunita Roy)",
                        phoneNumber = "+91 98111 22334",
                        relationship = "Mother",
                        priorityOrder = 1,
                        isPrimary = true
                    ),
                    TrustedContactEntity(
                        name = "Sister (Neha Roy)",
                        phoneNumber = "+91 98222 33445",
                        relationship = "Sister",
                        priorityOrder = 2,
                        isPrimary = false
                    ),
                    TrustedContactEntity(
                        name = "Rohan Verma",
                        phoneNumber = "+91 98333 44556",
                        relationship = "Friend / Flatmate",
                        priorityOrder = 3,
                        isPrimary = false
                    )
                )
                dao.insertContacts(defaultContacts)
            }
        }
    }
}
