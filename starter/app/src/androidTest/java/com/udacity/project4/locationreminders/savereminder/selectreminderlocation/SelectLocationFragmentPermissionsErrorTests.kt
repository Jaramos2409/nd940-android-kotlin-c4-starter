package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.getActivityReference
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
@MediumTest
class SelectLocationFragmentPermissionsErrorTests : KoinTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    // Testing Toasts on versions after Q is not possible at the moment so limiting to that SDK
    // version for now. See: https://github.com/android/android-test/issues/803
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
    @Test
    fun denying_foreground_location_permission_should_show_toast_warning_message() {
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
            }
        )

        setupDirectionsDialogDoesNotShow()

        val scenario = launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.findObject(By.pkg("com.android.permissioncontroller")), 5_000)
        device.findObject(By.res("com.android.permissioncontroller:id/permission_deny_button"))
            .click()

        // Verify Toast message
        onView(withText(R.string.feature_may_not_work_properly))
            .inRoot(withDecorView(not(scenario.getActivityReference().window.decorView)))
            .check(matches(isDisplayed()))
    }

    private fun setupDirectionsDialogDoesNotShow() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
            .edit()
            .putBoolean("feature_directions_shown", true).apply()
    }
}