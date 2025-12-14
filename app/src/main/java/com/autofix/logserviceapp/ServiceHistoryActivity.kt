package com.autofix.logserviceapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ActivityServiceHistoryBinding
import com.autofix.logserviceapp.adapter.ServiceHistoryAdapter
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.RiwayatService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServiceHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceHistoryBinding
    private lateinit var appDao: AppDao
    private var idKendaraan: Int = -1
    private lateinit var historyAdapter: ServiceHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDao = AppDatabase.getDatabase(this).appDao()
        idKendaraan = intent.getIntExtra("ID_KENDARAAN", -1)

        if (idKendaraan == -1) {
            Toast.makeText(this, "Error: Gagal mendapatkan ID Kendaraan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        loadHistoryData()

        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = ServiceHistoryAdapter(emptyList()) { riwayat ->
            showDeleteDialog(riwayat)
        }

        binding.rvServiceHistory.apply {
            layoutManager = LinearLayoutManager(this@ServiceHistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun showDeleteDialog(riwayat: RiwayatService) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat?")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat servis ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteHistory(riwayat)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteHistory(riwayat: RiwayatService) {
        lifecycleScope.launch(Dispatchers.IO) {
            appDao.deleteRiwayatService(riwayat)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ServiceHistoryActivity, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                loadHistoryData()
            }
        }
    }

    private fun loadHistoryData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val daftarRiwayat = appDao.getRiwayatByKendaraan(idKendaraan)

            withContext(Dispatchers.Main) {

                showEmptyState(daftarRiwayat.isEmpty())

                historyAdapter.updateData(daftarRiwayat)
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        val emptyLayout = findViewById<View>(R.id.layoutEmptyState)

        if (isEmpty) {
            binding.rvServiceHistory.visibility = View.GONE
            emptyLayout?.visibility = View.VISIBLE

            // Kustomisasi teks
            emptyLayout?.findViewById<TextView>(R.id.tvEmptyTitle)?.text = "Belum Ada Riwayat"
            emptyLayout?.findViewById<TextView>(R.id.tvEmptyMessage)?.text = "Data servis Anda akan muncul di sini."
        } else {
            binding.rvServiceHistory.visibility = View.VISIBLE
            emptyLayout?.visibility = View.GONE
        }
    }
}