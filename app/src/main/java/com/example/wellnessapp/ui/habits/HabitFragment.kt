package com.example.wellnessapp.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.data.Mood
import com.example.wellnessapp.data.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class HabitFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var fabAddHabit: FloatingActionButton

    // Dashboard UI
    private lateinit var lastMoodText: TextView
    private lateinit var nextReminderText: TextView
    private lateinit var hydrationStatusText: TextView
    private lateinit var hydrationIntervalText: TextView

    private val habitViewModel: HabitViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var prefs: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_habit, container, false)

        recyclerView = view.findViewById(R.id.recyclerHabits)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)

        lastMoodText = view.findViewById(R.id.lastMoodText)
        nextReminderText = view.findViewById(R.id.nextReminderText)
        hydrationStatusText = view.findViewById(R.id.hydrationStatusText)

        prefs = SharedPrefsManager(requireContext())

        habitAdapter = HabitAdapter(
            mutableListOf(),
            onHabitChecked = { habit, position, isChecked ->
                habitViewModel.toggleHabit(position, isChecked)
                updateProgress()
            },
            onEditHabit = { habit, position ->
                habitViewModel.updateHabit(position, habit.title)
                updateProgress()
            },
            onDeleteHabit = { position ->
                habitViewModel.deleteHabit(position)
                updateProgress()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = habitAdapter

        habitViewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.setHabits(habits)
            updateProgress()
        }

        fabAddHabit.setOnClickListener { showAddHabitDialog() }

        loadDashboardData()

        return view
    }

    private fun showAddHabitDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Add Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val habitTitle = input.text.toString().trim()
                if (habitTitle.isNotEmpty()) {
                    habitViewModel.addHabit(habitTitle)
                    updateProgress()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProgress() {
        val progress = habitViewModel.getProgress()
        progressBar.progress = progress.toInt()
        progressText.text = "Progress: ${progress.toInt()}%"
    }

    private fun loadDashboardData() {
        // Last Mood
        val moods = prefs.loadMoods()
        if (moods.isNotEmpty()) {
            val lastMood = moods.last()
            val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastMood.date))
            lastMoodText.text = "Last Mood: ${lastMood.emoji} ${lastMood.note} ($date)"
        } else {
            lastMoodText.text = "Last Mood: None"
        }

        // Next Reminder
        val reminders = prefs.loadReminders().filter { it.isActive }
        if (reminders.isNotEmpty()) {
            val next = reminders.minByOrNull { it.timeMillis }
            if (next != null) {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(next.timeMillis))
                nextReminderText.text = "Next Reminder: ${next.title} at $time"
            } else {
                nextReminderText.text = "Next Reminder: None"
            }
        } else {
            nextReminderText.text = "Next Reminder: None"
        }

        // Hydration Reminder Status
        val hydrationStatus = prefs.loadHydrationStatus()
        val hydrationInterval = prefs.loadHydrationInterval()
        hydrationStatusText.text = if (hydrationStatus) {
            "Active (Every $hydrationInterval min)"
        } else {
            "Inactive"
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData() // Refresh dashboard when returning
    }
}
