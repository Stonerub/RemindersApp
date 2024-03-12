package ru.reminders.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import common.compose.AppTheme
import presentation.app.App

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val driverFactory = DatabaseDriverFactory(this.applicationContext)
            AppTheme {
                App(driverFactory)
            }
        }
    }
}