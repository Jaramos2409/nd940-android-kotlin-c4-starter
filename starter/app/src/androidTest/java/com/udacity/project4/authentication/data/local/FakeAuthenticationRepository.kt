package com.udacity.project4.authentication.data.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.firebase.AuthenticationState

class FakeAuthenticationRepository(shouldAuthenticate: Boolean = false) :
    AuthenticationDataSource {

    override var authenticationState: LiveData<AuthenticationState> = MutableLiveData()
    private val _shouldAuthenticate = MutableLiveData<Boolean>()

    init {
        checkAuthenticationState()
        _shouldAuthenticate.postValue(shouldAuthenticate)
    }

    override fun checkAuthenticationState() {
        authenticationState = _shouldAuthenticate.map {
            if (it) {
                AuthenticationState.AUTHENTICATED
            } else {
                AuthenticationState.UNAUTHENTICATED
            }
        }
    }

    override fun signOut() {
        updateWhetherShouldAuthenticate(false)
        checkAuthenticationState()
    }

    fun updateWhetherShouldAuthenticate(shouldAuthenticate: Boolean) {
        _shouldAuthenticate.value = shouldAuthenticate
    }
}