package com.udacity.project4

import android.content.Context
import com.udacity.project4.location.CheckLocationManager
import com.udacity.project4.location.CheckLocationManagerInterface

class FakeCheckLocationManager(
    private val context: Context,
    private var locationSettingsEnabled: Boolean = false
) : CheckLocationManagerInterface {

    override fun checkDeviceLocationSettingsAndStartGeofence(
        listener: CheckLocationManager.LocationSettingsListener,
        resolve: Boolean
    ) {
        if (!locationSettingsEnabled) {
            (context as CheckLocationManager.LocationSettingsListener).onLocationSettingsFailed(
                Exception("Location settings are disabled")
            )
        }
    }

    override fun registerLocationProviderChangedReceiver(listener: CheckLocationManager.LocationSettingsListener) {}

    override fun unregisterLocationProviderChangedReceiver() {}
}