package com.apps.dogoes

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.Announcement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnnouncementsListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLoadAnnouncements: Button
    private lateinit var adapter: AnnouncementsAdapter
    private val announcements = mutableListOf<Announcement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements_list)

        recyclerView = findViewById(R.id.recyclerViewAnnouncements)
        btnLoadAnnouncements = findViewById(R.id.btnLoadAnnouncements)

        adapter = AnnouncementsAdapter(announcements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnLoadAnnouncements.setOnClickListener {
            loadAnnouncements()
        }
    }

    private fun loadAnnouncements() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getUserAnnouncements(userId).enqueue(object : Callback<List<Announcement>> {
            override fun onResponse(
                call: Call<List<Announcement>>,
                response: Response<List<Announcement>>
            ) {
                if (response.isSuccessful) {
                    announcements.clear()
                    response.body()?.let { announcements.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("Announcements", "Failed to fetch: ${response.errorBody()?.string()}")
                    Toast.makeText(this@AnnouncementsListActivity, "Failed to load announcements", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Announcement>>, t: Throwable) {
                Log.e("Announcements", "Error: ${t.message}")
                Toast.makeText(this@AnnouncementsListActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}