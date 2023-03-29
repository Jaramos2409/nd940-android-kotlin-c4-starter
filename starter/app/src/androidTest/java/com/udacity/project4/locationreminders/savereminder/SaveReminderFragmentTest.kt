package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.getActivityReference
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SaveReminderFragmentTest : KoinTest {

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
    fun setupFakeAuthenticationRepository() {
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { FakeAuthenticationRepository(shouldAuthenticate = true) }
            }
        )
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
    fun user_tapping_on_reminder_location_should_navigate_to_select_location_fragment() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.selectLocation))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
    }

    @Test
    fun user_tapping_on_save_button_with_no_title_or_location_should_not_navigate_anywhere() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verifyNoInteractions(navController)
    }

    @Test
    fun user_tapping_on_save_button_with_no_title_but_with_description_and_location_should_not_navigate_anywhere() {
        setupSaveReminderViewModel()

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.reminderDescription))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder Description"))

        onView(allOf(withId(R.id.selectedLocation), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verifyNoInteractions(navController)
    }

    @Test
    fun user_tapping_on_save_button_with_only_location_should_not_navigate_anywhere() {
        setupSaveReminderViewModel()

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(allOf(withId(R.id.selectedLocation), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verifyNoInteractions(navController)
    }

    @Test
    fun user_tapping_on_save_button_with_title_but_no_location_should_not_navigate_anywhere() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.reminderTitle))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder"))

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verifyNoInteractions(navController)
    }

    @Test
    fun user_tapping_on_save_button_with_title_and_location_should_navigate_to_reminder_list_fragment() {
        setupSaveReminderViewModel()

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.reminderTitle))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder"))

        onView(allOf(withId(R.id.selectedLocation), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verify(navController).popBackStack(
            R.id.reminderListFragment,
            false
        )
    }

    @Test
    fun user_tapping_on_save_button_with_title_description_and_location_should_navigate_to_reminder_list_fragment() {
        setupSaveReminderViewModel()

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.reminderTitle))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder"))

        onView(withId(R.id.reminderDescription))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder Description"))

        onView(allOf(withId(R.id.selectedLocation), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verify(navController).popBackStack(
            R.id.reminderListFragment,
            false
        )
    }

    // Testing Toasts on versions after Q is not possible at the moment so limiting to that SDK
    // version for now. See: https://github.com/android/android-test/issues/803
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
    @Test
    fun saving_reminder_should_display_reminder_saved_toast_message() {
        setupSaveReminderViewModel()

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.reminderTitle))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder"))

        onView(withId(R.id.reminderDescription))
            .check(matches(isDisplayed()))
            .perform(replaceText("Example Reminder Description"))

        onView(allOf(withId(R.id.selectedLocation), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(not(scenario.getActivityReference().window.decorView)))
            .check(matches(isDisplayed()))
    }

    private fun setupSaveReminderViewModel() {
        loadKoinModules(
            module {
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
    }
}