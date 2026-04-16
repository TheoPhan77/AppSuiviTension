package com.example.appsuivitension.utils

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val authManager = AuthManager(context)

    private fun getPrefKey(key: String): String {
        val userId = authManager.getActiveUserId() ?: "default"
        return "${userId}_$key"
    }

    var reminderEnabled: Boolean
        get() = prefs.getBoolean(getPrefKey("reminder_enabled"), true)
        set(value) = prefs.edit().putBoolean(getPrefKey("reminder_enabled"), value).apply()

    var reminderHour: Int
        get() = prefs.getInt(getPrefKey("reminder_hour"), 20)
        set(value) = prefs.edit().putInt(getPrefKey("reminder_hour"), value).apply()

    var reminderMinute: Int
        get() = prefs.getInt(getPrefKey("reminder_minute"), 0)
        set(value) = prefs.edit().putInt(getPrefKey("reminder_minute"), value).apply()

    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(prefs.getString(getPrefKey("theme_mode"), ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        set(value) = prefs.edit().putString(getPrefKey("theme_mode"), value.name).apply()
}
