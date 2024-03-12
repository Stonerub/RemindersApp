package ru.reminders.app

import android.app.Application
import android.app.NotificationManager
import android.content.Context

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        var appContext: Context? = null
            private set
        var notificationManager: NotificationManager? = null
            private set
    }
}