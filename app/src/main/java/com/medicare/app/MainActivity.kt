package com.medicare.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var web: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(MedicareBridge(this), "MedicareNative")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 200)
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } catch (_: Exception) { }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) {
            val all = AlarmStore.medications(this)
            for (i in 0 until all.length()) AlarmScheduler.schedule(this, all.getJSONObject(i))
        }
    }

    override fun onBackPressed() { if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed() }
}

class MedicareBridge(private val activity: Activity) {
    @JavascriptInterface fun scheduleMedication(json: String) {
        try { val m = JSONObject(json); AlarmStore.saveMedication(activity, m); AlarmScheduler.cancel(activity, m.optString("id")); AlarmScheduler.schedule(activity, m) } catch (_: Exception) { }
    }
    @JavascriptInterface fun cancelMedication(id: String) { AlarmScheduler.cancel(activity, id); AlarmStore.removeMedication(activity, id) }
    @JavascriptInterface fun recordTaken(id: String, alarmAt: Long, takenAt: Long) {
        val status = if (takenAt <= alarmAt + 10 * 60 * 1000L) "on_time" else "late"
        AlarmStore.putEvent(activity, id, alarmAt, takenAt, status)
    }
    @JavascriptInterface fun getEvents(): String = AlarmStore.events(activity).toString()
    @JavascriptInterface fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= 31) try { activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${activity.packageName}"))) } catch (_: Exception) { }
    }
    @JavascriptInterface fun openNotificationSettings() {
        try {
            val i = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                    else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}"))
            activity.startActivity(i)
        } catch (_: Exception) { }
    }
}
