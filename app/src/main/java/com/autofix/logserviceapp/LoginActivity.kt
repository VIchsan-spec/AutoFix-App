package com.autofix.logserviceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityLoginBinding
// Import dari sub-paket
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Pemilik
import com.autofix.logserviceapp.util.SessionManager
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var appDao: AppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Terapkan Tema (Gelap/Terang) yang tersimpan SEBELUM layout dimuat
        val currentTheme = SessionManager.getTheme(this)
        AppCompatDelegate.setDefaultNightMode(currentTheme)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Database
        appDao = AppDatabase.getDatabase(this).appDao()

        // Cek apakah user sudah login sebelumnya
        checkSession()

        // Atur listener untuk tombol
        setupClickListeners()
    }

    /**
     * Cek apakah ada session (ID Pemilik) yang tersimpan.
     * Jika ada, langsung lewati login dan buka HomeActivity.
     */
    private fun checkSession() {
        val loggedInId = SessionManager.getSession(this)
        if (loggedInId != -1) {
            // Jika ID ada (sudah login), langsung ke Home
            goToHome(loggedInId)
        }
        // Jika -1, biarkan di halaman login
    }

    /**
     * Mengatur semua listener klik untuk tombol dan link di halaman ini.
     */
    private fun setupClickListeners() {
        // Pindah ke Halaman Register
        binding.tvDaftarAkun.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Jalankan logika Login
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        // Pindah ke Halaman Lupa Password
        binding.tvLupaPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Menangani proses login: validasi input, cek database, dan pindah halaman.
     */
    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validasi input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Jalankan pengecekan database di background
        lifecycleScope.launch(Dispatchers.IO) {
            val pemilik = appDao.getPemilikByEmail(email)

            // Kembali ke Main thread untuk update UI
            withContext(Dispatchers.Main) {
                if (pemilik == null) {
                    // Email tidak ditemukan
                    Toast.makeText(this@LoginActivity, "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
                } else {
                    // Email ditemukan, cek password
                    // (PERINGATAN: Hashing di dunia nyata tidak seperti ini!)
                    val hashedInputPassword = password.hashCode().toString()

                    if (pemilik.password_hash == hashedInputPassword) {
                        // Password cocok
                        Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                        // Simpan ID ke SessionManager
                        SessionManager.saveSession(this@LoginActivity, pemilik.id_pemilik)

                        // Pindah ke HomeActivity
                        goToHome(pemilik.id_pemilik)

                    } else {
                        // Password salah
                        Toast.makeText(this@LoginActivity, "Password salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Pindah ke HomeActivity dan tutup LoginActivity.
     */
    private fun goToHome(idPemilik: Int) {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        // Kirim ID (meskipun sudah ada di session, bisa berguna)
        intent.putExtra("ID_PEMILIK", idPemilik)
        startActivity(intent)
        finish() // Tutup halaman login agar tidak bisa kembali
    }
}