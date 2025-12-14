package com.autofix.logserviceapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autofix.logserviceapp.databinding.ItemServiceHistoryBinding
import com.autofix.logserviceapp.db.entity.RiwayatService
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceHistoryAdapter(
    private var listRiwayat: List<RiwayatService>,
    private val onDelete: (RiwayatService) -> Unit // Listener hapus
) : RecyclerView.Adapter<ServiceHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemServiceHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemServiceHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val riwayat = listRiwayat[position]

        // Format tanggal
        val dateFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        holder.binding.tvHistoryDate.text = sdf.format(riwayat.tanggal_service)

        holder.binding.tvHistoryServiceType.text = riwayat.jenis_service

        // Format uang
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        holder.binding.tvHistoryCost.text = formatter.format(riwayat.biaya)

        // Klik tombol sampah
        holder.binding.btnDeleteHistory.setOnClickListener {
            onDelete(riwayat)
        }
    }

    override fun getItemCount(): Int {
        return listRiwayat.size
    }

    fun updateData(newList: List<RiwayatService>) {
        listRiwayat = newList
        notifyDataSetChanged()
    }
}