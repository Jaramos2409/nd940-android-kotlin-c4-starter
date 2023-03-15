package com.udacity.project4

import android.app.AlertDialog
import android.content.Context
import androidx.preference.PreferenceManager

class LocationReminderLocationFeatureDirectionsAlertDialog(private val context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun showIfNecessary() {
        if (!sharedPreferences.getBoolean("feature_directions_shown", false)) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Directions:")
            builder.setMessage(
                """
                To select a location for your reminder tap on any landmark, point of interest or existing marker.
            """.trimIndent()
            )
            builder.setPositiveButton("OK") { _, _ ->
                sharedPreferences.edit().putBoolean("feature_directions_shown", true).apply()
            }
            builder.show()
        }
    }
}
