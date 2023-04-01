package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @After
    fun cleanup() {
        stopKoin()
    }

    @Test
    fun saveReminderAndGetById() = runTest {
        val expectedReminder = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644
        )
        database.reminderDao().saveReminder(expectedReminder)

        val actualReminder = database.reminderDao().getReminderById(expectedReminder.id)

        assertThat(actualReminder as ReminderDTO, notNullValue())
        assertThat(actualReminder.id, `is`(expectedReminder.id))
        assertThat(actualReminder.title, `is`(actualReminder.title))
        assertThat(actualReminder.location, `is`(expectedReminder.location))
        assertThat(actualReminder.description, `is`(expectedReminder.description))
        assertThat(actualReminder.latitude, `is`(expectedReminder.latitude))
        assertThat(actualReminder.longitude, `is`(expectedReminder.longitude))
    }

    @Test
    fun saveReminderAndGetListOfReminders() = runTest {
        val expectedListOfReminders = mutableListOf<ReminderDTO>()

        val expectedReminder = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644
        ).apply { expectedListOfReminders.add(this) }
        database.reminderDao().saveReminder(expectedReminder)

        val actualListOfReminders = database.reminderDao().getReminders()

        assertThat(actualListOfReminders, `is`(expectedListOfReminders))
    }

    @Test
    fun saveTwoRemindersAndGetAllReminders() = runTest {
        val expectedListOfReminders = mutableListOf<ReminderDTO>()

        val reminderOne = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969
        ).apply { expectedListOfReminders.add(this) }
        database.reminderDao().saveReminder(reminderOne)

        val reminderTwo = ReminderDTO(
            "NOAH New Years 2023",
            "The Great Muta's Penultimate Battle",
            "Nippon Budokan",
            35.6933,
            139.7497
        ).apply { expectedListOfReminders.add(this) }
        database.reminderDao().saveReminder(reminderTwo)

        val actualListOfReminders = database.reminderDao().getReminders()

        assertThat(actualListOfReminders, `is`(expectedListOfReminders))
    }

    @Test
    fun getRemindersWhenNoneAreSavedInDb() = runTest {
        val actualListOfReminders = database.reminderDao().getReminders()

        assertThat(actualListOfReminders, `is`(empty()))
    }

    @Test
    fun saveReminderThenDeleteAllRemindersAndCheckIfDeletedInDbWithGetReminderId() = runTest {
        val expectedReminder = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644
        )
        database.reminderDao().saveReminder(expectedReminder)

        database.reminderDao().deleteAllReminders()

        val nullReminderDTO = database.reminderDao().getReminderById(expectedReminder.id)

        assertThat(nullReminderDTO, nullValue())
    }

    @Test
    fun saveReminderThenDeleteAllRemindersAndCheckIfDeletedInDbWithGetReminders() = runTest {
        val expectedReminder = ReminderDTO(
            "Example Title of a Reminder #1",
            "Example description for a reminder",
            "Example location",
            -11.19345,
            144.74644
        )
        database.reminderDao().saveReminder(expectedReminder)

        database.reminderDao().deleteAllReminders()

        val emptyListOfReminderDTO = database.reminderDao().getReminders()

        assertThat(emptyListOfReminderDTO, `is`(empty()))
    }

    @Test
    fun saveTwoRemindersThenDeleteAllRemindersAndCheckIfDeletedInDbWithGetReminders() = runTest {
        val reminderOne = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969
        )
        database.reminderDao().saveReminder(reminderOne)

        val reminderTwo = ReminderDTO(
            "NOAH New Years 2023",
            "The Great Muta's Penultimate Battle",
            "Nippon Budokan",
            35.6933,
            139.7497
        )
        database.reminderDao().saveReminder(reminderTwo)

        database.reminderDao().deleteAllReminders()

        val emptyListOfReminderDTO = database.reminderDao().getReminders()

        assertThat(emptyListOfReminderDTO, `is`(empty()))
    }
}