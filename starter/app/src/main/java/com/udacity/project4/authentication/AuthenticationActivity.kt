package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.firebase.AuthenticationState
import com.udacity.project4.locationreminders.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val authenticationViewModel by viewModel<AuthenticationViewModel>()
    private lateinit var binding: ActivityAuthenticationBinding

    private var startActivityIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            run {
                // Add same code that you want to add in onActivityResult method
                val response =
                    IdpResponse.fromResultIntent(result.data)
                if (result.resultCode == Activity.RESULT_OK) {
                    Timber.i("Successfully signed in user " + FirebaseAuth.getInstance().currentUser?.displayName + "!")
                } else {
                    Timber.i("Sign in unsuccessful " + response?.error?.errorCode)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        authenticationViewModel.authenticationState.observe(this)
        { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> startActivity(
                    Intent(
                        this,
                        RemindersActivity::class.java
                    )
                )
                AuthenticationState.UNAUTHENTICATED -> Timber.i("Not Authenticated")
                else -> Timber.e("New $authenticationState state that doesn't require any UI change")
            }
        }

        binding.authButton.setOnClickListener { launchSignInFlow() }
    }

    private fun launchSignInFlow() {
        val customLayout =
            AuthMethodPickerLayout.Builder(R.layout.activity_authentication_choose_provider)
                .setGoogleButtonId(R.id.sign_in_with_google_button)
                .setEmailButtonId(R.id.sign_in_with_email_button)
                .build()

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityIntent.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.CustomFirebaseUi)
                .setIsSmartLockEnabled(false)
                .build()
        )
    }
}
