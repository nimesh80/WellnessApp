package com.example.wellnessapp.ui.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.data.Reminder
import com.example.wellnessapp.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(
    private val reminders: List<Reminder>,
    private val onDelete: (Reminder) -> Unit,
    private val onEdit: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = reminders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        holder.binding.tvTitle.text = reminder.title
        holder.binding.tvTime.text = fmt.format(Date(reminder.timeMillis))

        holder.binding.btnDelete.setOnClickListener {
            onDelete(reminder)
        }

        holder.binding.root.setOnClickListener {
            onEdit(reminder)
        }
    }
}
