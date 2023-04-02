package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.location.CheckLocationManagerInterface
import com.udacity.project4.location.FakeCheckLocationManager
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
@MediumTest
class SaveReminderFragmentLocationErrorTest : KoinTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val listOfPermissions = mutableListOf<String>().apply {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @Rule
    @JvmField
    var grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(*listOfPermissions.toTypedArray())

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun location_services_alert_dialog_should_appear_if_check_location_manager_sense_gps_is_off() {
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { FakeAuthenticationRepository(shouldAuthenticate = true) }
                single {
                    SaveReminderViewModel(
                        get(),
                        get()
                    ).apply {
                        setReminderLocationData(
                            selectedLocationName = "Tokyo Dome",
                            selectedLatitude = 35.719448,
                            selectedLongitude = 139.749969
                        )
                    }
                }
                factory<CheckLocationManagerInterface> { (_: Context, _: ActivityResultLauncher<IntentSenderRequest>) ->
                    FakeCheckLocationManager()
                }
            }
        )

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        // Submit to trigger location settings check
        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(ViewActions.click())

        onView(withText(R.string.location_required_error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}