package com.example.reminders

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel(application: Application) : ViewModel() {

    val allReminders: LiveData<List<Reminder>>
    private val repository: ReminderRepository
    val searchResults: MutableLiveData<List<Reminder>>

    init {
        val reminderDb = AppDatabase.getInstance(application)
        val reminderDao = reminderDb.reminderDao()
        repository = ReminderRepository(reminderDao)

        allReminders = repository.allReminders
        searchResults = repository.searchResults
    }

    fun insertReminder(reminder: Reminder) {
        repository.insertReminder(reminder)
    }

    fun editReminder(rid: Int, message: String, icon: String) {
        repository.editReminder(rid, message, icon)
    }

    fun deleteReminder(message: String) {
        repository.deleteReminder(message)
    }

}