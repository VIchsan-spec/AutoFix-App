package com.autofix.logserviceapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityForgotPasswordBinding
// Import dari sub-paket database
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var appDao: AppDao
    private var emailToReset: String? = null // Untuk menyimpan email yang valid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDao = AppDatabase.getDatabase(this).appDao()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Tombol Kembali
        binding.imgBack.setOnClickListener {
            finish()
        }

        // Tombol Tahap 1: Cek Email
        binding.btnCekEmail.setOnClickListener {
            checkEmail()
        }

        // Tombol Tahap 2: Reset Password
        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun checkEmail() {
        val email = binding.etForgotEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Masukkan email", Toast.LENGTH_SHORT).show()
            return
        }

        // Cek email di database di background
        lifecycleScope.launch(Dispatchers.IO) {
            val pemilik = appDao.getPemilikByEmail(email)
            withContext(Dispatchers.Main) {
                if (pemilik != null) {
                    // Email ditemukan
                    emailToReset = email // Simpan email yang valid
                    binding.layoutResetPassword.visibility = View.VISIBLE // Tampilkan form reset
                    binding.etForgotEmail.isEnabled = false // Nonaktifkan input email
                    binding.btnCekEmail.isEnabled = false
                    Toast.makeText(this@ForgotPasswordActivity, "Email ditemukan, silakan masukkan password baru", Toast.LENGTH_SHORT).show()
                } else {
                    // Email tidak ditemukan
                    Toast.makeText(this@ForgotPasswordActivity, "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetPassword() {
        val passwordBaru = binding.etResetPasswordBaru.text.toString()
        val konfirmasiPassword = binding.etResetKonfirmasiPassword.text.toString()

        // Validasi
        if (passwordBaru.isEmpty() || konfirmasiPassword.isEmpty()) {
            Toast.makeText(this, "Password baru dan konfirmasi harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru != konfirmasiPassword) {
            Toast.makeText(this, "Password baru dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        emailToReset?.let { email ->
            val passwordHashBaru = passwordBaru.hashCode().toString()

            // Update password di database di background
            lifecycleScope.launch(Dispatchers.IO) {
                appDao.updatePasswordByEmail(email, passwordHashBaru)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ForgotPasswordActivity, "Password berhasil direset", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke LoginActivity
                }
            }
        } ?: run {
            Toast.makeText(this, "Error: Email tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}