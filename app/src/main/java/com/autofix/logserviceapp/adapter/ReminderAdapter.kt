package com.autofix.logserviceapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autofix.logserviceapp.databinding.ItemServiceReminderBinding
import com.autofix.logserviceapp.db.entity.Reminder
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderAdapter(
    private var listReminder: List<Reminder>,
    private val onDelete: (Reminder) -> Unit // Listener hapus
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(val binding: ItemServiceReminderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemServiceReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = listReminder[position]

        // Format tanggal
        val myFormat = "dd MMMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        holder.binding.tvReminderDate.text = sdf.format(reminder.tanggal_pengingat)

        holder.binding.tvReminderMessage.text = reminder.pesan_pengingat

        // Klik tombol sampah
        holder.binding.btnDeleteReminder.setOnClickListener {
            onDelete(reminder)
        }
    }

    override fun getItemCount(): Int {
        return listReminder.size
    }

    fun updateData(newList: List<Reminder>) {
        listReminder = newList
        notifyDataSetChanged()
    }
}