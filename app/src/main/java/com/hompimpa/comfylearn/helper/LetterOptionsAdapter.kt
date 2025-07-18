package com.hompimpa.comfylearn.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R

class LetterOptionsAdapter(
    private val letters: List<String>,
    private val onLetterClick: (String) -> Unit
) : RecyclerView.Adapter<LetterOptionsAdapter.ViewHolder>() {

    private var itemSize: Int = 0

    fun updateItemSize(size: Int) {
        if (itemSize != size) {
            itemSize = size
            notifyItemRangeChanged(0, itemCount)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val letterTextView: TextView = itemView.findViewById(R.id.letterTextView)

        fun bind(letter: String) {
            if (itemSize > 0) {
                val params = itemView.layoutParams
                params.width = itemSize
                params.height = itemSize
                itemView.layoutParams = params
            }

            letterTextView.text = letter
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                letterTextView,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )

            itemView.setOnSoundClickListener {
                onLetterClick(letter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_letter_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(letters[position])
    }

    override fun getItemCount(): Int = letters.size
}