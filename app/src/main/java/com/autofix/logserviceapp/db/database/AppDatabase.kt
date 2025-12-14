package com.autofix.logserviceapp.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
// Import file Converter, DAO, dan semua Entity
import com.autofix.logserviceapp.db.converter.Converters
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Kendaraan
import com.autofix.logserviceapp.db.entity.Pemilik
import com.autofix.logserviceapp.db.entity.Reminder
import com.autofix.logserviceapp.db.entity.RiwayatService

@Database(
    entities = [
        Pemilik::class,
        Kendaraan::class,
        RiwayatService::class,
        Reminder::class
    ],
    version = 1
)
@TypeConverters(Converters::class) // Daftar 'penerjemah' Date/Long
abstract class AppDatabase : RoomDatabase() {

    // Fungsi abstrak agar Room tahu cara mendapatkan DAO
    abstract fun appDao(): AppDao

    // Companion object (Singleton) untuk memastikan hanya ada
    // satu koneksi database yang dibuat di seluruh aplikasi.
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Jika INSTANCE tidak null, kembalikan.
            // Jika null, buat database baru.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autofix_database" // Nama file database lokal
                )
                    // Jika versi diubah, hapus database lama & buat baru
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                // Kembalikan instance baru
                instance
            }
        }
    }
}