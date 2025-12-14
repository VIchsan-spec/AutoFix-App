package com.autofix.logserviceapp.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    // ID dan Nama Channel
    const val CHANNEL_ID = "AUTOFIX_REMINDER_CHANNEL"
    private const val CHANNEL_NAME = "Pengingat Servis"
    private const val CHANNEL_DESC = "Notifikasi untuk pengingat servis kendaraan"

    /**
     * Dijalankan sekali saat aplikasi pertama kali dibuka.
     * Mendaftarkan channel notifikasi kita ke sistem Android.
     */
    fun createNotificationChannel(application: Application) {
        // Cek jika versi Android O (Oreo) atau lebih tinggi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Buat channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                // atur getar, lampu, dll. di sini
            }

            // Daftarkan channel ke sistem
            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}