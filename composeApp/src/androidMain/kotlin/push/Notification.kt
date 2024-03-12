package push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import common.fucntions.CHANNEL_ID
import ru.reminders.app.MainActivity
import ru.reminders.app.R

const val TITLE_EXTRA = "titleExtra"
const val TEXT_EXTRA = "textExtra"
const val PUSH_KEY = "pushKeyExtra"

class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val pushKey = intent?.getIntExtra(PUSH_KEY, 1) ?: 1
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            pushKey,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        context?.let {
            val notification = NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setColor(ContextCompat.getColor(it, R.color.icon_color))
                .setContentTitle(intent?.getStringExtra(TITLE_EXTRA))
                .setContentText(intent?.getStringExtra(TEXT_EXTRA))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(openAppPendingIntent)
                .build()

            val notificationManager = it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                pushKey,
                notification
            )
        }
    }

}