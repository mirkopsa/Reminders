package com.example.reminders

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReminderDao {

    @Insert
    fun insertReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE message = :message")
    fun findReminder(message: String): List<Reminder>

    @Query("DELETE FROM reminders WHERE message = :message")
    fun deleteReminder(message: String)

    @Query("UPDATE reminders SET message = :message, icon = :icon WHERE rid = :rid")
    fun editReminder(rid: Int, message: String, icon: String)

    @Query("SELECT * FROM reminders")
    fun getAllReminders(): LiveData<List<Reminder>>

}