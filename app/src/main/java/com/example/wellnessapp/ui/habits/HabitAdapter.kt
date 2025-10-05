package com.example.wellnessapp.ui.habits

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.Habit
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val onHabitChecked: (Habit, Int, Boolean) -> Unit,
    private val onEditHabit: (Habit, Int) -> Unit,
    private val onDeleteHabit: (Int) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitCheckBox: CheckBox = itemView.findViewById(R.id.habitCheckBox)
        val habitTitle: TextView = itemView.findViewById(R.id.habitTitle)
        val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.habitTitle.text = habit.title
        holder.habitCheckBox.isChecked = habit.isCompleted

        holder.habitCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onHabitChecked(habit, position, isChecked)
        }

        holder.menuButton.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.menuButton)
            popup.menuInflater.inflate(R.menu.habit_item_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editHabit -> {
                        showEditDialog(holder, habit, position)
                        true
                    }
                    R.id.deleteHabit -> {
                        showDeleteConfirmDialog(holder, position)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = habits.size

    fun setHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    // FIXED: Edit dialog with white background and black text
    private fun showEditDialog(holder: HabitViewHolder, habit: Habit, position: Int) {
        val context = holder.itemView.context

        // Create EditText with proper styling
        val input = EditText(context).apply {
            setText(habit.title)
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(50, 30, 50, 30)
        }

        // Create container with white background
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(60, 40, 60, 20)
            addView(input)
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Habit")
            .setView(container)
            .setPositiveButton("Save") { dialog, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    habit.title = newTitle
                    habit.date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    onEditHabit(habit, position)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply {
                // Set dialog background to white
                window?.setBackgroundDrawableResource(android.R.color.white)
                show()

                // Set button text colors to be visible
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.parseColor("#4CAF50"))
                getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.parseColor("#757575"))
            }
    }

    // FIXED: Delete confirmation dialog with white background
    private fun showDeleteConfirmDialog(holder: HabitViewHolder, position: Int) {
        val context = holder.itemView.context

        AlertDialog.Builder(context)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Delete") { dialog, _ ->
                onDeleteHabit(position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply {
                // Set dialog background to white
                window?.setBackgroundDrawableResource(android.R.color.white)
                show()

                // Set button text colors
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.parseColor("#F44336"))
                getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.parseColor("#757575"))
            }
    }
}