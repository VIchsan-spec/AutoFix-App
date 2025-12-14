package com.autofix.logserviceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityProfileBinding
// Import dari sub-paket
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Pemilik
import com.autofix.logserviceapp.util.SessionManager
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var appDao: AppDao
    private var idPemilikLogin: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Database dan Session
        appDao = AppDatabase.getDatabase(this).appDao()
        idPemilikLogin = SessionManager.getSession(this)

        // Cek apakah session valid
        if (idPemilikLogin == -1) {
            Toast.makeText(this, "Sesi berakhir, silakan login kembali", Toast.LENGTH_SHORT).show()
            handleLogout() // Paksa logout jika session tidak ada
            return
        }

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Muat (atau muat ulang) data profil setiap kali halaman ini ditampilkan
        loadProfileData()
    }

    /**
     * Memuat data Pemilik dari database berdasarkan ID di session,
     * lalu menampilkannya ke UI.
     */
    private fun loadProfileData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val pemilik = appDao.getPemilikById(idPemilikLogin)

            withContext(Dispatchers.Main) {
                if (pemilik != null) {
                    // Tampilkan data ke UI
                    binding.tvProfileName.text = pemilik.nama_lengkap
                    binding.tvProfileEmail.text = pemilik.email
                } else {
                    // Jika data tidak ada (terhapus?), paksa logout
                    Toast.makeText(this@ProfileActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    handleLogout()
                }
            }
        }
    }

    /**
     * Mengatur semua listener klik di halaman ini.
     */
    private fun setupClickListeners() {
        // Tombol Kembali
        binding.imgBack.setOnClickListener {
            finish()
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // Tombol gabungan "Edit Profile & Pengaturan"
        binding.btnEditProfileSettings.setOnClickListener {
            val intent = Intent(this, EditProfileSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Menampilkan dialog konfirmasi sebelum logout.
     */
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
            .setPositiveButton("Ya, Logout") { _, _ ->
                handleLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /**
     * Menangani proses logout: hapus session dan kembali ke Login.
     */
    private fun handleLogout() {
        // Hapus session
        SessionManager.clearSession(this)

        // Kembali ke halaman Login
        val intent = Intent(this, LoginActivity::class.java)
        // Hapus semua activity sebelumnya dari tumpukan (stack)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup halaman Profile
    }
}