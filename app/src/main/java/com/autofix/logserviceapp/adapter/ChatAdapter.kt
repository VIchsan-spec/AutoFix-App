package com.autofix.logserviceapp.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ItemChatBinding
import com.autofix.logserviceapp.ChatMessage // Import dari paket utama

class ChatAdapter(private val messages: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]
        val params = holder.binding.tvMessage.layoutParams as LinearLayout.LayoutParams

        if (chat.isUser) {
            // Pesan User: Rata Kanan, Backgroundnya warna Emas intinya sih ini pesan si user
            params.gravity = Gravity.END
            holder.binding.tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_user)
            holder.binding.tvMessage.setTextColor(Color.BLACK)
        } else {
            // Pesan Bot: Rata Kiri, Backgroundnya warna Abu-abu ini pesan si bot
            params.gravity = Gravity.START
            holder.binding.tvMessage.setBackgroundResource(R.drawable.bg_chat_bubble_bot)
            holder.binding.tvMessage.setTextColor(Color.WHITE)
        }

        holder.binding.tvMessage.layoutParams = params
        holder.binding.tvMessage.text = chat.message
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}