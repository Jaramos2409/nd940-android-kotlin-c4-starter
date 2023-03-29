package com.udacity.project4

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
class ReminderDescriptionActivityTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun user_should_see_reminder_information_from_intent_on_reminder_description_activity() {
        val reminderDataItem = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            12.345,
            67.890
        )

        val intent = ReminderDescriptionActivity.newIntent(
            ApplicationProvider.getApplicationContext(),
            reminderDataItem
        )

        ActivityScenario.launch<ReminderDescriptionActivity>(intent).use {
            dataBindingIdlingResource.monitorActivity(it)

            onView(allOf(withId(R.id.reminderTitle), withText("Title")))
                .check(matches(isDisplayed()))

            onView(allOf(withId(R.id.reminderDescription), withText("Description")))
                .check(matches(isDisplayed()))

            onView(allOf(withId(R.id.selectLocation), withText("Reminder Location")))
                .check(matches(isDisplayed()))

            onView(allOf(withId(R.id.selectedLocation), withText("Location")))
                .check(matches(isDisplayed()))
        }
    }

}