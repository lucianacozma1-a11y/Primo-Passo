package it.primopasso

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object Reminders {
    const val CHANNEL_ID = "primo_passo"
    const val EXTRA = "reminder"

    fun createChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Promemoria", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Lezione del mattino e micro-inizio del pomeriggio"
            }
        )
    }

    private fun pi(context: Context, id: String, code: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, code, Intent(context, ReminderReceiver::class.java).putExtra(EXTRA, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun scheduleAll(context: Context) {
        val p = Prefs(context)
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(pi(context, "morning", 501))
        am.cancel(pi(context, "afternoon", 502))
        if (!p.remindersChosen) return
        if (p.morningOn) schedule(context, "morning", 501, p.morningMinutes)
        if (p.afternoonOn) schedule(context, "afternoon", 502, p.afternoonMinutes)
    }

    private fun schedule(context: Context, id: String, code: Int, minutes: Int) {
        val am = context.getSystemService(AlarmManager::class.java)
        var target = LocalDate.now().atTime(minutes / 60, minutes % 60)
        if (!target.isAfter(LocalDateTime.now().plusSeconds(20))) target = target.plusDays(1)
        val millis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        try {
            if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi(context, id, code))
            else am.setWindow(AlarmManager.RTC_WAKEUP, millis, 10 * 60_000L, pi(context, id, code))
        } catch (_: SecurityException) {
            am.setWindow(AlarmManager.RTC_WAKEUP, millis, 10 * 60_000L, pi(context, id, code))
        }
    }

    fun show(context: Context, id: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val p = Prefs(context)
        val (title, text) = if (id == "morning") {
            val l = p.nextLesson()
            if (l == null || p.lessonDoneToday()) return
            "Buongiorno ☀ Lezione ${l.id}" to "${l.title}: cinque minuti per partire con leggerezza."
        } else {
            "Un micro-inizio?" to "C'è una cosa che rimandi? Troviamo il primo passo: bastano cinque minuti."
        }
        createChannel(context)
        val open = PendingIntent.getActivity(
            context, if (id == "morning") 601 else 602,
            Intent(context, MainActivity::class.java).putExtra(EXTRA, id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF4E8577.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            context.getSystemService(NotificationManager::class.java).notify(if (id == "morning") 701 else 702, n)
        } catch (_: SecurityException) {
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(Reminders.EXTRA) ?: return
        Reminders.show(context, id)
        Reminders.scheduleAll(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminders.scheduleAll(context)
}
