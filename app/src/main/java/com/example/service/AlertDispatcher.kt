package com.example.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

data class DispatchedNotification(
    val recipientName: String,
    val phoneNumber: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED"
)

class AlertDispatcher(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private var isAlarmPlaying = false

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val pattern = longArrayOf(0, 300, 150, 300, 150, 500)
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val pattern = longArrayOf(0, 300, 150, 300, 150, 500)
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.w("HerShieldAlert", "Vibration failed: ${e.message}")
        }
    }

    fun startEmergencySiren() {
        if (isAlarmPlaying) return
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 5000)
            isAlarmPlaying = true
        } catch (e: Exception) {
            Log.w("HerShieldAlert", "ToneGenerator siren failed: ${e.message}")
        }
    }

    fun stopEmergencySiren() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
            isAlarmPlaying = false
        } catch (e: Exception) {
            Log.w("HerShieldAlert", "Stop siren error: ${e.message}")
        }
    }

    fun isSirenActive(): Boolean = isAlarmPlaying

    /**
     * Directly places a phone call to emergency contacts without redirecting to the dialer keypad
     * when android.permission.CALL_PHONE is granted. Falls back to ACTION_DIAL if permission is missing.
     */
    fun dialEmergencyContact(phoneNumber: String, forceDirectCall: Boolean = true) {
        try {
            val cleanNumber = phoneNumber.replace(" ", "").replace("-", "").trim()
            val hasCallPermission = context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

            if (forceDirectCall && hasCallPermission) {
                // Direct automatic call without opening/redirecting to dialer app
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$cleanNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
                Log.d("HerShieldAlert", "Automatic direct phone call initiated to $cleanNumber")
            } else {
                // Fallback to dialer
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$cleanNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                Log.d("HerShieldAlert", "Phone dialer launched for $cleanNumber")
            }
        } catch (e: Exception) {
            Log.e("HerShieldAlert", "Could not start call: ${e.message}")
            // Graceful fallback to dialer
            try {
                val cleanNumber = phoneNumber.replace(" ", "").replace("-", "").trim()
                val fallbackIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$cleanNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Log.e("HerShieldAlert", "Dialer fallback failed: ${err.message}")
            }
        }
    }

    /**
     * Instantly shares emergency distress location via SMS or Android share sheet
     */
    fun sendEmergencySms(phoneNumber: String, messageText: String) {
        try {
            val cleanNumber = phoneNumber.replace(" ", "").replace("-", "").trim()
            val hasSmsPermission = context.checkSelfPermission(android.Manifest.permission.SEND_SMS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

            if (hasSmsPermission) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(android.telephony.SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    android.telephony.SmsManager.getDefault()
                }
                val parts = smsManager.divideMessage(messageText)
                smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                Log.d("HerShieldAlert", "Emergency SMS sent directly to $cleanNumber")
            } else {
                // Pre-fill SMS Intent directly
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$cleanNumber")
                    putExtra("sms_body", messageText)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(smsIntent)
            }
        } catch (e: Exception) {
            Log.w("HerShieldAlert", "Send SMS error, opening share chooser: ${e.message}")
            shareEmergencyAlert(messageText)
        }
    }

    fun openMapLocation(latitude: Double, longitude: Double, label: String = "HerShield Emergency") {
        try {
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to browser Google Maps
            try {
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                val webIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Log.e("HerShieldAlert", "Could not open map: ${ex.message}")
            }
        }
    }

    fun shareEmergencyAlert(message: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Broadcast HerShield Emergency Alert").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Log.e("HerShieldAlert", "Share failed: ${e.message}")
        }
    }
}
