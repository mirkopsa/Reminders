package com.example.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: LiveData<List<Reminder>> = reminderDao.getAllReminders()
    val searchResults = MutableLiveData<List<Reminder>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertReminder(newreminder: Reminder) {
        coroutineScope.launch(Dispatchers.IO) {
            reminderDao.insertReminder(newreminder)
        }
    }

    fun deleteReminder(message: String) {
        coroutineScope.launch(Dispatchers.IO) {
            reminderDao.deleteReminder(message)
        }
    }

    fun editReminder(rid: Int, message: String, icon: String) {
        coroutineScope.launch(Dispatchers.IO) {
            reminderDao.editReminder(rid, message, icon)
        }
    }

    fun findReminder(message: String) {
        coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = asyncFind(message).await()
        }
    }

    private fun asyncFind(message: String): Deferred<List<Reminder>?> =
        coroutineScope.async(Dispatchers.IO) {
            return@async reminderDao.findReminder(message)
        }

}