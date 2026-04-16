package com.example.appsuivitension

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.appsuivitension.ui.screens.MainScreen
import com.example.appsuivitension.ui.theme.AppSuiviTensionTheme
import com.example.appsuivitension.utils.SettingsManager
import com.example.appsuivitension.utils.ThemeMode
import com.example.appsuivitension.worker.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager
    private var currentThemeMode by mutableStateOf(ThemeMode.SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        settingsManager = SettingsManager(this)
        currentThemeMode = settingsManager.themeMode
        
        enableEdgeToEdge()
        setContent {
            AppSuiviTensionTheme(themeMode = currentThemeMode) {
                MainScreen(onThemeChange = { newMode ->
                    currentThemeMode = newMode
                    settingsManager.themeMode = newMode
                }, onReminderChange = {
                    setupDailyReminder()
                })
            }
        }
        
        // On configure le rappel en arrière-plan pour ne pas ralentir le démarrage
        lifecycleScope.launch(Dispatchers.IO) {
            setupDailyReminder()
        }
    }

    private fun setupDailyReminder() {
        if (!settingsManager.reminderEnabled) {
            WorkManager.getInstance(this).cancelUniqueWork("daily_reminder")
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settingsManager.reminderHour)
            set(Calendar.MINUTE, settingsManager.reminderMinute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )
    }
}
