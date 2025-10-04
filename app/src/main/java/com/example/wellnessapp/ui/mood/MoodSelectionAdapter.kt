package com.example.wellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.databinding.ItemMoodSelectionBinding

class MoodSelectionAdapter(
    private val moods: List<MoodOption>,
    private val onSelect: (MoodOption) -> Unit
) : RecyclerView.Adapter<MoodSelectionAdapter.MoodSelectionViewHolder>() {

    inner class MoodSelectionViewHolder(val binding: ItemMoodSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mood: MoodOption) {
            binding.textEmoji.text = mood.emoji
            binding.textLabel.text = mood.label
            binding.root.setOnClickListener { onSelect(mood) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodSelectionViewHolder {
        val binding = ItemMoodSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MoodSelectionViewHolder(binding)
    }

    override fun getItemCount() = moods.size

    override fun onBindViewHolder(holder: MoodSelectionViewHolder, position: Int) {
        holder.bind(moods[position])
    }
}
