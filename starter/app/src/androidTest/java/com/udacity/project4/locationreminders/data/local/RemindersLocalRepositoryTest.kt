package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun cleanUp() = database.close()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun saveReminderAndGetById() = runTest {
        val id = UUID.randomUUID().toString()
        val reminder = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969,
            id
        )
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(id)

        assertThat(result, instanceOf(Result.Success::class.java))
        assertThat((result as Result.Success).data, `is`(reminder))
    }

    @Test
    fun saveReminderAndGetAllReminders() = runTest {
        val reminder = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969
        )
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminders()

        assertThat(result, instanceOf(Result.Success::class.java))
        assertThat((result as Result.Success).data, `is`(listOf(reminder)))
    }

    @Test
    fun saveTwoReminderAndGetAllReminders() = runTest {
        val expectedListOfReminders = mutableListOf<ReminderDTO>()

        val reminderOne = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969
        ).apply { expectedListOfReminders.add(this) }
        remindersLocalRepository.saveReminder(reminderOne)

        val reminderTwo = ReminderDTO(
            "NOAH New Years 2023",
            "The Great Muta's Penultimate Battle",
            "Nippon Budokan",
            35.6933,
            139.7497
        ).apply { expectedListOfReminders.add(this) }
        remindersLocalRepository.saveReminder(reminderTwo)

        val result = remindersLocalRepository.getReminders()

        assertThat(result, instanceOf(Result.Success::class.java))
        assertThat((result as Result.Success).data, `is`(expectedListOfReminders))
    }

    @Test
    fun getRemindersWhenNoneAreSavedInDb() = runTest {
        val result = remindersLocalRepository.getReminders()

        assertThat(result, instanceOf(Result.Success::class.java))
        assertThat((result as Result.Success).data, `is`(empty()))
    }

    @Test
    fun saveReminderThenDeleteAllRemindersAndCheckIfDeletedInDbWithGetReminderId() = runTest {
        val id = UUID.randomUUID().toString()
        val reminder = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969,
            id
        )
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminder(id)

        assertThat(result, instanceOf(Result.Error::class.java))
        assertThat((result as Result.Error).message, `is`("Reminder not found!"))
    }

    @Test
    fun saveReminderThenDeleteAllRemindersAndCheckIfDeletedInDbWithGetReminders() = runTest {
        val reminder = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969
        )
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        assertThat(result, instanceOf(Result.Success::class.java))
        assertThat((result as Result.Success).data, `is`(empty()))
    }

    @Test
    fun saveTwoRemindersDeleteAllReminders() = runTest {
        val actualListOfReminders = mutableListOf<ReminderDTO>()

        val reminderOne = ReminderDTO(
            "Wrestle Kingdom",
            "Annual January 4th show for New Japan Pro Wrestling",
            "Tokyo Dome",
            35.719448,
            139.749969
        ).apply { actualListOfReminders.add(this) }
        remindersLocalRepository.saveReminder(reminderOne)

        val reminderTwo = ReminderDTO(
            "NOAH New Years 2023",
            "The Great Muta's Penultimate Battle",
            "Nippon Budokan",
            35.6933,
            139.7497
        ).apply { actualListOfReminders.add(this) }
        remindersLocalRepository.saveReminder(reminderTwo)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        assertThat(result, instanceOf(Result.Success::class.java))
        assertThat((result as Result.Success).data, `is`(empty()))
    }
}