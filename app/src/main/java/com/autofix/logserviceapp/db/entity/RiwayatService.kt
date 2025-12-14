package com.autofix.logserviceapp.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date // Menggunakan tipe data Date

@Entity(tableName = "riwayat_service_table",
    foreignKeys = [
        ForeignKey(
            entity = Kendaraan::class, // Terhubung ke tabel Kendaraan
            parentColumns = ["kendaraan_id"],
            childColumns = ["id_kendaraan_service"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RiwayatService(
    @PrimaryKey(autoGenerate = true)
    val id_service: Int = 0,

    val id_kendaraan_service: Int, // Kolom kunci penghubung
    val tanggal_service: Date, // Tipe data Date (akan di-convert oleh Converters.kt)
    val jenis_service: String,
    val biaya: Double,
    val catatan: String
)