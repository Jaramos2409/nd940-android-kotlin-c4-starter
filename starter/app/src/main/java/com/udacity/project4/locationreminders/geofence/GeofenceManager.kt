package com.udacity.project4.locationreminders.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.asListOfDomainModel
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.PermissionUtils
import com.udacity.project4.utils.getBroadcast
import kotlinx.coroutines.*
import timber.log.Timber

class GeofenceManager(private val context: Context, private val dataSource: ReminderDataSource) {

    private val geofenceManagerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun addGeofencesFromDatabase() = geofenceManagerScope.launch {
        when (val fetchedActiveReminders = dataSource.getReminders()) {
            is com.udacity.project4.locationreminders.data.dto.Result.Success -> {
                if (fetchedActiveReminders.data.isNotEmpty())
                    addGeofences(fetchedActiveReminders.data.asListOfDomainModel())
            }
            else -> {
                return@launch
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun addGeofences(geofences: List<ReminderDataItem>) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofencingRequest = createGeofencingRequest(geofences)
        val geofencePendingIntent = createGeofencePendingIntent()

        if (PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Timber.i("Geofence added")
                }
                .addOnFailureListener { exception ->
                    Timber.e("Issue adding geofence: ${exception.message}")
                }
        }
    }

    private fun createGeofencingRequest(geofences: List<ReminderDataItem>): GeofencingRequest {
        val geofenceList = geofences.map { geofence ->
            Geofence.Builder()
                .setRequestId(geofence.id)
                .setCircularRegion(
                    geofence.latitude!!,
                    geofence.longitude!!,
                    GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun createGeofencePendingIntent(): PendingIntent {
        return getBroadcast(
            context,
            0,
            Intent(context, GeofenceBroadcastReceiver::class.java).apply {
                action = GeofencingConstants.ACTION_GEOFENCE_EVENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

}