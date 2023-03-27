package com.udacity.project4.authentication

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.firebase.ui.auth.KickoffActivity
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.RoutingActivity
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
class AuthenticationActivityTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var fakeAuthenticationRepository: FakeAuthenticationRepository

    @Rule
    @JvmField
    var fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    @Rule
    @JvmField
    var backgroundLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    @get:Rule
    val intentsTestRule = IntentsRule()

    @Before
    fun makeSureSignedOutOfFirebaseAuth() {
        FirebaseAuth.getInstance().signOut()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun authenticationActivity_sign_in_and_navigate_to_remindersActivity() {
        setupFakeAuthenticationRepository()

        ActivityScenario.launch(AuthenticationActivity::class.java).use {
            dataBindingIdlingResource.monitorActivity(it)

            // Using Espresso Intents to stub out interactions with FirebaseUI so we can focus on testing our code
            intending(hasComponent(KickoffActivity::class.java.name))
                .respondWithFunction {
                    fakeAuthenticationRepository.updateWhetherShouldAuthenticate(true)
                    fakeAuthenticationRepository.checkAuthenticationState()

                    Instrumentation.ActivityResult(
                        RESULT_OK,
                        Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthenticationActivity::class.java
                        )
                    )
                }

            onView(withId(R.id.auth_button))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withId(R.id.addReminderFAB))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun authenticationActivity_sign_in_fails_and_should_not_navigate_to_remindersActivity() {
        setupFakeAuthenticationRepository()

        ActivityScenario.launch(RoutingActivity::class.java).use {
            dataBindingIdlingResource.monitorActivity(it)

            // Using Espresso Intents to stub out interactions with FirebaseUI so we can focus on testing our code
            intending(hasComponent(KickoffActivity::class.java.name))
                .respondWithFunction {
                    fakeAuthenticationRepository.updateWhetherShouldAuthenticate(false)
                    fakeAuthenticationRepository.checkAuthenticationState()

                    Instrumentation.ActivityResult(
                        RESULT_CANCELED,
                        Intent(
                            ApplicationProvider.getApplicationContext(),
                            AuthenticationActivity::class.java
                        )
                    )
                }

            onView(withId(R.id.auth_button))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withText("Directions"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        }
    }

    private fun setupFakeAuthenticationRepository() {
        fakeAuthenticationRepository = FakeAuthenticationRepository(shouldAuthenticate = false)
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { fakeAuthenticationRepository }
            }
        )
    }
}