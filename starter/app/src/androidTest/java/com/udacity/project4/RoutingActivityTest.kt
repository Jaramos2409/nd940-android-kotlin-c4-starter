package com.udacity.project4

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.authentication.AuthenticationDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module


@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
class RoutingActivityTest {

    @Rule
    @JvmField
    var fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    @Rule
    @JvmField
    var backgroundLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    @Test
    fun routingActivityTest_Should_Navigate_To_RemindersListFragment_If_AuthenticationStatus_Is_AUTHENTICATED() {
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { FakeAuthenticationRepository(shouldAuthenticate = true) }
            }
        )

        ActivityScenario.launch(RoutingActivity::class.java).use {
            Espresso.onView(withId(R.id.addReminderFAB))
                .check(ViewAssertions.matches(isDisplayed()))
        }
    }

    @Test
    fun routingActivityTest_Should_Navigate_To_AuthenticationActivity_If_AuthenticationStatus_Is_UNAUTHENTICATED() {
        loadKoinModules(
            module {
                single<AuthenticationDataSource> { FakeAuthenticationRepository(shouldAuthenticate = false) }
            }
        )

        ActivityScenario.launch(RoutingActivity::class.java).use {
            Espresso.onView(withId(R.id.auth_button))
                .check(ViewAssertions.matches(isDisplayed()))
        }
    }

}