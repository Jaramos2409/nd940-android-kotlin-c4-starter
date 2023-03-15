package com.udacity.project4.base

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import timber.log.Timber

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Timber.i("Permissions were granted.")
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                Timber.i("Permissions were not granted.")
                Toast.makeText(
                    requireContext(),
                    "Feature may not work properly without all permission granted.",
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
        _viewModel.showErrorMessage.observe(this) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        _viewModel.showToast.observe(this) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        _viewModel.showSnackBar.observe(this) {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        }
        _viewModel.showSnackBarInt.observe(this) {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        }

        _viewModel.navigationCommand.observe(this) { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        }
        checkPermissionsAndStartGeofencing()
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_LOW_POWER, 3600000L)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
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
                AlertDialog.Builder(requireContext())
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
            }
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
//        if (viewModel.geofenceIsActive()) return
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
            arePermissionsGranted(mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        } else {
            true
        }

    private fun requestForegroundLocationAndNotificationPermissions() {
        if (!areLocationAndOrNotificationPermissionsGranted()) {
            if (checkIfShouldShowRequestRationale(listOfForegroundAndNotificationPermissions())) {
                view?.let {
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.permissions_required))
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->
                            requestPermissionLauncher
                                .launch(listOfForegroundAndNotificationPermissions().toTypedArray())
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            } else {
                requestPermissionLauncher
                    .launch(listOfForegroundAndNotificationPermissions().toTypedArray())
            }
        }
        checkBackgroundLocation()
    }

    private fun areLocationAndOrNotificationPermissionsGranted() =
        arePermissionsGranted(listOfForegroundAndNotificationPermissions())

    private fun checkBackgroundLocation() {
        if (!isBackgroundLocationPermissionsGranted()) {
            if (checkIfShouldShowRequestRationale((mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("This app needs background permissions to continue.")
                        .setPositiveButton("Grant Permissions") { _, _ ->
                            requestPermissionLauncher
                                .launch(mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION).toTypedArray())
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissionLauncher
                        .launch(mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION).toTypedArray())
                }
            }
        }
    }

    private fun checkIfShouldShowRequestRationale(listOfPermissions: List<String>): Boolean =
        listOfPermissions.indexOfFirst { permission ->
            shouldShowRequestPermissionRationale(permission)
        } != -1

    private fun arePermissionsGranted(listOfPermissions: List<String>): Boolean =
        listOfPermissions.indexOfFirst { permission ->
            ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        } != -1

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