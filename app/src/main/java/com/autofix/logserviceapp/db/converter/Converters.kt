package com.autofix.logserviceapp.db.converter

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    /**
     * Mengubah Long (angka milidetik) dari database menjadi objek Date.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        // Jika value-nya tidak null, buat objek Date baru darinya
        return value?.let { Date(it) }
    }

    /**
     * Mengubah objek Date dari aplikasi menjadi Long (angka milidetik) untuk disimpan.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        // Jika date-nya tidak null, ambil .time (angka milidetiknya)
        return date?.time
    }
}