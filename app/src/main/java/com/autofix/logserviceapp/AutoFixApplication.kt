package com.autofix.logserviceapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.autofix.logserviceapp.util.NotificationHelper
import com.autofix.logserviceapp.util.SessionManager
import java.util.concurrent.TimeUnit

class AutoFixApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // =================================================
        // TERAPKAN PREFERENSI SAAT APLIKASI DIBUKA
        // =================================================

        // 1. Terapkan Tema (Gelap/Terang)
        val currentTheme = SessionManager.getTheme(this)
        AppCompatDelegate.setDefaultNightMode(currentTheme)

        // 2. Terapkan Bahasa (ID/EN)
        val currentLang = SessionManager.getLanguage(this)
        val locales = LocaleListCompat.forLanguageTags(currentLang)
        AppCompatDelegate.setApplicationLocales(locales)
        // =================================================

        // 3. Buat Saluran Notifikasi
        NotificationHelper.createNotificationChannel(this)

        // 4. Jadwalkan kerja di Latar Belakang
        setupDailyReminderWork()
    }

    private fun setupDailyReminderWork() {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DAILY_REMINDER_CHECK",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWorkRequest
        )
    }
}