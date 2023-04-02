package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.asDomainModel
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val remindersLocalRepository: ReminderDataSource by inject(ReminderDataSource::class.java)

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        Timber.i("Inside of onReceiver")
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            Timber.i("Inside of ACTION_GEOFENCE_EVENT")

            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent?.hasError() == true) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                Timber.e(errorMessage)
                return@goAsync
            }

            if (geofencingEvent?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Timber.v(context.getString(R.string.geofence_entered))

                if (!geofencingEvent.triggeringGeofences.isNullOrEmpty()) {
                    geofencingEvent.triggeringGeofences?.forEach { triggeredGeofence ->
                        run {
                            when (val getReminderResult =
                                remindersLocalRepository.getReminder(triggeredGeofence.requestId)) {
                                is Result.Error -> {
                                    Timber.e(getReminderResult.message)
                                    return@goAsync
                                }
                                is Result.Success -> {
                                    NotificationUtils.sendReminderNotification(
                                        context,
                                        getReminderResult.data.asDomainModel()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob()).launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}