package com.udacity.project4

import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.getActivityReference
import com.udacity.project4.util.monitorActivity
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module


@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersActivityPermissionsErrorTests {

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
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
            }
        )

        // Launch the activity that requests the permission
        ActivityScenario.launch(RemindersActivity::class.java).use {
            dataBindingIdlingResource.monitorActivity(it)

            // Tapping Deny on Permissions Handler
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.findObject(By.pkg("com.android.permissioncontroller")), 5_000)
            device.findObject(By.res("com.android.permissioncontroller:id/permission_deny_button"))
                .click()

            // Verify Toast message
            onView(withText(R.string.feature_may_not_work_properly))
                .inRoot(withDecorView(not(it.getActivityReference().window.decorView)))
                .check(matches(isDisplayed()))
        }
    }

}
