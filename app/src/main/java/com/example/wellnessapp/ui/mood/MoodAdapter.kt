package com.example.wellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.data.Mood
import com.example.wellnessapp.databinding.ItemMoodBinding

class MoodAdapter(
    private var moods: MutableList<Mood>,
    private val onEdit: (Mood, Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(val binding: ItemMoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.binding.textEmoji.text = mood.emoji
        holder.binding.textDate.text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
            .format(mood.date)
        holder.binding.textNote.text = mood.note

        holder.binding.btnEdit.setOnClickListener { onEdit(mood, position) }
        holder.binding.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount() = moods.size

    fun setMoods(newMoods: MutableList<Mood>) {
        moods = newMoods
        notifyDataSetChanged()
    }
}
