package com.autofix.logserviceapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityRegisterBinding
// Import dari sub-paket database
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Pemilik
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var appDao: AppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Database
        appDao = AppDatabase.getDatabase(this).appDao()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Tombol Kembali
        binding.imgBack.setOnClickListener {
            finish()
        }

        // Tombol DAFTAR AKUN
        binding.btnDaftarAkun.setOnClickListener {
            handleRegister()
        }
    }

    /**
     * Menangani proses registrasi: validasi input dan simpan ke database.
     */
    private fun handleRegister() {
        // 1. Ambil teks dari semua input
        val namaLengkap = binding.etNamaLengkap.text.toString().trim()
        val email = binding.etEmailRegister.text.toString().trim()
        val password = binding.etPasswordRegister.text.toString()
        val konfirmasiPassword = binding.etKonfirmasiPassword.text.toString()

        // 2. Validasi input
        if (namaLengkap.isEmpty() || email.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != konfirmasiPassword) {
            Toast.makeText(this, "Password dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Jalankan perintah database di background
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val hashedPassword = password.hashCode().toString()

                // Buat objek Pemilik baru
                val pemilikBaru = Pemilik(
                    nama_lengkap = namaLengkap,
                    email = email,
                    password_hash = hashedPassword
                    // Kolom 'telepon' akan null (default)
                )

                // 4. Simpan ke database
                appDao.registerPemilik(pemilikBaru)

                // 5. Pindah ke thread UI (Main)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    finish() // Tutup halaman Register dan kembali ke Login
                }

            } catch (e: Exception) {
                // Tangani jika email sudah ada (jika email di-set 'unique')
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Gagal mendaftar: Email mungkin sudah ada", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}