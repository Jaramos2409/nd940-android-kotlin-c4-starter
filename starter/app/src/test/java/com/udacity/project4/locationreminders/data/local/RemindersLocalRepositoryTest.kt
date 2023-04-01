package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeRemindersDao
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.*
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersLocalRepositoryTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @After
    fun cleanup() {
        stopKoin()
    }

    @Test
    fun `get reminders when expecting one reminder in database`() = runTest {
        val expectedListOfReminders = listOf(
            ReminderDTO(
                "Example Title of a Reminder #1",
                "Example description for a reminder",
                "Example location",
                -11.19345,
                144.74644,
                UUID.randomUUID().toString()
            )
        )

        val remindersLocalRepository = RemindersLocalRepository(
            FakeRemindersDao(HashMap(expectedListOfReminders.associateBy { it.id })),
            Dispatchers.Unconfined
        )

        val actualListOfRemindersResult = remindersLocalRepository.getReminders()
//        advanceUntilIdle()

        assertThat(actualListOfRemindersResult, instanceOf(Result.Success::class.java))
        assertThat(
            (actualListOfRemindersResult as Result.Success).data, `is`(
                expectedListOfReminders
            )
        )
    }

    @Test
    fun `get reminders when expecting no reminders in database`() = runTest {
        val remindersLocalRepository = RemindersLocalRepository(
            FakeRemindersDao(),
            Dispatchers.Unconfined
        )

        val actualListOfReminders = remindersLocalRepository.getReminders()

        assertThat(actualListOfReminders, instanceOf(Result.Success::class.java))
        assertThat((actualListOfReminders as Result.Success).data, `is`(empty()))
    }

    @Test
    fun `get reminders when expecting the call to fail from dao`() = runTest {
        val expectedListOfReminders = listOf(
            ReminderDTO(
                "Example Title of a Reminder #1",
                "Example description for a reminder",
                "Example location",
                -11.19345,
                144.74644,
                UUID.randomUUID().toString()
            )
        )

        val remindersLocalRepository = RemindersLocalRepository(
            FakeRemindersDao(
                HashMap(expectedListOfReminders.associateBy { it.id }),
                shouldFail = true
            ),
            Dispatchers.Unconfined
        )

        val actualListOfReminders = remindersLocalRepository.getReminders()

        assertThat(actualListOfReminders, instanceOf(Result.Error::class.java))
        assertThat(
            (actualListOfReminders as Result.Error).message, `is`(
                "there was an issue."
            )
        )
    }

    @Test
    fun `save reminder should call saveReminder to save the reminder`() = runTest {
        val mockRemindersDao = mock<FakeRemindersDao>()

        val reminderDTO = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644,
            UUID.randomUUID().toString()
        )

        val remindersLocalRepository = RemindersLocalRepository(
            mockRemindersDao,
            Dispatchers.Unconfined
        )

        remindersLocalRepository.saveReminder(reminderDTO)

        verify(mockRemindersDao).saveReminder(reminderDTO)
    }

    @Test
    fun `get reminder returns the expected ReminderDTO`() = runTest {
        val expectedReminderDTO = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644,
            UUID.randomUUID().toString()
        )

        val remindersLocalRepository = RemindersLocalRepository(
            FakeRemindersDao(HashMap(listOf(expectedReminderDTO).associateBy { it.id })),
            Dispatchers.Unconfined
        )

        val actualReminderDTOResult = remindersLocalRepository.getReminder(expectedReminderDTO.id)

        assertThat(actualReminderDTOResult, instanceOf(Result.Success::class.java))
        assertThat((actualReminderDTOResult as Result.Success).data, `is`(expectedReminderDTO))
    }

    @Test
    fun `get reminder returns error when ReminderDTO is not found`() = runTest {
        val remindersLocalRepository = RemindersLocalRepository(
            FakeRemindersDao(),
            Dispatchers.Unconfined
        )

        val actualReminderDTOResult = remindersLocalRepository.getReminder("invalidId")

        assertThat(actualReminderDTOResult, instanceOf(Result.Error::class.java))
        assertThat((actualReminderDTOResult as Result.Error).message, `is`("Reminder not found!"))
    }

    @Test
    fun `get reminder returns error when remindersDao is expected to fail`() = runTest {
        val reminderDTO = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644,
            UUID.randomUUID().toString()
        )

        val remindersLocalRepository = RemindersLocalRepository(
            FakeRemindersDao(
                HashMap(listOf(reminderDTO).associateBy { it.id }),
                shouldFail = true
            ),
            Dispatchers.Unconfined
        )

        val actualReminderDTOResult = remindersLocalRepository.getReminder(reminderDTO.id)

        assertThat(actualReminderDTOResult, instanceOf(Result.Error::class.java))
        assertThat((actualReminderDTOResult as Result.Error).message, `is`("there was an issue."))
    }

    @Test
    fun `deleteAllReminders should call deleteAllReminders from remindersDao to delete all reminders from DB`() =
        runTest {
            val mockRemindersDao = mock<FakeRemindersDao>()

            val remindersLocalRepository = RemindersLocalRepository(
                mockRemindersDao,
                Dispatchers.Unconfined
            )

            remindersLocalRepository.deleteAllReminders()

            verify(mockRemindersDao).deleteAllReminders()
        }
}