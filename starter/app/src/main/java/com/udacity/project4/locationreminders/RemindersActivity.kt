package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.firebase.AuthenticationState
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "RemindersActivity"

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity(R.layout.activity_reminders) {

    private val authenticationViewModel by viewModel<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "Inside of onCreate for RemindersActivity")

        authenticationViewModel.authenticationState.observe(this)
        { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> Log.i(
                    TAG,
                    "Authenticated"
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
