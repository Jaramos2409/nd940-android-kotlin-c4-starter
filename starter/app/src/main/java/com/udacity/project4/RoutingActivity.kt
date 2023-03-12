package com.udacity.project4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.firebase.AuthenticationState
import com.udacity.project4.locationreminders.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "RoutingActivity"

class RoutingActivity : AppCompatActivity(R.layout.activity_rounting) {

    private val authenticationViewModel by viewModel<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authenticationViewModel.authenticationState.observe(this)
        { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> startActivity(
                    Intent(
                        this,
                        RemindersActivity::class.java
                    )
                )
                AuthenticationState.UNAUTHENTICATED -> startActivity(
                    Intent(
                        this,
                        AuthenticationActivity::class.java
                    )
                )
                else -> Log.e(
                    TAG,
                    "New $authenticationState state that doesn't require any UI change"
                )
            }
        }
    }

}
