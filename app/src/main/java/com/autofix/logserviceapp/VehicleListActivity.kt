package com.autofix.logserviceapp

import android.content.Intent
import android.os.Bundle
import android.view.View // Import View
import android.widget.TextView // Import TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ActivityVehicleListBinding
import com.autofix.logserviceapp.adapter.VehicleAdapter
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.entity.Kendaraan
import com.autofix.logserviceapp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VehicleListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleListBinding
    private lateinit var appDao: AppDao
    private var idPemilik: Int = -1
    private lateinit var vehicleAdapter: VehicleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idPemilik = SessionManager.getSession(this)

        if (idPemilik == -1) {
            Toast.makeText(this, "Error: Sesi tidak ditemukan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        appDao = AppDatabase.getDatabase(this).appDao()

        setupRecyclerView()

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.fabAddVehicle.setOnClickListener {
            val intent = Intent(this, AddVehicleActivity::class.java)
            intent.putExtra("ID_PEMILIK", idPemilik)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadVehicleData()
    }

    private fun setupRecyclerView() {
        vehicleAdapter = VehicleAdapter(
            listKendaraan = emptyList(),
            clickListener = { kendaraan ->
                SessionManager.saveActiveVehicle(this, kendaraan.kendaraan_id)
                Toast.makeText(this, "${kendaraan.nama_kendaraan} dipilih", Toast.LENGTH_SHORT).show()
                finish()
            },
            onDelete = { kendaraan ->
                showDeleteDialog(kendaraan)
            }
        )

        binding.rvVehicleList.apply {
            layoutManager = LinearLayoutManager(this@VehicleListActivity)
            adapter = vehicleAdapter
        }
    }

    private fun showDeleteDialog(kendaraan: Kendaraan) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kendaraan?")
            .setMessage("Apakah Anda yakin ingin menghapus '${kendaraan.nama_kendaraan}'? Semua data servis dan pengingat terkait juga akan terhapus.")
            .setPositiveButton("Hapus") { _, _ ->
                deleteVehicle(kendaraan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteVehicle(kendaraan: Kendaraan) {
        lifecycleScope.launch(Dispatchers.IO) {
            appDao.deleteKendaraan(kendaraan)

            val activeVehicleId = SessionManager.getActiveVehicle(this@VehicleListActivity)
            if (activeVehicleId == kendaraan.kendaraan_id) {
                SessionManager.saveActiveVehicle(this@VehicleListActivity, -1)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@VehicleListActivity, "Kendaraan dihapus", Toast.LENGTH_SHORT).show()
                loadVehicleData()
            }
        }
    }

    private fun loadVehicleData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val daftarKendaraan = appDao.getKendaraanByPemilik(idPemilik)

            withContext(Dispatchers.Main) {
                // =================================================
                // LOGIKA EMPTY STATE
                // =================================================
                showEmptyState(daftarKendaraan.isEmpty())
                // =================================================

                vehicleAdapter.updateData(daftarKendaraan)
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        val emptyLayout = findViewById<View>(R.id.layoutEmptyState)

        if (isEmpty) {
            binding.rvVehicleList.visibility = View.GONE
            emptyLayout?.visibility = View.VISIBLE

            // Kustomisasi teks
            emptyLayout?.findViewById<TextView>(R.id.tvEmptyTitle)?.text = "Belum Ada Kendaraan"
            emptyLayout?.findViewById<TextView>(R.id.tvEmptyMessage)?.text = "Tambahkan kendaraan Anda sekarang!"
        } else {
            binding.rvVehicleList.visibility = View.VISIBLE
            emptyLayout?.visibility = View.GONE
        }
    }
}