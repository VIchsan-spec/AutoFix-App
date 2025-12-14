package com.autofix.logserviceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ActivityEditProfileSettingsBinding
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Pemilik
import com.autofix.logserviceapp.util.SessionManager
import com.autofix.logserviceapp.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileSettingsBinding
    private lateinit var appDao: AppDao
    private var idPemilikLogin: Int = -1
    private var currentPemilik: Pemilik? = null

    private var isSwitchNotifLoading = true
    private var isSwitchThemeLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDao = AppDatabase.getDatabase(this).appDao()
        idPemilikLogin = SessionManager.getSession(this)

        if (idPemilikLogin == -1) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadCurrentProfileData()
        setupClickListeners()
        setupSettings()
    }


    private fun loadCurrentProfileData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val pemilik = appDao.getPemilikById(idPemilikLogin)
            withContext(Dispatchers.Main) {
                if (pemilik != null) {
                    currentPemilik = pemilik
                    binding.etEditNama.setText(pemilik.nama_lengkap)
                    binding.etEditEmail.setText(pemilik.email)
                    binding.etEditTelepon.setText(pemilik.telepon ?: "")
                } else {
                    Toast.makeText(this@EditProfileSettingsActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnUpdate.setOnClickListener {
            handleSaveChanges()
        }
    }

    private fun setupSettings() {

        // --- Logika Mode Gelap ---
        val currentTheme = SessionManager.getTheme(this)
        isSwitchThemeLoading = true
        binding.switchModeGelap.isChecked = (currentTheme == AppCompatDelegate.MODE_NIGHT_YES)
        isSwitchThemeLoading = false
        binding.switchModeGelap.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchThemeLoading) {
                val newTheme = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(newTheme)
                SessionManager.saveTheme(this, newTheme)
                val themeName = if (isChecked) "Gelap" else "Terang"
                Toast.makeText(this, "Mode $themeName Diaktifkan", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Logika Notifikasi ---
        val isNotifEnabled = SessionManager.getNotificationPreference(this)
        isSwitchNotifLoading = true
        binding.switchNotifikasi.isChecked = isNotifEnabled
        isSwitchNotifLoading = false
        binding.switchNotifikasi.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchNotifLoading) {
                SessionManager.saveNotificationPreference(this, isChecked)
                if (isChecked) {
                    Toast.makeText(this, "Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notifikasi Dinonaktifkan", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // --- Logika Bahasa ---
        updateLanguageLabel()
        binding.btnChangeLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Indonesia", "English")
        val languageCodes = arrayOf("in", "en")

        val currentLangCode = SessionManager.getLanguage(this)
        val currentLangIndex = languageCodes.indexOf(currentLangCode).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Pilih Bahasa")
            .setSingleChoiceItems(languages, currentLangIndex) { dialog, which ->
                val selectedLangCode = languageCodes[which]

                // 1. Simpan bahasa baru
                SessionManager.saveLanguage(this, selectedLangCode)

                // 2. Terapkan bahasa baru
                val locales = LocaleListCompat.forLanguageTags(selectedLangCode)
                AppCompatDelegate.setApplicationLocales(locales)

                // 3. Tutup dialog
                dialog.dismiss()

                // 4. RESTART APLIKASI
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)

                // Selesaikan (finish) activity ini secara manual
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateLanguageLabel() {
        val currentLangCode = SessionManager.getLanguage(this)
        binding.tvCurrentLanguage.text = if (currentLangCode == "in") "ID" else "EN"
    }
    // =================================================

    private fun handleSaveChanges() {
        val namaBaru = binding.etEditNama.text.toString().trim()
        val emailBaru = binding.etEditEmail.text.toString().trim()
        val teleponBaru = binding.etEditTelepon.text.toString().trim()

        if (namaBaru.isEmpty() || emailBaru.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedPemilik = currentPemilik?.copy(
            nama_lengkap = namaBaru,
            email = emailBaru,
            telepon = teleponBaru
        )

        if (updatedPemilik != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                appDao.updatePemilik(updatedPemilik)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileSettingsActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            Toast.makeText(this, "Gagal menyimpan perubahan, data profil tidak ada", Toast.LENGTH_SHORT).show()
        }
    }
}