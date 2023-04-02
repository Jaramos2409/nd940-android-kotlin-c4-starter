package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.base.SaveBaseFragment
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceManager
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : SaveBaseFragment() {

    override val _viewModel: SaveReminderViewModel by inject()
    private val geofenceManager: GeofenceManager by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            if (!checkIfAllPermissionsApproved()) {
                requestForegroundLocationAndNotificationPermissions()
            }

            checkLocationSettings()

            val reminderDataItem = ReminderDataItem(
                title = _viewModel.reminderTitle.value,
                description = _viewModel.reminderDescription.value,
                location = _viewModel.reminderSelectedLocationStr.value,
                latitude = _viewModel.latitude.value,
                longitude = _viewModel.longitude.value
            )

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                geofenceManager.addGeofences(listOf(reminderDataItem))

                _viewModel.validateAndSaveReminder(
                    reminderDataItem
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

}
