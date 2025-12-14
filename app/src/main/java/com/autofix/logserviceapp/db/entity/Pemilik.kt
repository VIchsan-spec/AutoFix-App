package com.autofix.logserviceapp.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pemilik_table")
data class Pemilik(
    @PrimaryKey(autoGenerate = true)
    val id_pemilik: Int = 0,

    val nama_lengkap: String,
    val email: String,
    val password_hash: String,
    val telepon: String? = null // Nullable agar data lama tidak error
)