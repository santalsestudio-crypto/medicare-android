package com.medicare.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AlarmStore {
    private const val PREFS = "medicare_native"
    private const val MEDS = "medications"
    private const val EVENTS = "events"
    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveMedication(c: Context, item: JSONObject) {
        val all = JSONArray(prefs(c).getString(MEDS, "[]"))
        val out = JSONArray()
        for (i in 0 until all.length()) if (all.getJSONObject(i).optString("id") != item.optString("id")) out.put(all.getJSONObject(i))
        out.put(item)
        prefs(c).edit().putString(MEDS, out.toString()).apply()
    }

    fun removeMedication(c: Context, id: String) {
        val all = JSONArray(prefs(c).getString(MEDS, "[]")); val out = JSONArray()
        for (i in 0 until all.length()) if (all.getJSONObject(i).optString("id") != id) out.put(all.getJSONObject(i))
        prefs(c).edit().putString(MEDS, out.toString()).apply()
    }

    fun medications(c: Context) = JSONArray(prefs(c).getString(MEDS, "[]"))

    fun putEvent(c: Context, id: String, alarmAt: Long, takenAt: Long, status: String) {
        val all = JSONObject(prefs(c).getString(EVENTS, "{}"))
        val key = "${id}_${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(alarmAt))}"
        all.put(key, JSONObject().put("id", id).put("alarmAt", alarmAt).put("takenAt", takenAt).put("status", status))
        prefs(c).edit().putString(EVENTS, all.toString()).apply()
    }

    fun events(c: Context) = JSONObject(prefs(c).getString(EVENTS, "{}"))
}
