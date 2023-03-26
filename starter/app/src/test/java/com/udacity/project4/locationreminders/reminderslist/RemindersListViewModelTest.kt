package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@SmallTest
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setup() = runBlocking {
        fakeDataSource = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun tearDownAndCleanup() = runBlocking {
        stopKoin()
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun `loadReminders() when there are reminders expected in the data source`() = runTest {
        val title = "Example Title of a Reminder #1"
        val description = "Example description for a reminder"
        val location = "Example location"
        val latitude = -11.19345
        val longitude = 144.74644
        val id = UUID.randomUUID().toString()

        // Setup FakeDataSource
        fakeDataSource.saveReminder(
            ReminderDTO(
                title,
                description,
                location,
                latitude,
                longitude,
                id
            )
        )

        // Trigger loading of new reminders:
        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        // Assert the remindersList has expected values
        val actualRemindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(
            actualRemindersList, hasItem(
                ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude,
                    id
                )
            )
        )
    }


    @Test
    fun `loadReminders() when there are no reminders expected in the data source and should produce error in snackbar`() =
        runTest {
            // Trigger loading of new reminders:
            remindersListViewModel.loadReminders()
            advanceUntilIdle()

            // Assert the remindersList has expected values
            val actualShowSnackBarValue = remindersListViewModel.showSnackBar.getOrAwaitValue()
            assertThat(actualShowSnackBarValue, `is`("Reminders not found"))
        }

    @Test
    fun `loadReminders() should update showLoading value as expected`() = runTest {
        val title = "Example Title of a Reminder #1"
        val description = "Example description for a reminder"
        val location = "Example location"
        val latitude = -11.19345
        val longitude = 144.74644
        val id = UUID.randomUUID().toString()

        // Setup FakeDataSource
        fakeDataSource.saveReminder(
            ReminderDTO(
                title,
                description,
                location,
                latitude,
                longitude,
                id
            )
        )

        // Trigger loading of new reminders:
        remindersListViewModel.loadReminders()

        // Assert that showLoading is set to true prior to loadReminders going into viewModelScope
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Advance so all the functionality in viewModelScope executes
        advanceUntilIdle()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `invalidateShowNoData() sets showNoData to true when remindersList is empty`() =
        runTest {
            // Trigger loading of new reminders:
            remindersListViewModel.loadReminders()
            advanceUntilIdle()

            // Assert the remindersList has expected values
            val actualShowNoDataValue = remindersListViewModel.showNoData.getOrAwaitValue()
            assertThat(actualShowNoDataValue, `is`(true))
        }

    @Test
    fun `invalidateShowNoData() sets showNoData to false when remindersList is not empty`() =
        runTest {
            val title = "Example Title of a Reminder #1"
            val description = "Example description for a reminder"
            val location = "Example location"
            val latitude = -11.19345
            val longitude = 144.74644
            val id = UUID.randomUUID().toString()

            // Setup FakeDataSource
            fakeDataSource.saveReminder(
                ReminderDTO(
                    title,
                    description,
                    location,
                    latitude,
                    longitude,
                    id
                )
            )

            // Trigger loading of new reminders:
            remindersListViewModel.loadReminders()
            advanceUntilIdle()

            // Assert the remindersList has expected values
            val actualShowNoDataValue = remindersListViewModel.showNoData.getOrAwaitValue()
            assertThat(actualShowNoDataValue, `is`(false))
        }

}