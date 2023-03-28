package com.udacity.project4.locationreminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RemindersActivityViewModel : ViewModel() {

    private val _shouldShowLocationAlertDialog = MutableLiveData<Boolean>()
    val shouldShowLocationAlertDialog: LiveData<Boolean>
        get() = _shouldShowLocationAlertDialog

    init {
        _shouldShowLocationAlertDialog.value = false
    }

    fun showAlertDialog() {
        _shouldShowLocationAlertDialog.value = true
    }

    fun resetAlertDialogCheck() {
        _shouldShowLocationAlertDialog.value = false
    }

}