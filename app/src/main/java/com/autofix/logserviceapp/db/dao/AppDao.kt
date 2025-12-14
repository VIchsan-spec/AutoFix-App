package com.autofix.logserviceapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
// data entity
import com.autofix.logserviceapp.db.entity.Kendaraan
import com.autofix.logserviceapp.db.entity.Pemilik
import com.autofix.logserviceapp.db.entity.Reminder
import com.autofix.logserviceapp.db.entity.RiwayatService
import java.util.Date

/**
 * Data class khusus untuk menampung hasil query GROUP BY
 * pada halaman Total Cost.
 */
data class CostByCategory(
    val jenis_service: String,
    val total_biaya: Double
)

@Dao
interface AppDao {

    // --- Perintah Pemilik (User) ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerPemilik(pemilik: Pemilik)

    @Query("SELECT * FROM pemilik_table WHERE email = :email LIMIT 1")
    suspend fun getPemilikByEmail(email: String): Pemilik?

    @Query("SELECT * FROM pemilik_table WHERE id_pemilik = :id LIMIT 1")
    suspend fun getPemilikById(id: Int): Pemilik?

    @Update
    suspend fun updatePemilik(pemilik: Pemilik)

    @Query("UPDATE pemilik_table SET password_hash = :newPasswordHash WHERE email = :email")
    suspend fun updatePasswordByEmail(email: String, newPasswordHash: String)

    // --- Perintah Kendaraan ---
    @Insert
    suspend fun addKendaraan(kendaraan: Kendaraan)

    @Query("SELECT * FROM kendaraan_table WHERE id_pemilik_kendaraan = :idPemilik")
    suspend fun getKendaraanByPemilik(idPemilik: Int): List<Kendaraan>

    @Query("SELECT * FROM kendaraan_table WHERE kendaraan_id = :idKendaraan LIMIT 1")
    suspend fun getKendaraanById(idKendaraan: Int): Kendaraan?

    @androidx.room.Delete
    suspend fun deleteKendaraan(kendaraan: Kendaraan)

    // --- Perintah Riwayat Service (Completed) ---
    @Insert
    suspend fun addRiwayatService(riwayatService: RiwayatService)

    @Query("SELECT * FROM riwayat_service_table WHERE id_kendaraan_service = :idKendaraan ORDER BY tanggal_service DESC")
    suspend fun getRiwayatByKendaraan(idKendaraan: Int): List<RiwayatService>

    @Query("SELECT COUNT(*) FROM riwayat_service_table WHERE id_kendaraan_service = :idKendaraan")
    suspend fun getCountCompletedService(idKendaraan: Int): Int

    @androidx.room.Delete
    suspend fun deleteRiwayatService(riwayat: RiwayatService)

    // --- Perintah Reminder (Upcoming/Overdue) ---
    @Insert
    suspend fun addReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE id_kendaraan_reminder = :idKendaraan ORDER BY tanggal_pengingat ASC")
    suspend fun getRemindersByKendaraan(idKendaraan: Int): List<Reminder>

    @Query("SELECT COUNT(*) FROM reminders WHERE id_kendaraan_reminder = :idKendaraan")
    suspend fun getCountReminders(idKendaraan: Int): Int

    // Query untuk "Oversus" (Telat)
    @Query("SELECT COUNT(*) FROM reminders WHERE id_kendaraan_reminder = :idKendaraan AND tanggal_pengingat < :todayTimestamp")
    suspend fun getCountOversusReminders(idKendaraan: Int, todayTimestamp: Long): Int

    @Query("SELECT * FROM reminders WHERE tanggal_pengingat >= :todayStart AND tanggal_pengingat < :tomorrowStart")
    suspend fun getRemindersDueToday(todayStart: Long, tomorrowStart: Long): List<Reminder>

    @androidx.room.Delete
    suspend fun deleteReminder(reminder: Reminder)

    // --- Perintah Total Cost (Filter) ---
    @Query("SELECT SUM(biaya) FROM riwayat_service_table WHERE id_kendaraan_service = :idKendaraan AND tanggal_service BETWEEN :startTime AND :endTime")
    suspend fun getTotalCostByKendaraan(idKendaraan: Int, startTime: Long, endTime: Long): Double?

    @Query("SELECT jenis_service, SUM(biaya) as total_biaya FROM riwayat_service_table WHERE id_kendaraan_service = :idKendaraan AND tanggal_service BETWEEN :startTime AND :endTime GROUP BY jenis_service")
    suspend fun getCostBreakdownByKendaraan(idKendaraan: Int, startTime: Long, endTime: Long): List<CostByCategory>

}