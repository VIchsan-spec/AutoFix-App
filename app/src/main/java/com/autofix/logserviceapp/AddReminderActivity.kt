package com.autofix.logserviceapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityAddReminderBinding
// Import dari sub-paket database
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Reminder
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Import untuk Tanggal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private val myCalendar = Calendar.getInstance()
    private lateinit var appDao: AppDao
    private var idKendaraan: Int = -1
    private var selectedDate: Date? = null // Untuk menyimpan objek Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
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
        binding.btnAddReminder.setOnClickListener { handleSaveReminder() }
    }

    private fun handleSaveReminder() {
        // Ambil data dari form
        val tanggal = selectedDate
        val pesan = binding.etReminderMessage.text.toString().trim()

        // Validasi
        if (tanggal == null || pesan.isEmpty()) {
            Toast.makeText(this, "Tanggal dan Pesan harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat objek Entity
        val reminderBaru = Reminder(
            id_kendaraan_reminder = idKendaraan,
            tanggal_pengingat = tanggal!!, // Simpan objek Date
            pesan_pengingat = pesan
        )

        // Simpan ke database di background
        lifecycleScope.launch(Dispatchers.IO) {
            appDao.addReminder(reminderBaru)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddReminderActivity, "Pengingat berhasil disimpan", Toast.LENGTH_SHORT).show()
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
            updateLabel()
        }

        binding.etReminderDate.isFocusable = false
        binding.etReminderDate.isClickable = true
        binding.etReminderDate.setOnClickListener {
            DatePickerDialog(
                this@AddReminderActivity,
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
            val myFormat = "dd MMMM yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            binding.etReminderDate.setText(sdf.format(it))
        }
    }
}