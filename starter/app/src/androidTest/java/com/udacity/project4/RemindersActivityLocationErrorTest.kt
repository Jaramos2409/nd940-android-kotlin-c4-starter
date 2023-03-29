package com.udacity.project4

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.location.CheckLocationManagerInterface
import com.udacity.project4.location.FakeCheckLocationManager
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersActivityLocationErrorTest : KoinTest {

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
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @After
    fun cleanup() {
        runBlocking {
            inject<ReminderDataSource>().value.deleteAllReminders()
        }
    }

    @Test
    fun location_services_alert_dialog_should_appear_if_check_location_manager_sense_gps_is_off() {
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { FakeAuthenticationRepository(shouldAuthenticate = true) }
                factory<CheckLocationManagerInterface> { (context: Context, _: ActivityResultLauncher<IntentSenderRequest>) ->
                    FakeCheckLocationManager(context)
                }
            }
        )

        ActivityScenario.launch(RemindersActivity::class.java).use {
            dataBindingIdlingResource.monitorActivity(it)

            Espresso.onView(ViewMatchers.withText(R.string.location_required_error))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}