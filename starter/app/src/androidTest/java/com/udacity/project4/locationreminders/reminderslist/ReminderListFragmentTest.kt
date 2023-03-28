package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val reminderDataSource by inject<ReminderDataSource>()
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
    fun loadKoinModules() {
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

    @After
    fun cleanup() {
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Test
    fun user_navigates_to_save_reminder_fragment_after_tapping_floating_action_button_on_reminder_list_fragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.addReminderFAB))
            .check(matches(isDisplayed()))
            .perform(ViewActions.click())

        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun user_sees_no_reminders_in_list_and_sees_no_data_icon_and_words() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)


        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun user_sees_one_reminder_in_list() {
        runBlocking {
            reminderDataSource.saveReminder(
                ReminderDTO(
                    "Wrestle Kingdom",
                    "Annual January 4th show for New Japan Pro Wrestling",
                    "Tokyo Dome",
                    35.719448,
                    139.749969
                )
            )
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        onView(allOf(withId(R.id.title), withText("Wrestle Kingdom")))
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.description),
                withText("Annual January 4th show for New Japan Pro Wrestling")
            )
        )
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.location_name), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun user_sees_two_reminders_in_list() {
        runBlocking {
            reminderDataSource.saveReminder(
                ReminderDTO(
                    "Wrestle Kingdom",
                    "Annual January 4th show for New Japan Pro Wrestling",
                    "Tokyo Dome",
                    35.719448,
                    139.749969
                )
            )

            reminderDataSource.saveReminder(
                ReminderDTO(
                    "NOAH New Years 2023",
                    "The Great Muta's Penultimate Battle",
                    "Nippon Budokan",
                    35.6933,
                    139.7497
                )
            )
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        onView(allOf(withId(R.id.title), withText("Wrestle Kingdom")))
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.description),
                withText("Annual January 4th show for New Japan Pro Wrestling")
            )
        )
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.location_name), withText("Tokyo Dome")))
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.title), withText("NOAH New Years 2023")))
            .check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.description),
                withText("The Great Muta's Penultimate Battle")
            )
        )
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.location_name), withText("Nippon Budokan")))
            .check(matches(isDisplayed()))
    }

}