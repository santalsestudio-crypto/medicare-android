package com.medicare.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONObject
import java.util.Calendar

object AlarmScheduler {
    fun schedule(c: Context, m: JSONObject) {
        val id = m.optString("id")
        val time = m.optString("time", "08:00").split(":")
        if (id.isBlank() || time.size != 2) return
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time[0].toIntOrNull() ?: 8)
            set(Calendar.MINUTE, time[1].toIntOrNull() ?: 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val freq = m.optString("freq", "Diario")
        var attempts = 0
        while (cal.timeInMillis <= now || !isEligible(cal, freq, m.optString("created"))) {
            cal.add(Calendar.DAY_OF_YEAR, 1); attempts++
            if (attempts > 8) return
        }
        set(c, m, cal.timeInMillis)
    }

    fun scheduleNext(c: Context, m: JSONObject, after: Long) {
        val freq = m.optString("freq", "Diario")
        if (freq == "Solo recordatorio único") return
        val time = m.optString("time", "08:00").split(":")
        val cal = Calendar.getInstance().apply {
            timeInMillis = after + 60_000
            set(Calendar.HOUR_OF_DAY, time[0].toIntOrNull() ?: 8)
            set(Calendar.MINUTE, time[1].toIntOrNull() ?: 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        var attempts = 0
        while (cal.timeInMillis <= after || !isEligible(cal, freq, m.optString("created"))) {
            cal.add(Calendar.DAY_OF_YEAR, 1); attempts++
            if (attempts > 8) return
        }
        set(c, m, cal.timeInMillis)
    }

    private fun isEligible(cal: Calendar, freq: String, created: String): Boolean {
        return when (freq) {
            "Lunes a viernes" -> cal.get(Calendar.DAY_OF_WEEK) in Calendar.MONDAY..Calendar.FRIDAY
            "Solo recordatorio único" -> java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time) == created
            else -> true
        }
    }

    private fun set(c: Context, m: JSONObject, at: Long) {
        val intent = Intent(c, AlarmReceiver::class.java).apply {
            putExtra("id", m.optString("id")); putExtra("name", m.optString("name")); putExtra("freq", m.optString("freq")); putExtra("time", m.optString("time")); putExtra("created", m.optString("created")); putExtra("alarmAt", at)
        }
        val pi = PendingIntent.getBroadcast(c, m.optString("id").hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    fun cancel(c: Context, id: String) {
        val i = Intent(c, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(c, id.hashCode(), i, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: return
        (c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
        pi.cancel()
    }
}
