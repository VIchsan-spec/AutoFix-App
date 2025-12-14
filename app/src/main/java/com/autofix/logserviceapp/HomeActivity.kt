package com.autofix.logserviceapp

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ActivityHomeBinding
// Import dari sub-paket
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Kendaraan
import com.autofix.logserviceapp.util.SessionManager
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Import Format
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var appDao: AppDao
    private var idPemilikLogin: Int = -1
    private var kendaraanAktif: Kendaraan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDao = AppDatabase.getDatabase(this).appDao()
        idPemilikLogin = SessionManager.getSession(this)

        if (idPemilikLogin == -1) {
            Toast.makeText(this, "Error: Sesi tidak ditemukan", Toast.LENGTH_SHORT).show()
            logout()
            return
        }
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Muat data setiap kali halaman dibuka
        loadDataKendaraan()
    }

    private fun loadDataKendaraan() {
        lifecycleScope.launch(Dispatchers.IO) {
            val daftarKendaraan = appDao.getKendaraanByPemilik(idPemilikLogin)
            withContext(Dispatchers.Main) {
                if (daftarKendaraan.isEmpty()) {
                    Toast.makeText(this@HomeActivity, "Anda belum mendaftarkan kendaraan", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@HomeActivity, AddVehicleActivity::class.java)
                    intent.putExtra("ID_PEMILIK", idPemilikLogin)
                    startActivity(intent)
                    finish()
                } else {
                    val idKendaraanAktif = SessionManager.getActiveVehicle(this@HomeActivity)
                    var vehicle = daftarKendaraan.find { it.kendaraan_id == idKendaraanAktif }
                    if (vehicle == null) {
                        vehicle = daftarKendaraan[0]
                        SessionManager.saveActiveVehicle(this@HomeActivity, vehicle.kendaraan_id)
                    }
                    kendaraanAktif = vehicle

                    binding.tvHomeTitle.text = kendaraanAktif?.nama_kendaraan ?: "Home"

                    // Muat data dengan animasi
                    loadServiceInfoAndCost()
                }
            }
        }
    }

    private fun loadServiceInfoAndCost() {
        kendaraanAktif?.let { kendaraan ->
            lifecycleScope.launch(Dispatchers.IO) {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val countCompleted = appDao.getCountCompletedService(kendaraan.kendaraan_id)
                val countReminders = appDao.getCountReminders(kendaraan.kendaraan_id)
                val countOversus = appDao.getCountOversusReminders(kendaraan.kendaraan_id, today)

                val startTime = 0L
                val endTime = System.currentTimeMillis()
                val totalCost = appDao.getTotalCostByKendaraan(kendaraan.kendaraan_id, startTime, endTime) ?: 0.0

                withContext(Dispatchers.Main) {

                    // Animasi Angka Service Info
                    val cardServiceInfo = binding.cardServiceInfo
                    animateTextView(cardServiceInfo.findViewById(R.id.tvCompletedCount), countCompleted)
                    animateTextView(cardServiceInfo.findViewById(R.id.tvUpexCount), (countReminders - countOversus))
                    animateTextView(cardServiceInfo.findViewById(R.id.tvOversusCount), countOversus)

                    // Animasi Angka Total Biaya
                    val tvTotalCost = binding.cardTotalCost.findViewById<TextView>(R.id.tvTotalCostValue)
                    animateTextViewDouble(tvTotalCost, totalCost)
                }
            }
        }
    }

    private fun animateTextView(textView: TextView, endValue: Int) {
        val animator = ValueAnimator.ofInt(0, endValue)
        animator.duration = 1000 // Durasi 1 detik
        animator.interpolator = DecelerateInterpolator() // Efek melambat di akhir
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    private fun animateTextViewDouble(textView: TextView, endValue: Double) {
        val animator = ValueAnimator.ofFloat(0f, endValue.toFloat())
        animator.duration = 1500 // Durasi 1.5 detik
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0
            textView.text = formatter.format(value)
        }
        animator.start()
    }

    private fun setupClickListeners() {
        binding.tvHomeTitle.setOnClickListener {
            startActivity(Intent(this, VehicleListActivity::class.java))
        }
        binding.imgProfileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.btnAddService.setOnClickListener {
            kendaraanAktif?.let {
                val intent = Intent(this, AddServiceActivity::class.java)
                intent.putExtra("ID_KENDARAAN", it.kendaraan_id)
                startActivity(intent)
            }
        }
        binding.btnServiceHistory.setOnClickListener {
            kendaraanAktif?.let {
                val intent = Intent(this, ServiceHistoryActivity::class.java)
                intent.putExtra("ID_KENDARAAN", it.kendaraan_id)
                startActivity(intent)
            }
        }
        binding.btnServiceReminder.setOnClickListener {
            kendaraanAktif?.let {
                val intent = Intent(this, ServiceReminderActivity::class.java)
                intent.putExtra("ID_KENDARAAN", it.kendaraan_id)
                startActivity(intent)
            }
        }
        binding.cardTotalCost.setOnClickListener {
            kendaraanAktif?.let {
                val intent = Intent(this, TotalCostActivity::class.java)
                intent.putExtra("ID_KENDARAAN", it.kendaraan_id)
                startActivity(intent)
            }
        }
        // Tombol Chatbot (FAB)
        binding.fabChatbot.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }
    }

    private fun logout() {
        SessionManager.clearSession(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}