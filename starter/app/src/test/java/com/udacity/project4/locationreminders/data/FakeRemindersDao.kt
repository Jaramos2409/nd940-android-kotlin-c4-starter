package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao

class FakeRemindersDao(
    private var reminders: HashMap<String, ReminderDTO> = hashMapOf(),
    private val shouldFail: Boolean = false
) :
    RemindersDao {

    override suspend fun getReminders(): List<ReminderDTO> {
        checkIfShouldFail()
        return reminders.values.toList()
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        checkIfShouldFail()
        return if (reminders.containsKey(reminderId))
            reminders[reminderId]
        else null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    private fun checkIfShouldFail() {
        if (shouldFail)
            throw java.lang.Exception("there was an issue.")
    }
}