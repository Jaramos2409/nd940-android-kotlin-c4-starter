package com.udacity.project4

import android.Manifest
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityEndToEndTest : KoinTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var fakeAuthenticationRepository: FakeAuthenticationRepository
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

    @Before
    fun clearAnyExistingSharedPreferences() {
        PreferenceManager
            .getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
            .edit()
            .clear()
            .apply()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @After
    fun cleanup() {
        runBlocking {
            inject<ReminderDataSource>().value.deleteAllReminders()
        }
    }

    @Test
    fun user_should_navigate_to_login_after_tapping_logout_button_on_remindersListFragment() {
        fakeAuthenticationRepository = FakeAuthenticationRepository(shouldAuthenticate = true)
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { fakeAuthenticationRepository }
            }
        )

        ActivityScenario.launch(RemindersActivity::class.java).use {
            dataBindingIdlingResource.monitorActivity(it)

            onView(withId(R.id.logout))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withId(R.id.auth_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun user_should_be_able_to_add_reminder() {
        fakeAuthenticationRepository = FakeAuthenticationRepository(shouldAuthenticate = true)
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { fakeAuthenticationRepository }
                // Setting up SaveReminderViewModel to already have location data
                // so we can avoid having to select a physical marker on the google map
                // ui.
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

        ActivityScenario.launch(RemindersActivity::class.java).use {
            dataBindingIdlingResource.monitorActivity(it)

            onView(withId(R.id.addReminderFAB))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withId(R.id.saveReminder))
                .check(matches(isDisplayed()))

            onView(withId(R.id.selectLocation))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withText(R.string.map_directions_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))

            onView(withText("OK"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withId(R.id.location_reminder_save_button))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withId(R.id.reminderTitle))
                .check(matches(isDisplayed()))
                .perform(replaceText("Wrestle Kingdom"))

            onView(withId(R.id.reminderDescription))
                .check(matches(isDisplayed()))
                .perform(replaceText("Annual January 4th show for New Japan Pro Wrestling"))

            onView(withId(R.id.selectedLocation))
                .check(matches(isDisplayed()))
                .check(matches(withText("Tokyo Dome")))

            onView(withId(R.id.saveReminder))
                .check(matches(isDisplayed()))
                .perform(click())

            onView(withId(R.id.title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Wrestle Kingdom")))

            onView(withId(R.id.description))
                .check(matches(isDisplayed()))
                .check(matches(withText("Annual January 4th show for New Japan Pro Wrestling")))

            onView(withId(R.id.location_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("Tokyo Dome")))
        }
    }

}
