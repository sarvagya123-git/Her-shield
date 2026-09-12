package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val accuracyMeters: Float,
    val isRealGps: Boolean
)

class LocationHelper(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocationResult = withContext(Dispatchers.IO) {
        val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return@withContext getSimulatedFallbackLocation("Permission not granted (Simulated Secure Location)")
        }

        try {
            val location = suspendCancellableCoroutine { cont ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // Fallback to lastLocation
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            }

            if (location != null) {
                val addressName = reverseGeocode(location.latitude, location.longitude)
                UserLocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = addressName,
                    accuracyMeters = location.accuracy,
                    isRealGps = true
                )
            } else {
                getSimulatedFallbackLocation("GPS signal weak (Simulated Location)")
            }
        } catch (e: Exception) {
            getSimulatedFallbackLocation("GPS exception fallback: ${e.message}")
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(addresses?.firstOrNull(), lat, lng)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                formatAddress(addresses?.firstOrNull(), lat, lng)
            }
        } catch (e: Exception) {
            "Lat: %.4f, Lng: %.4f (Near City Center)".format(lat, lng)
        }
    }

    private fun formatAddress(address: Address?, lat: Double, lng: Double): String {
        if (address == null) return "Lat: %.4f, Lng: %.4f".format(lat, lng)
        val thoroughfare = address.thoroughfare ?: address.subLocality ?: ""
        val locality = address.locality ?: address.subAdminArea ?: ""
        val admin = address.adminArea ?: ""
        val full = listOf(thoroughfare, locality, admin).filter { it.isNotBlank() }.joinToString(", ")
        return if (full.isNotBlank()) full else address.getAddressLine(0) ?: "Lat: %.4f, Lng: %.4f".format(lat, lng)
    }

    fun getSimulatedFallbackLocation(reason: String = ""): UserLocationResult {
        return UserLocationResult(
            latitude = 28.6315,
            longitude = 77.2167,
            address = "Radial Road 4, Connaught Place, New Delhi",
            accuracyMeters = 8.5f,
            isRealGps = false
        )
    }
}
