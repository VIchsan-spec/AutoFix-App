package com.autofix.logserviceapp.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "kendaraan_table",
    foreignKeys = [
        ForeignKey(
            entity = Pemilik::class, // Terhubung ke tabel Pemilik
            parentColumns = ["id_pemilik"],
            childColumns = ["id_pemilik_kendaraan"],
            onDelete = ForeignKey.CASCADE // Jika Pemilik dihapus, Kendaraan ikut hapus
        )
    ]
)
data class Kendaraan(
    @PrimaryKey(autoGenerate = true)
    val kendaraan_id: Int = 0,

    val id_pemilik_kendaraan: Int, // Kolom kunci penghubung
    val nama_kendaraan: String,
    val merek: String,
    val model: String,
    val nomor_polisi: String,
    val kilometer_terakhir: Int
)