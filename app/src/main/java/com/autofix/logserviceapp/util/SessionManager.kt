package com.autofix.logserviceapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Object Singleton untuk mengelola SharedPreferences aplikasi.
 * Ini menyimpan data session seperti ID pengguna, kendaraan aktif, tema, notifikasi, dan bahasa.
 */
object SessionManager {

    private const val PREFS_NAME = "AutoFixSession"
    private const val KEY_ID_PEMILIK = "id_pemilik"
    private const val KEY_ID_KENDARAAN = "id_kendaraan_aktif"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_NOTIF_ENABLED = "notif_enabled"
    private const val KEY_LANGUAGE = "language"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Pemilik (User) Session ---

    /**
     * Menyimpan ID Pemilik (user) setelah login berhasil.
     */
    fun saveSession(context: Context, idPemilik: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_ID_PEMILIK, idPemilik)
        editor.apply()
    }

    /**
     * Mengambil ID Pemilik yang sedang login.
     * @return ID Pemilik, atau -1 jika tidak ada session (belum login).
     */
    fun getSession(context: Context): Int {
        return getPreferences(context).getInt(KEY_ID_PEMILIK, -1)
    }

    // --- Kendaraan Session ---

    /**
     * Menyimpan ID Kendaraan yang dipilih pengguna sebagai kendaraan aktif.
     */
    fun saveActiveVehicle(context: Context, idKendaraan: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_ID_KENDARAAN, idKendaraan)
        editor.apply()
    }

    /**
     * Mengambil ID Kendaraan yang sedang aktif.
     * @return ID Kendaraan, atau -1 jika belum ada yang dipilih.
     */
    fun getActiveVehicle(context: Context): Int {
        return getPreferences(context).getInt(KEY_ID_KENDARAAN, -1)
    }

    // --- Tema (Mode Gelap/Terang) ---

    /**
     * Menyimpan preferensi tema (Mode Gelap atau Terang).
     * Gunakan konstanta dari AppCompatDelegate (MODE_NIGHT_YES / MODE_NIGHT_NO).
     */
    fun saveTheme(context: Context, themeMode: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_THEME_MODE, themeMode)
        editor.apply()
    }

    /**
     * Mengambil preferensi tema yang tersimpan.
     * @return Mode tema (defaultnya MODE_NIGHT_NO atau Mode Terang).
     */
    fun getTheme(context: Context): Int {
        return getPreferences(context).getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO)
    }

    // --- Notifikasi ---

    /**
     * Menyimpan preferensi notifikasi (On/Off).
     */
    fun saveNotificationPreference(context: Context, isEnabled: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_NOTIF_ENABLED, isEnabled)
        editor.apply()
    }

    /**
     * Mengambil preferensi notifikasi.
     * @return true jika notifikasi diizinkan (default), false jika tidak.
     */
    fun getNotificationPreference(context: Context): Boolean {
        // Default-nya 'true' (notifikasi aktif)
        return getPreferences(context).getBoolean(KEY_NOTIF_ENABLED, true)
    }

    // --- Bahasa ---

    /**
     * Menyimpan kode bahasa (cth: "in" atau "en").
     */
    fun saveLanguage(context: Context, languageCode: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_LANGUAGE, languageCode)
        editor.apply()
    }

    /**
     * Mengambil kode bahasa yang tersimpan.
     * @return Kode bahasa (defaultnya "in" untuk Indonesia).
     */
    fun getLanguage(context: Context): String {
        // Default ke "in" (Indonesia)
        return getPreferences(context).getString(KEY_LANGUAGE, "in") ?: "in"
    }

    // --- Hapus Session (Logout) ---

    /**
     * Menghapus data session pengguna (ID Pemilik dan ID Kendaraan) saat logout.
     * Preferensi tema, notifikasi, dan bahasa TIDAK dihapus.
     */
    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_ID_PEMILIK)
        editor.remove(KEY_ID_KENDARAAN)
        editor.apply()
    }
}