package com.example.reminders

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val rid: Int,
    @ColumnInfo(name = "message") var message: String = "",
    @ColumnInfo(name = "icon") var icon: String = "",
    @ColumnInfo(name = "location_x") val location_x: String = "",
    @ColumnInfo(name = "location_y") val location_y: String = "",
    @ColumnInfo(name = "reminder_time") val reminder_time: Long = 0,
    @ColumnInfo(name = "creation_time") val creation_time: Long = 0,
    @ColumnInfo(name = "creator_id") val creator_id: Int = 0,
    @ColumnInfo(name = "reminder_seen") val reminder_seen: Boolean = false
)