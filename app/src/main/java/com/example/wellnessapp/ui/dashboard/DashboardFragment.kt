package com.example.wellnessapp.ui.dashboard

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wellnessapp.R
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.ui.habits.HabitViewModel
import com.example.wellnessapp.ui.mood.MoodViewModel
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var lastMoodText: TextView
    private lateinit var nextReminderText: TextView
    private lateinit var hydrationStatusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    private val habitViewModel: HabitViewModel by activityViewModels()
    private val moodViewModel: MoodViewModel by activityViewModels()

    private lateinit var prefs: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        lastMoodText = view.findViewById(R.id.lastMoodText)
        nextReminderText = view.findViewById(R.id.nextReminderText)
        hydrationStatusText = view.findViewById(R.id.hydrationStatusText)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)

        prefs = SharedPrefsManager(requireContext())

        // Observe habits LiveData
        habitViewModel.habits.observe(viewLifecycleOwner) {
            updateProgress()
        }

        // Observe moods LiveData - FIXED: Now properly updates when moods change
        moodViewModel.moods.observe(viewLifecycleOwner) {
            updateLastMood()
        }

        // Listen for reminder & hydration changes
        prefs.prefs.registerOnSharedPreferenceChangeListener(prefsListener)

        return view
    }

    private val prefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in listOf("reminders", "hydration_status", "hydration_interval")) {
                loadReminderData()
            }
        }

    private fun updateProgress() {
        val progress = habitViewModel.getProgress()
        progressBar.progress = progress.toInt()
        progressText.text = "${progress.toInt()}%"
    }

    // FIXED: Separated mood update logic
    private fun updateLastMood() {
        val moods = moodViewModel.moods.value
        if (!moods.isNullOrEmpty()) {
            val lastMood = moods.first() // moods are added at position 0, so first is latest
            val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val formattedTime = timeFormat.format(Date(lastMood.date))

            lastMoodText.text = if (lastMood.note.isNotEmpty()) {
                "${lastMood.emoji} ${lastMood.note}"
            } else {
                lastMood.emoji
            }
        } else {
            lastMoodText.text = "None"
        }
    }

    private fun loadReminderData() {
        // Next Reminder
        val reminders = prefs.loadReminders().filter { it.isActive }
        if (reminders.isNotEmpty()) {
            val next = reminders.minByOrNull { it.timeMillis }
            if (next != null) {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date(next.timeMillis))
                nextReminderText.text = "${next.title} at $time"
            } else {
                nextReminderText.text = "None"
            }
        } else {
            nextReminderText.text = "None"
        }

        // Hydration Reminder Status
        val hydrationStatus = prefs.loadHydrationStatus()
        val hydrationInterval = prefs.loadHydrationInterval()
        hydrationStatusText.text = if (hydrationStatus) {
            "Every $hydrationInterval min"
        } else {
            "Inactive"
        }
    }

    override fun onResume() {
        super.onResume()
        updateLastMood()
        loadReminderData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prefs.prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
}