package com.autofix.logserviceapp.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date // Menggunakan tipe data Date

@Entity(tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Kendaraan::class, // Terhubung ke tabel Kendaraan
            parentColumns = ["kendaraan_id"],
            childColumns = ["id_kendaraan_reminder"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id_reminder: Int = 0,

    val id_kendaraan_reminder: Int, // Kolom kunci penghubung
    val tanggal_pengingat: Date, // Tipe data Date (akan di-convert oleh Converters.kt)
    val pesan_pengingat: String
)