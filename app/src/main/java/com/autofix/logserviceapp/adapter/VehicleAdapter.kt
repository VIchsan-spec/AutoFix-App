package com.autofix.logserviceapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autofix.logserviceapp.databinding.ItemVehicleBinding
import com.autofix.logserviceapp.db.entity.Kendaraan

class VehicleAdapter(
    private var listKendaraan: List<Kendaraan>,
    private val clickListener: (Kendaraan) -> Unit, // Listener untuk klik item
    private val onDelete: (Kendaraan) -> Unit // Listener untuk tombol hapus
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(val binding: ItemVehicleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val kendaraan = listKendaraan[position]

        holder.binding.tvVehicleName.text = kendaraan.nama_kendaraan
        holder.binding.tvVehicleDetails.text = "${kendaraan.merek} ${kendaraan.model} - ${kendaraan.nomor_polisi}"

        // Klik pada kartu untuk memilih kendaraan
        holder.itemView.setOnClickListener {
            clickListener(kendaraan)
        }

        // Klik tombol sampah untuk menghapus
        holder.binding.btnDeleteVehicle.setOnClickListener {
            onDelete(kendaraan)
        }
    }

    override fun getItemCount(): Int {
        return listKendaraan.size
    }

    fun updateData(newList: List<Kendaraan>) {
        listKendaraan = newList
        notifyDataSetChanged()
    }
}