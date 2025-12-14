package com.autofix.logserviceapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.util.NotificationHelper
import com.autofix.logserviceapp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class DailyReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Cek Preference User (Apakah fitur notifikasi diaktifkan?)
            // Pastikan fungsi getNotificationPreference ada di SessionManager
            val notifEnabled = SessionManager.getNotificationPreference(appContext)
            if (!notifEnabled) {
                return@withContext Result.success()
            }

            // 2. Siapkan Database
            val dao = AppDatabase.getDatabase(appContext).appDao()

            // 3. Hitung Waktu (Timestamp) Hari Ini
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

            // 4. Ambil data reminder yang jatuh tempo hari ini
            val remindersToday = dao.getRemindersDueToday(startOfDay, endOfDay)

            // 5. Looping data dan tampilkan notifikasi
            remindersToday.forEach { reminder ->
                val kendaraan = dao.getKendaraanById(reminder.id_kendaraan_reminder)
                val namaKendaraan = kendaraan?.nama_kendaraan ?: "Kendaraan Anda"

                showNotification(
                    context = appContext,
                    // Gunakan ID dari database agar unik dan tidak tertumpuk
                    notificationId = reminder.id_reminder,
                    title = "Waktunya Servis: $namaKendaraan",
                    message = reminder.pesan_pengingat
                )
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure()
        }
    }

    // Tambahkan anotasi ini agar Android Studio tidak marah (merah) soal Permission
    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, notificationId: Int, title: String, message: String) {

        // Cek Izin Notifikasi (Khusus Android 13+)
        // Jika tidak punya izin, fungsi langsung berhenti (return)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Intent agar saat notif diklik, pindah ke LoginActivity (atau HomeActivity)
        // Pastikan LoginActivity sudah di-import atau satu package
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // Gunakan ID unik request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Buat Builder Notifikasi
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Pastikan ikon ini ada
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Agar teks panjang terbaca
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Agar muncul popup (heads-up)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Tampilkan Notifikasi
        // Karena sudah ada @SuppressLint di atas, 'notify' tidak akan merah lagi
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}