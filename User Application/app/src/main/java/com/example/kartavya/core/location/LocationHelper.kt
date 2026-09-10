package com.example.kartavya.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.Locale

fun fetchDeviceLocation(
    context: Context,
    onLocationFetched: (locationName: String, lat: Double?, lng: Double?) -> Unit
) {
    val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    if (finePerm != PackageManager.PERMISSION_GRANTED && coarsePerm != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    try {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        var bestLocation: Location? = null
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                    bestLocation = loc
                }
            }
        }

        if (bestLocation != null) {
            val lat = bestLocation.latitude
            val lng = bestLocation.longitude
            val geocoder = Geocoder(context, Locale.getDefault())
            var addressText = "Lat: %.4f, Lng: %.4f".format(lat, lng)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                            val subAdmin = addr.subAdminArea ?: addr.adminArea ?: ""
                            val formatted = listOf(thoroughfare, subAdmin).filter { it.isNotBlank() }.joinToString(", ")
                            if (formatted.isNotBlank()) addressText = formatted
                        }
                        Handler(Looper.getMainLooper()).post {
                            onLocationFetched(addressText, lat, lng)
                        }
                    }
                    return
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: addr.locality ?: ""
                        val subAdmin = addr.subAdminArea ?: addr.adminArea ?: ""
                        val formatted = listOf(thoroughfare, subAdmin).filter { it.isNotBlank() }.joinToString(", ")
                        if (formatted.isNotBlank()) addressText = formatted
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onLocationFetched(addressText, lat, lng)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}
