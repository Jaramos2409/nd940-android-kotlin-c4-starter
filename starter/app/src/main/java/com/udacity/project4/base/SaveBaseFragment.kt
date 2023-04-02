package com.udacity.project4.base

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.udacity.project4.R
import com.udacity.project4.location.CheckLocationManager
import com.udacity.project4.location.CheckLocationManagerInterface
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.PermissionUtils
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

abstract class SaveBaseFragment : BaseFragment(),
    CheckLocationManager.LocationSettingsListener {

    abstract override val _viewModel: SaveReminderViewModel

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Timber.i("Permissions were granted.")
            } else {
                Timber.i("Permissions were not granted.")
                Toast.makeText(
                    requireContext(),
                    R.string.feature_may_not_work_properly,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val requestDeviceLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            checkLocationManager.checkDeviceLocationSettings(this, false)
        }

    private val checkLocationManager: CheckLocationManagerInterface by inject {
        parametersOf(
            requireContext(),
            requestDeviceLocationLauncher
        )
    }

    override fun onResume() {
        super.onResume()
        checkLocationManager.registerLocationProviderChangedReceiver(this)
    }

    override fun onPause() {
        super.onPause()
        checkLocationManager.unregisterLocationProviderChangedReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewModel.shouldShowLocationAlertDialog.observe(viewLifecycleOwner)
        { shouldShowAlertDialog ->
            run {
                if (shouldShowAlertDialog) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.location_required_error))
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->
                            checkLocationManager.checkDeviceLocationSettings(this)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            _viewModel.resetAlertDialogCheck()
                        }
                        .create()
                        .show()
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    internal fun requestForegroundLocationAndNotificationPermissions() {
        if (!areLocationAndOrNotificationPermissionsGranted()) {
            if (PermissionUtils.checkIfShouldShowRequestRationale(
                    requireActivity(),
                    PermissionUtils.listOfForegroundAndNotificationPermissions()
                )
            ) {
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.permissions_required))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        PermissionUtils.requestPermission(
                            requestPermissionLauncher,
                            PermissionUtils.listOfForegroundAndNotificationPermissions()
                                .toTypedArray()
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
                    PermissionUtils.listOfForegroundAndNotificationPermissions().toTypedArray()
                )
            }
        }
        checkBackgroundLocation()
    }

    protected fun checkIfAllPermissionsApproved(): Boolean {
        val foregroundLocationAndOrNotificationApproved =
            areLocationAndOrNotificationPermissionsGranted()
        val backgroundPermissionApproved =
            isBackgroundLocationPermissionsGranted()
        return foregroundLocationAndOrNotificationApproved && backgroundPermissionApproved
    }

    private fun checkBackgroundLocation() {
        if (!isBackgroundLocationPermissionsGranted()) {
            if (PermissionUtils.checkIfShouldShowRequestRationale(
                    requireActivity(),
                    (mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AlertDialog.Builder(requireContext())
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
        PermissionUtils.arePermissionsGranted(
            requireContext(),
            PermissionUtils.listOfForegroundAndNotificationPermissions()
        )

    private fun isBackgroundLocationPermissionsGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionUtils.arePermissionsGranted(
                requireContext(),
                mutableListOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        } else {
            true
        }

    protected fun checkLocationSettings() {
        checkLocationManager.checkDeviceLocationSettings(this)
    }

    override fun onLocationSettingsEnabled() {}

    override fun onLocationSettingsFailed(exception: Exception) {
        _viewModel.showAlertDialog()
    }
}