package com.autofix.logserviceapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// Import R (Resources)
import com.autofix.logserviceapp.R
// Import ViewBinding
import com.autofix.logserviceapp.databinding.ActivityTotalCostBinding
// Import dari sub-paket database
import com.autofix.logserviceapp.db.database.AppDatabase
import com.autofix.logserviceapp.db.dao.AppDao
import com.autofix.logserviceapp.db.dao.CostByCategory
// Import library Pie Chart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
// Import Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Import untuk format Rupiah dan Waktu
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class TotalCostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTotalCostBinding
    private lateinit var appDao: AppDao
    private var idKendaraan: Int = -1

    //jenis filter
    private enum class FilterType { THIS_YEAR, THIS_MONTH, LAST_6_MONTHS, ALL_TIME }
    private var currentFilter: FilterType = FilterType.THIS_YEAR // Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalCostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Database dan ambil ID Kendaraan
        appDao = AppDatabase.getDatabase(this).appDao()
        idKendaraan = intent.getIntExtra("ID_KENDARAAN", -1)

        // Cek ID Kendaraan
        if (idKendaraan == -1) {
            Toast.makeText(this, "Error: Gagal mendapatkan ID Kendaraan", Toast.LENGTH_LONG).show()
            finish(); return
        }

        binding.imgBack.setOnClickListener { finish() }

        setupTimeFilterSpinner() // Setup Spinner
        setupPieChart()
        // loadCostData() akan dipanggil oleh onItemSelectedListener saat setup
    }

    /**
     * Mengatur Spinner (dropdown) untuk filter waktu.
     */
    private fun setupTimeFilterSpinner() {
        // Buat adapter untuk Spinner dari string array
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.cost_filters, // Ambil opsi dari strings.xml
            R.layout.spinner_item_layout // Layout kustom kita
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout) // Layout kustom kita
        }
        binding.spinnerFilterTime.adapter = adapter

        // Listener saat item dipilih
        binding.spinnerFilterTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Tentukan filter berdasarkan posisi
                currentFilter = when (position) {
                    0 -> FilterType.THIS_YEAR
                    1 -> FilterType.THIS_MONTH
                    2 -> FilterType.LAST_6_MONTHS
                    3 -> FilterType.ALL_TIME
                    else -> FilterType.THIS_YEAR
                }
                // Muat ulang data dengan filter baru
                loadCostData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* Abaikan */ }
        }
    }

    /**
     * Mengatur tampilan dasar Pie Chart (tanpa data).
     */
    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isRotationEnabled = true
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK) // Persentase di dalam chart
            setEntryLabelTextSize(12f)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setNoDataText("Belum ada data biaya servis")
            setNoDataTextColor(Color.WHITE)
            legend.isEnabled = false // Matikan legend bawaan
        }
    }

    /**
     * Mengambil data biaya dari database berdasarkan filter waktu,
     * lalu menampilkan Pie Chart dan legend kustom.
     */
    private fun loadCostData() {
        // Tentukan rentang waktu (startTime, endTime) berdasarkan filter
        val now = Calendar.getInstance()
        val startTime: Long
        val endTime: Long

        when (currentFilter) {
            FilterType.THIS_YEAR -> {
                startTime = Calendar.getInstance().apply { set(Calendar.YEAR, now.get(Calendar.YEAR)); set(Calendar.DAY_OF_YEAR, 1); setTimeFieldsToStartOfDay(this) }.timeInMillis
                endTime = Calendar.getInstance().apply { set(Calendar.YEAR, now.get(Calendar.YEAR)); set(Calendar.MONTH, Calendar.DECEMBER); set(Calendar.DAY_OF_MONTH, 31); setTimeFieldsToEndOfDay(this) }.timeInMillis
            }
            FilterType.THIS_MONTH -> {
                startTime = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); setTimeFieldsToStartOfDay(this) }.timeInMillis
                endTime = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)); setTimeFieldsToEndOfDay(this) }.timeInMillis
            }
            FilterType.LAST_6_MONTHS -> {
                endTime = now.timeInMillis // Akhir = Sekarang
                startTime = Calendar.getInstance().apply { add(Calendar.MONTH, -6); setTimeFieldsToStartOfDay(this) }.timeInMillis
            }
            FilterType.ALL_TIME -> {
                startTime = 0L // Awal waktu (tahun 1970)
                endTime = now.timeInMillis // Akhir = Sekarang
            }
        }

        // Ambil data dari database di background
        lifecycleScope.launch(Dispatchers.IO) {
            val totalCost = appDao.getTotalCostByKendaraan(idKendaraan, startTime, endTime) ?: 0.0
            val breakdown = appDao.getCostBreakdownByKendaraan(idKendaraan, startTime, endTime)

            // Kembali ke UI thread untuk update tampilan
            withContext(Dispatchers.Main) {
                // Update TextView Total Biaya (Format Rupiah)
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                formatter.maximumFractionDigits = 0
                binding.tvTotalCostValue.text = formatter.format(totalCost)

                // Hapus legend kustom lama sebelum menambah yang baru
                binding.customLegendLayout.removeAllViews()

                if (breakdown.isNotEmpty()) {
                    val entries = ArrayList<PieEntry>()
                    val totalBiayaSemua = breakdown.sumOf { it.total_biaya } // Total untuk hitung %
                    val colors = ColorTemplate.COLORFUL_COLORS.toList()

                    breakdown.forEachIndexed { index, item ->
                        // 1. Buat PieEntry
                        entries.add(PieEntry(item.total_biaya.toFloat(), "")) // Label kosong

                        // 2. Buat Tampilan Legend Kustom
                        val inflater = layoutInflater
                        val legendItemView = inflater.inflate(R.layout.custom_legend_item, binding.customLegendLayout, false)

                        // 3. Set data untuk legend item
                        val color = colors[index % colors.size]
                        legendItemView.findViewById<View>(R.id.legendColorView).background.setTint(color)
                        legendItemView.findViewById<TextView>(R.id.legendLabelText).text = item.jenis_service

                        // Hitung dan set persentase
                        val percentage = (item.total_biaya / totalBiayaSemua) * 100
                        legendItemView.findViewById<TextView>(R.id.legendPercentText).text = String.format(Locale.US, "%.0f%%", percentage)

                        // 4. Tambahkan legend item ke layout
                        binding.customLegendLayout.addView(legendItemView)
                    }

                    // Atur data dan tampilan Pie Chart
                    val dataSet = PieDataSet(entries, "")
                    dataSet.colors = colors
                    dataSet.setDrawValues(true) // Tampilkan persentase di chart
                    dataSet.valueTextColor = Color.BLACK
                    dataSet.valueTextSize = 14f
                    dataSet.valueFormatter = PercentFormatter(binding.pieChart)

                    binding.pieChart.data = PieData(dataSet)
                    binding.pieChart.invalidate()
                    binding.pieChart.animateY(1000) // Animasi
                } else {
                    // Jika tidak ada data, kosongkan chart
                    binding.pieChart.clear()
                    binding.pieChart.invalidate()
                }
            }
        }
    }

    private fun setTimeFieldsToStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
    }
    private fun setTimeFieldsToEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
    }
}