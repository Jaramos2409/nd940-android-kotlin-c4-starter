package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saverReminderViewModel: SaveReminderViewModel

    private lateinit var fakeReminderDataSource: FakeReminderDataSource

    @Before
    fun setup() = runBlocking {
        fakeReminderDataSource = FakeReminderDataSource()

        saverReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminderDataSource
        )
    }

    @After
    fun tearDownAndCleanup() = runBlocking {
        stopKoin()
        fakeReminderDataSource.deleteAllReminders()
    }

    @Test
    fun `reminderTitle and reminderDescription should be null initially`() {
        assertThat(saverReminderViewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(saverReminderViewModel.reminderDescription.value, `is`(nullValue()))
    }

    @Test
    fun `onClear() should set all MutableLiveData objects to null`() {
        saverReminderViewModel.onClear()
        assertThat(saverReminderViewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(saverReminderViewModel.reminderDescription.value, `is`(nullValue()))
        assertThat(saverReminderViewModel.reminderSelectedLocationStr.value, `is`(nullValue()))
        assertThat(saverReminderViewModel.latitude.value, `is`(nullValue()))
        assertThat(saverReminderViewModel.longitude.value, `is`(nullValue()))
    }

    @Test
    fun `validateEnteredData() should return true when all data is entered correctly`() {
        val reminderData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            12.345,
            67.890
        )
        val result = saverReminderViewModel.validateEnteredData(reminderData)
        assertThat(result, `is`(true))
    }

    @Test
    fun `validateEnteredData() should return false when title is empty`() {
        val reminderData = ReminderDataItem(
            "",
            "Description",
            "Location",
            12.345,
            67.890
        )
        val result = saverReminderViewModel.validateEnteredData(reminderData)
        assertThat(result, `is`(false))
    }

    @Test
    fun `validateEnteredData() should return false when location is empty`() {
        val reminderData = ReminderDataItem(
            "Example Title",
            "Description",
            "",
            12.345,
            67.890
        )
        val result = saverReminderViewModel.validateEnteredData(reminderData)
        assertThat(result, `is`(false))
    }

    @Test
    fun `validateAndSaveReminder() should save the reminder data to the DataSource`() = runTest {
        val reminderData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            12.345,
            67.890
        )

        val reminderDTO = ReminderDTO(
            reminderData.title,
            reminderData.description,
            reminderData.location,
            reminderData.latitude,
            reminderData.longitude,
            reminderData.id
        )

        saverReminderViewModel.validateAndSaveReminder(reminderData)
        advanceUntilIdle()

        val getSavedReminder = fakeReminderDataSource.getReminder(reminderDTO.id)

        assertThat(getSavedReminder, instanceOf(Result.Success::class.java))
        assertThat((getSavedReminder as Result.Success).data, `is`(reminderDTO))
    }

    @Test
    fun `validateAndSaveReminder() should not save the reminder data to the DataSource when ReminderDataItem is not valid`() =
        runTest {
            val reminderData = ReminderDataItem(
                "",
                "Description",
                "Location",
                12.345,
                67.890
            )

            val reminderDTO = ReminderDTO(
                reminderData.title,
                reminderData.description,
                reminderData.location,
                reminderData.latitude,
                reminderData.longitude,
                reminderData.id
            )

            saverReminderViewModel.validateAndSaveReminder(reminderData)
            advanceUntilIdle()

            val getSavedReminder = fakeReminderDataSource.getReminder(reminderDTO.id)

            assertThat(getSavedReminder, instanceOf(Result.Error::class.java))
            assertThat(
                (getSavedReminder as Result.Error).message,
                `is`("Reminder ${reminderDTO.id} not found.")
            )
        }

    @Test
    fun `setReminderLocationData() should set the selected location data`() {
        val selectedMarker = mock(Marker::class.java)
        val selectedPOI = mock(PointOfInterest::class.java)
        val selectedLocationName = "Location"
        val selectedLatitude = 12.345
        val selectedLongitude = 67.890

        saverReminderViewModel.setReminderLocationData(
            selectedMarker,
            selectedPOI,
            selectedLocationName,
            selectedLatitude,
            selectedLongitude
        )

        assertThat(saverReminderViewModel.getSelectedMarker(), `is`(selectedMarker))
        assertThat(saverReminderViewModel.getSelectedPoi(), `is`(selectedPOI))
        assertThat(
            saverReminderViewModel.reminderSelectedLocationStr.value,
            `is`(selectedLocationName)
        )
        assertThat(saverReminderViewModel.latitude.value, `is`(selectedLatitude))
        assertThat(saverReminderViewModel.longitude.value, `is`(selectedLongitude))
    }

    @Test
    fun `validateAndSaveReminder() should update showLoading value as expected`() = runTest {
        val reminderData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            12.345,
            67.890
        )

        saverReminderViewModel.validateAndSaveReminder(reminderData)
        assertThat(saverReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        advanceUntilIdle()

        assertThat(saverReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `validateAndSaveReminder() should update showToast value as expected`() = runTest {
        val reminderData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            12.345,
            67.890
        )

        saverReminderViewModel.validateAndSaveReminder(reminderData)
        advanceUntilIdle()

        assertThat(
            saverReminderViewModel.showToast.getOrAwaitValue(),
            `is`(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.reminder_saved))
        )
    }

    @Test
    fun `validateAndSaveReminder() should update navigationCommand value as expected`() = runTest {
        val reminderData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            12.345,
            67.890
        )

        saverReminderViewModel.validateAndSaveReminder(reminderData)
        advanceUntilIdle()

        assertThat(
            saverReminderViewModel.navigationCommand.getOrAwaitValue(),
            `is`(NavigationCommand.BackTo(R.id.reminderListFragment))
        )
    }

}