package com.udacity.project4.locationreminders

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.firebase.AuthenticationState
import com.udacity.project4.locationreminders.geofence.GeofenceManager
import com.udacity.project4.utils.PermissionUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity(R.layout.activity_reminders) {

    private val authenticationViewModel by viewModel<AuthenticationViewModel>()
    private val geofenceManager: GeofenceManager by inject()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Timber.i("Permissions were granted.")
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                Timber.i("Permissions were not granted.")
                Toast.makeText(
                    this,
                    R.string.feature_may_not_work_properly,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val requestDeviceLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    override fun onResume() {
        super.onResume()
        if (areLocationAndOrNotificationPermissionsGranted()) {
            loadExistingGeofences()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("Inside of onCreate for RemindersActivity")

        authenticationViewModel.authenticationState.observe(this)
        { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> Timber.i("Authenticated")
                AuthenticationState.UNAUTHENTICATED -> startActivity(
                    Intent(
                        this,
                        AuthenticationActivity::class.java
                    )
                )
                else -> Timber.e("New $authenticationState state that doesn't require any UI change")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_LOW_POWER, 3600000L)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
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
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.location_required_error))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        checkDeviceLocationSettingsAndStartGeofence()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.i("Location settings are enabled.")
                loadExistingGeofences()
            }
        }
    }

    private fun loadExistingGeofences() {
        geofenceManager.addGeofencesFromDatabase()
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (checkIfAllPermissionsApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundLocationAndNotificationPermissions()
        }
    }

    private fun checkIfAllPermissionsApproved(): Boolean {
        val foregroundLocationAndOrNotificationApproved =
            areLocationAndOrNotificationPermissionsGranted()
        val backgroundPermissionApproved =
            isBackgroundLocationPermissionsGranted()
        return foregroundLocationAndOrNotificationApproved && backgroundPermissionApproved
    }

    private fun isBackgroundLocationPermissionsGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionUtils.arePermissionsGranted(
                this,
                mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        } else {
            true
        }

    private fun requestForegroundLocationAndNotificationPermissions() {
        if (!areLocationAndOrNotificationPermissionsGranted()) {
            if (PermissionUtils.checkIfShouldShowRequestRationale(
                    this,
                    listOfForegroundAndNotificationPermissions()
                )
            ) {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.permissions_required))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        PermissionUtils.requestPermission(
                            requestPermissionLauncher,
                            listOfForegroundAndNotificationPermissions().toTypedArray()
                        )
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                PermissionUtils.requestPermission(
                    requestPermissionLauncher,
                    listOfForegroundAndNotificationPermissions().toTypedArray()
                )
            }
        }
        checkBackgroundLocation()
    }

    private fun checkBackgroundLocation() {
        if (!isBackgroundLocationPermissionsGranted()) {
            if (PermissionUtils.checkIfShouldShowRequestRationale(
                    this,
                    (mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AlertDialog.Builder(this)
                        .setMessage("This app needs background permissions to continue.")
                        .setPositiveButton("Grant Permissions") { _, _ ->
                            PermissionUtils.requestPermission(
                                requestPermissionLauncher,
                                mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION).toTypedArray()
                            )
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    PermissionUtils.requestPermission(
                        requestPermissionLauncher,
                        mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION).toTypedArray()
                    )
                }
            }
        }
    }

    private fun areLocationAndOrNotificationPermissionsGranted() =
        PermissionUtils.arePermissionsGranted(this, listOfForegroundAndNotificationPermissions())

    private fun listOfForegroundAndNotificationPermissions(): MutableList<String> {
        val listOfPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            listOfPermissions.add(Manifest.permission.POST_NOTIFICATIONS)

        return listOfPermissions
    }

}
