package common.fucntions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import presentation.app.model.PushModel
import push.Notification
import push.PUSH_KEY
import push.TEXT_EXTRA
import push.TITLE_EXTRA
import ru.reminders.app.MyApplication
import toIntOrNull

const val CHANNEL_ID = "ru.reminders.app"

actual fun setupNotificationChannel() {
    val context = MyApplication.appContext
    context?.let {
        val chanel = NotificationChannel(
            CHANNEL_ID,
            "ru.reminders.app",
            NotificationManager.IMPORTANCE_HIGH
        )
        chanel.description = "used for reminders notifications"
        val notificationManager = MyApplication.notificationManager
        notificationManager?.createNotificationChannel(chanel)
    }
}

@SuppressLint("ScheduleExactAlarm")
actual fun sendPush(pushModel: PushModel) {
    MyApplication.appContext?.let { context ->
        if (System.currentTimeMillis() <= (pushModel.time.firstOrNull()?.date ?: 0)) {
            val pendingIntents = context.buildIntents(pushModel)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            pushModel.time.forEach {
                val index = pushModel.time.indexOf(it)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it.date,
                    pendingIntents[index]
                )
            }
        }
    }
}

actual fun deletePush(pushModel: PushModel) {
    MyApplication.appContext?.let { context ->
        val pendingIntents = context.buildIntents(pushModel)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntents.forEach {
            alarmManager.cancel(it)
        }
    }
}

private fun Context.buildIntents(pushModel: PushModel): List<PendingIntent> {
    return pushModel.time.map {
        val pushKey = pushModel.id * (pushModel.time.indexOf(it) + 1) * 1000
        val intent = Intent(this, Notification::class.java)
        with(pushModel) {
            intent.putExtra(TITLE_EXTRA, title)
            intent.putExtra(TEXT_EXTRA, text)
            intent.putExtra(PUSH_KEY, pushKey)
        }
        PendingIntent.getBroadcast(
            this,
            pushKey.toIntOrNull() ?: 0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    }
}