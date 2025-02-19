package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    private val _selectedMarker = MutableLiveData<Marker?>()
    private val _selectedPOI = MutableLiveData<PointOfInterest?>()

    private val _latitude = MutableLiveData<Double?>()
    val latitude: LiveData<Double?>
        get() = _latitude

    private val _longitude = MutableLiveData<Double?>()
    val longitude: LiveData<Double?>
        get() = _longitude

    private val _shouldShowLocationAlertDialog = MutableLiveData<Boolean>()
    val shouldShowLocationAlertDialog: LiveData<Boolean>
        get() = _shouldShowLocationAlertDialog

    init {
        _shouldShowLocationAlertDialog.value = false
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        _selectedMarker.value = null
        _selectedPOI.value = null
        _latitude.value = null
        _longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value =
                NavigationCommand.BackTo(R.id.reminderListFragment)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }

        return true
    }

    fun getSelectedPoi() = _selectedPOI.value

    fun getSelectedMarker() = _selectedMarker.value

    fun setReminderLocationData(
        selectedMarker: Marker? = null,
        selectedPOI: PointOfInterest? = null,
        selectedLocationName: String,
        selectedLatitude: Double,
        selectedLongitude: Double
    ) {
        _selectedMarker.value = selectedMarker
        _selectedPOI.value = selectedPOI
        reminderSelectedLocationStr.value = selectedLocationName
        _latitude.value = selectedLatitude
        _longitude.value = selectedLongitude
    }

    fun showAlertDialog() {
        _shouldShowLocationAlertDialog.value = true
    }

    fun resetAlertDialogCheck() {
        _shouldShowLocationAlertDialog.value = false
    }

}