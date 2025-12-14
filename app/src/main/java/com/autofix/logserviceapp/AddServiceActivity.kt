package com.autofix.logserviceapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityAddServiceBinding
// Import dari sub-paket database
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.RiwayatService
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Import untuk Tanggal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddServiceBinding
    private val myCalendar = Calendar.getInstance()
    private lateinit var appDao: AppDao
    private var idKendaraan: Int = -1
    private var selectedDate: Date? = null // Untuk menyimpan objek Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Database dan ambil ID Kendaraan
        appDao = AppDatabase.getDatabase(this).appDao()
        idKendaraan = intent.getIntExtra("ID_KENDARAAN", -1)

        // Cek ID Kendaraan
        if (idKendaraan == -1) {
            Toast.makeText(this, "Error: Gagal mendapatkan ID Kendaraan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Atur listener tombol
        binding.imgBack.setOnClickListener { finish() }
        setupDatePicker()
        binding.btnAddServiceForm.setOnClickListener { handleSaveService() }
    }

    private fun handleSaveService() {
        // Ambil data dari form
        val tanggal = selectedDate
        val jenisServis = binding.etServiceType.text.toString().trim()
        val biayaStr = binding.etCost.text.toString().trim()
        val catatan = binding.etNotes.text.toString().trim()

        // Validasi
        if (tanggal == null || jenisServis.isEmpty() || biayaStr.isEmpty()) {
            Toast.makeText(this, "Tanggal, Jenis Servis, dan Biaya harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val biaya = biayaStr.toDoubleOrNull()
        if (biaya == null) {
            Toast.makeText(this, "Biaya harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat objek Entity
        val riwayatBaru = RiwayatService(
            id_kendaraan_service = idKendaraan,
            tanggal_service = tanggal!!, // Simpan objek Date
            jenis_service = jenisServis,
            biaya = biaya,
            catatan = catatan
        )

        // Simpan ke database di background
        lifecycleScope.launch(Dispatchers.IO) {
            appDao.addRiwayatService(riwayatBaru)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddServiceActivity, "Servis berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish() // Tutup halaman
            }
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            // Setel waktu ke awal hari (00:00:00) agar filter akurat
            myCalendar.set(Calendar.HOUR_OF_DAY, 0)
            myCalendar.set(Calendar.MINUTE, 0)
            myCalendar.set(Calendar.SECOND, 0)
            myCalendar.set(Calendar.MILLISECOND, 0)

            selectedDate = myCalendar.time // Simpan sebagai objek Date
            updateLabel() // Perbarui teks di EditText
        }

        binding.etSelectDate.isFocusable = false
        binding.etSelectDate.isClickable = true
        binding.etSelectDate.setOnClickListener {
            DatePickerDialog(
                this@AddServiceActivity,
                dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateLabel() {
        // Fungsi ini untuk menampilkan teks di EditText
        selectedDate?.let {
            val myFormat = "dd MMMM yyyy" // cth: 23 Oktober 2025
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            binding.etSelectDate.setText(sdf.format(it))
        }
    }
}