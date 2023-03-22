package com.example.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                // Error
                println("geofencingEvent error")
            } else {
                geofencingEvent.triggeringGeofences?.forEach {
                    val geofence = it.requestId
                    // display notification
                    print("jes notification")
                    geofenceReminderNotification(context, geofence)
                }
            }
        }
    }
}