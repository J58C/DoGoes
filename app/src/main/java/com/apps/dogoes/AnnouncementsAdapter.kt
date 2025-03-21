package com.apps.dogoes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.apps.dogoes.api.AnnouncementResponse

class AnnouncementsAdapter(private var announcements: MutableList<AnnouncementResponse>) :
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

    fun updateData(newAnnouncements: List<AnnouncementResponse>) {
        val diffCallback = AnnouncementDiffCallback(announcements, newAnnouncements)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        announcements.clear()
        announcements.addAll(newAnnouncements)
        diffResult.dispatchUpdatesTo(this)
    }

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)

        fun bind(announcement: AnnouncementResponse) {
            tvTitle.text = announcement.title
            tvContent.text = announcement.content
        }
    }

    class AnnouncementDiffCallback(
        private val oldList: List<AnnouncementResponse>,
        private val newList: List<AnnouncementResponse>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].announcement_id == newList[newItemPosition].announcement_id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}