package com.autofix.logserviceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityAddVehicleBinding
// Import dari sub-paket database
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Kendaraan
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddVehicleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVehicleBinding
    private lateinit var appDao: AppDao
    private var idPemilik: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil ID Pemilik yang dikirim (dari Home atau Register)
        idPemilik = intent.getIntExtra("ID_PEMILIK", -1)

        // Inisialisasi DAO
        appDao = AppDatabase.getDatabase(this).appDao()

        // Cek ID Pemilik
        if (idPemilik == -1) {
            Toast.makeText(this, "Error: Gagal mendapatkan ID Pemilik", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Atur listener tombol
        binding.btnSimpanKendaraan.setOnClickListener {
            handleSimpanKendaraan()
        }
    }

    private fun handleSimpanKendaraan() {
        // Ambil data dari form
        val nama = binding.etNamaKendaraan.text.toString().trim()
        val merek = binding.etMerek.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val nopol = binding.etNomorPolisi.text.toString().trim()
        val kilometerStr = binding.etKilometer.text.toString().trim()

        // Validasi
        if (nama.isEmpty() || merek.isEmpty() || model.isEmpty() || nopol.isEmpty() || kilometerStr.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val kilometer = kilometerStr.toIntOrNull()
        if (kilometer == null) {
            Toast.makeText(this, "Kilometer harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat objek Entity
        val kendaraanBaru = Kendaraan(
            id_pemilik_kendaraan = idPemilik,
            nama_kendaraan = nama,
            merek = merek,
            model = model,
            nomor_polisi = nopol,
            kilometer_terakhir = kilometer
        )

        // Simpan ke database di background
        lifecycleScope.launch(Dispatchers.IO) {
            appDao.addKendaraan(kendaraanBaru)

            // Setelah berhasil, kembali ke HomeActivity
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddVehicleActivity, "Kendaraan berhasil disimpan", Toast.LENGTH_SHORT).show()

                // Buka HomeActivity lagi (yang sekarang akan menemukan kendaraan ini)
                val intent = Intent(this@AddVehicleActivity, HomeActivity::class.java)
                // Hapus history agar tombol back tidak kembali ke form ini
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Tutup halaman ini
            }
        }
    }
}