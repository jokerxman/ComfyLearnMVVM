package com.hompimpa.comfylearn.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.learnProg.ProgressionItem

class ProgressionAdapter(
    private val items: List<ProgressionItem>,
    private val onItemClick: (ProgressionItem) -> Unit
) : RecyclerView.Adapter<ProgressionAdapter.ProgressionViewHolder>() {

    class ProgressionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.activityNameTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val itemLayout: View = itemView.findViewById(R.id.progression_item_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progression, parent, false)
        return ProgressionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressionViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.activityName
        holder.statusTextView.text = item.status
        holder.itemLayout.setOnSoundClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}