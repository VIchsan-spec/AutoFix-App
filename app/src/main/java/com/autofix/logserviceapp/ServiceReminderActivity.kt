package com.autofix.logserviceapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ActivityServiceReminderBinding
import com.autofix.logserviceapp.adapter.ReminderAdapter
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServiceReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceReminderBinding
    private lateinit var appDao: AppDao
    private var idKendaraan: Int = -1
    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDao = AppDatabase.getDatabase(this).appDao()
        idKendaraan = intent.getIntExtra("ID_KENDARAAN", -1)

        if (idKendaraan == -1) {
            Toast.makeText(this, "Error: Gagal mendapatkan ID Kendaraan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.fabAddReminder.setOnClickListener {
            val intent = Intent(this, AddReminderActivity::class.java)
            intent.putExtra("ID_KENDARAAN", idKendaraan)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadReminderData()
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(emptyList()) { reminder ->
            showDeleteDialog(reminder)
        }

        binding.rvServiceReminder.apply {
            layoutManager = LinearLayoutManager(this@ServiceReminderActivity)
            adapter = reminderAdapter
        }
    }

    private fun showDeleteDialog(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengingat?")
            .setMessage("Apakah Anda yakin ingin menghapus pengingat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteReminder(reminder)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteReminder(reminder: Reminder) {
        lifecycleScope.launch(Dispatchers.IO) {
            appDao.deleteReminder(reminder)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ServiceReminderActivity, "Pengingat dihapus", Toast.LENGTH_SHORT).show()
                loadReminderData()
            }
        }
    }

    private fun loadReminderData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val daftarReminder = appDao.getRemindersByKendaraan(idKendaraan)

            withContext(Dispatchers.Main) {
                showEmptyState(daftarReminder.isEmpty())
                reminderAdapter.updateData(daftarReminder)
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {// ini kalau isi halamannya kosong

        if (isEmpty) {
            binding.rvServiceReminder.visibility = View.GONE
            binding.emptyStateLayout.root.visibility = View.VISIBLE // Akses root dari include

            // Kustomisasi teks lewat binding juga
            binding.emptyStateLayout.tvEmptyTitle.text = "Belum Ada Pengingat"
            binding.emptyStateLayout.tvEmptyMessage.text = "Tambahkan jadwal servis agar tidak lupa!"
        } else {
            binding.rvServiceReminder.visibility = View.VISIBLE
            binding.emptyStateLayout.root.visibility = View.GONE
        }
    }
}