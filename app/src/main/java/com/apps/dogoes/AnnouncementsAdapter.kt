package com.apps.dogoes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apps.dogoes.api.Announcement

class AnnouncementsAdapter(private val announcements: List<Announcement>) :
    RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.bind(announcement)
    }

    override fun getItemCount(): Int = announcements.size

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)

        fun bind(announcement: Announcement) {
            tvTitle.text = announcement.title
            tvContent.text = announcement.content
        }
    }
}