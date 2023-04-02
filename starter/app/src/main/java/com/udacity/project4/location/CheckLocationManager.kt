package com.udacity.project4.location

import android.content.*
import android.location.LocationManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import timber.log.Timber

class CheckLocationManager(
    private val context: Context,
    private val requestDeviceLocationLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): CheckLocationManagerInterface {

    interface LocationSettingsListener {
        fun onLocationSettingsEnabled()
        fun onLocationSettingsFailed(exception: Exception)
    }

    private var listener: LocationSettingsListener? = null
    private val settingsClient = LocationServices.getSettingsClient(context)

    private val locationProviderChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                listener?.let { checkDeviceLocationSettings(it) }
            }
        }
    }

    override fun registerLocationProviderChangedReceiver(listener: LocationSettingsListener) {
        this.listener = listener
        context.registerReceiver(
            locationProviderChangedReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    override fun unregisterLocationProviderChangedReceiver() {
        context.unregisterReceiver(locationProviderChangedReceiver)
        listener = null
    }

    override fun checkDeviceLocationSettings(listener: LocationSettingsListener, resolve: Boolean) {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_LOW_POWER, 3600000L)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    requestDeviceLocationLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.d("Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                listener.onLocationSettingsFailed(exception)
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.i("Location settings are enabled.")
                listener.onLocationSettingsEnabled()
            }
        }
    }

}
