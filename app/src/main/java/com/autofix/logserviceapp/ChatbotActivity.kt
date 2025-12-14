package com.autofix.logserviceapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
// Import R dan Binding
import com.autofix.logserviceapp.R
import com.autofix.logserviceapp.databinding.ActivityChatbotBinding
// Import Adapter (dari sub-paket adapter)
import com.autofix.logserviceapp.adapter.ChatAdapter
// Import Model (dari paket utama)
import com.autofix.logserviceapp.ChatMessage
// Import JSON handling
import org.json.JSONArray
import java.io.IOException

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = ArrayList<ChatMessage>()

    // Struktur data untuk menyimpan pertanyaan & jawaban dari JSON
    data class QnA(
        val pertanyaan: String,
        val jawaban: String,
        val keywords: List<String>
    )

    private val knowledgeBase = ArrayList<QnA>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // 1. Muat database pengetahuan saat aplikasi dibuka
        loadKnowledgeBase()

        // 2. Tampilkan pesan sambutan
        addBotMessage("Halo! Saya asisten montir Auto Fix. Silakan tanya soal perawatan, harga sparepart, atau tips motor.")

        // 3. Listener tombol kirim
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        // 4. Listener tombol kembali
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        binding.rvChat.adapter = chatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(this)
    }

    // --- LOGIKA CHATBOT ---

    private fun loadKnowledgeBase() {
        try {
            // Membaca file JSON dari folder assets
            val jsonString = assets.open("knowledge_base.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val pertanyaan = obj.getString("pertanyaan")
                val jawaban = obj.getString("jawaban")

                // Ambil array kata kunci
                val keywordsArray = obj.getJSONArray("kata_kunci")
                val keywords = ArrayList<String>()
                for (j in 0 until keywordsArray.length()) {
                    // Simpan semua keyword dalam huruf kecil
                    keywords.add(keywordsArray.getString(j).lowercase())
                }

                knowledgeBase.add(QnA(pertanyaan, jawaban, keywords))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat database pengetahuan.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessage(text: String) {
        // Tampilkan pesan user (Kanan)
        chatAdapter.addMessage(ChatMessage(text, true))

        // Bersihkan input & scroll ke bawah
        binding.etMessage.text.clear()
        binding.rvChat.smoothScrollToPosition(messageList.size - 1)

        // Cari jawaban terbaik
        val answer = findAnswer(text)

        // Tampilkan jawaban bot dengan sedikit jeda (agar terlihat natural)
        binding.rvChat.postDelayed({
            addBotMessage(answer)
        }, 500)
    }

    // --- LOGIKA PENCARIAN PINTAR (SCORING SYSTEM) ---
    private fun findAnswer(query: String): String {
        // 1. Bersihkan input user: hapus tanda baca & ubah ke huruf kecil
        val cleanedQuery = query.lowercase().replace(Regex("[^a-z0-9 ]"), "")

        // 2. Pecah kalimat menjadi kata-kata (tokens)
        // Contoh: "berapa harga oli?" -> ["berapa", "harga", "oli"]
        val userWords = cleanedQuery.split(" ")

        var bestMatch: QnA? = null
        var maxScore = 0

        // 3. Cek setiap pertanyaan di database
        for (item in knowledgeBase) {
            var score = 0

            // A. Cek Kecocokan Kata Kunci (+2 Poin)
            for (keyword in item.keywords) {
                // Jika salah satu kata user COCOK dengan kata kunci
                if (userWords.contains(keyword)) {
                    score += 2
                }
                // Bonus: Jika kata user MENGANDUNG kata kunci (misal: "olinya" mengandung "oli")
                else if (keyword.length > 3 && userWords.any { it.contains(keyword) }) {
                    score += 1
                }
            }

            // B. Cek Kecocokan Judul Pertanyaan (+1 Poin)
            // pecah judul pertanyaan di database jadi kata-kata juga
            val questionWords = item.pertanyaan.lowercase().replace(Regex("[^a-z0-9 ]"), "").split(" ")
            for (qWord in questionWords) {
                if (userWords.contains(qWord)) {
                    score += 1
                }
            }

            // C. Simpan jika skor ini adalah yang tertinggi sejauh ini
            if (score > maxScore) {
                maxScore = score
                bestMatch = item
            }
        }

        // 4. Ambang Batas (Threshold)
        // Minimal skor harus >= 2 agar dianggap bukan kebetulan
        return if (maxScore >= 2 && bestMatch != null) {
            bestMatch.jawaban
        } else {
            "Maaf, saya belum paham. Coba gunakan kata kunci yang lebih spesifik, misal: 'harga oli', 'tekanan ban', 'ganti rem', atau 'servis'."
        }
    }

    private fun addBotMessage(text: String) {
        // Tampilkan pesan bot (Kiri)
        chatAdapter.addMessage(ChatMessage(text, false))
        binding.rvChat.smoothScrollToPosition(messageList.size - 1)
    }
}