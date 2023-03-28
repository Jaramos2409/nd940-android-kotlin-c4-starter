package com.udacity.project4.location

interface CheckLocationManagerInterface {
    fun checkDeviceLocationSettingsAndStartGeofence(
        listener: CheckLocationManager.LocationSettingsListener,
        resolve: Boolean = true
    )

    fun registerLocationProviderChangedReceiver(listener: CheckLocationManager.LocationSettingsListener)
    fun unregisterLocationProviderChangedReceiver()
}
