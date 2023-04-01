package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.data.local.FakeAuthenticationRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SelectLocationFragmentTest {

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
    fun clearAnyExistingSharedPreferences() {
        PreferenceManager
            .getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
            .edit()
            .clear()
            .apply()
    }

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
    fun user_should_see_feature_directions_if_first_time_on_screen() {
        val scenario = launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        onView(ViewMatchers.withText(R.string.map_directions_message))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun user_tapping_on_save_button_should_navigate_to_save_reminder_fragment() {
        setupDirectionsDialogDoesNotShow()

        val scenario = launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        onView(withId(R.id.location_reminder_save_button))
            .check(matches(isDisplayed()))
            .perform(click())

        Mockito.verify(navController).navigate(
            SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment()
        )
    }

    private fun setupDirectionsDialogDoesNotShow() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
            .edit()
            .putBoolean("feature_directions_shown", true).apply()
    }
}