package com.example.appsuivitension.utils

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var reminderEnabled: Boolean
        get() = prefs.getBoolean("reminder_enabled", true)
        set(value) = prefs.edit().putBoolean("reminder_enabled", value).apply()

    var reminderHour: Int
        get() = prefs.getInt("reminder_hour", 20)
        set(value) = prefs.edit().putInt("reminder_hour", value).apply()

    var reminderMinute: Int
        get() = prefs.getInt("reminder_minute", 0)
        set(value) = prefs.edit().putInt("reminder_minute", value).apply()

    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()
}
