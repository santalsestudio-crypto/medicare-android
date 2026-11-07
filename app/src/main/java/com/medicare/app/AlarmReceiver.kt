package com.medicare.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val name = intent.getStringExtra("name") ?: "tu medicamento"
        val at = intent.getLongExtra("alarmAt", System.currentTimeMillis())
        val channelId = "medicare_medication_reminders"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, "Recordatorios de medicamentos", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos de horarios de medicamentos"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            }
            nm.createNotificationChannel(channel)
        }
        val tap = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val actionIntent = Intent(context, TakenReceiver::class.java).apply { putExtra("id", id); putExtra("alarmAt", at) }
        val action = PendingIntent.getBroadcast(context, (id + at).hashCode(), actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Hora de tu medicamento")
            .setContentText("Es momento de tomar: $name")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Es momento de tomar $name. Tienes 10 minutos para confirmar y ganar tus puntos."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(tap)
            .addAction(android.R.drawable.checkbox_on_background, "Ya la tomé", action)
            .build()
        nm.notify(id.hashCode(), n)
        val m = findMedication(AlarmStore.medications(context), id)
        if (m != null) AlarmScheduler.scheduleNext(context, m, at)
    }

    private fun findMedication(a: org.json.JSONArray, id: String): org.json.JSONObject? {
        for (i in 0 until a.length()) { val m = a.getJSONObject(i); if (m.optString("id") == id) return m }
        return null
    }
}

class TakenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val alarmAt = intent.getLongExtra("alarmAt", System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val status = if (now <= alarmAt + 10 * 60 * 1000L) "on_time" else "late"
        AlarmStore.putEvent(context, id, alarmAt, now, status)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id.hashCode())
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val all = AlarmStore.medications(context)
        for (i in 0 until all.length()) AlarmScheduler.schedule(context, all.getJSONObject(i))
    }
}
