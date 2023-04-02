package com.udacity.project4.location

class FakeCheckLocationManager(
    private var locationSettingsEnabled: Boolean = false
) : CheckLocationManagerInterface {

    override fun checkDeviceLocationSettings(
        listener: CheckLocationManager.LocationSettingsListener,
        resolve: Boolean
    ) {
        if (!locationSettingsEnabled) {
            listener.onLocationSettingsFailed(Exception("Location settings are disabled"))
        }
    }

    override fun registerLocationProviderChangedReceiver(listener: CheckLocationManager.LocationSettingsListener) {}

    override fun unregisterLocationProviderChangedReceiver() {}
}