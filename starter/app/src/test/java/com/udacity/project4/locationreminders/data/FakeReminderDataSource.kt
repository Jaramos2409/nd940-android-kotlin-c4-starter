package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeReminderDataSource(private var reminders: HashMap<String, ReminderDTO>? = hashMapOf()) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (reminders?.isNotEmpty() == true) {
            return Result.Success(reminders!!.values.toList())
        }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.put(reminder.id, reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (reminders?.isNotEmpty() == true) {
            return Result.Success(reminders!![id] as ReminderDTO)
        }
        return Result.Error("Reminder $id not found.")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}