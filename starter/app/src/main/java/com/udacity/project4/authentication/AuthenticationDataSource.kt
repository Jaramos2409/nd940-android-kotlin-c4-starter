package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.udacity.project4.firebase.AuthenticationState

interface AuthenticationDataSource {
    fun getAuthenticationState(): LiveData<AuthenticationState>
}