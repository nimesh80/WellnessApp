package com.example.wellnessapp.ui.reminder

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellnessapp.data.Reminder
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.databinding.FragmentReminderBinding
import com.example.wellnessapp.utils.AlarmScheduler
import java.util.*

class ReminderFragment : Fragment() {

    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPrefsManager
    private lateinit var scheduler: AlarmScheduler
    private lateinit var adapter: ReminderAdapter
    private var reminders = mutableListOf<Reminder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        prefs = SharedPrefsManager(requireContext())
        scheduler = AlarmScheduler(requireContext())

        loadHydrationInterval()

        reminders = prefs.loadReminders()
        sortReminders()
        setupReminderList()

        binding.btnAddReminder.setOnClickListener { askForTitle() }
        binding.btnSetHydrationInterval.setOnClickListener { showIntervalPicker() }

        return binding.root
    }

    private fun loadHydrationInterval() {
        val interval = prefs.loadHydrationInterval()
        binding.hydrationIntervalText.text = "Interval: $interval min"
    }

    private fun showIntervalPicker() {
        val editText = EditText(requireContext()).apply {
            hint = "Enter interval in minutes"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Set Hydration Interval")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val input = editText.text.toString()
                if (input.isNotEmpty()) {
                    val minutes = input.toIntOrNull()
                    if (minutes != null && minutes in 5..180) {
                        prefs.saveHydrationInterval(minutes)
                        binding.hydrationIntervalText.text = "Interval: $minutes min"
                        scheduleHydrationReminder(minutes)
                        Toast.makeText(requireContext(),
                            "Hydration reminder set every $minutes minutes", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(),
                            "Interval must be between 5 and 180 minutes", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleHydrationReminder(minutes: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutes)

        val hydrationReminder = Reminder(
            id = 9999,
            title = "Hydration Reminder",
            timeMillis = cal.timeInMillis
        )

        reminders.removeAll { it.id == 9999 }
        reminders.add(hydrationReminder)
        prefs.saveReminders(reminders)
        prefs.saveHydrationStatus(true)

        scheduler.schedule(hydrationReminder)
        adapter.notifyDataSetChanged()
    }

    private fun askForTitle() {
        val editText = EditText(requireContext()).apply {
            hint = "Enter reminder title"
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("New Reminder")
            .setMessage("Enter a title for your reminder:")
            .setView(editText)
            .setPositiveButton("Next") { _, _ ->
                val title = editText.text.toString().ifBlank { "Custom Reminder" }
                pickDateTime(title)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickDateTime(title: String) {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            TimePickerDialog(requireContext(), { _, h, min ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d, h, min, 0)

                val reminder = Reminder(
                    id = (reminders.size + 1),
                    title = title,
                    timeMillis = cal.timeInMillis
                )

                reminders.add(reminder)
                sortReminders()
                prefs.saveReminders(reminders)
                scheduler.schedule(reminder)
                adapter.notifyDataSetChanged()

                Toast.makeText(requireContext(), "Reminder set!", Toast.LENGTH_SHORT).show()

            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun editReminder(reminder: Reminder) {
        val editText = EditText(requireContext()).apply {
            setText(reminder.title)
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Reminder")
            .setMessage("Update reminder title:")
            .setView(editText)
            .setPositiveButton("Next") { _, _ ->
                val newTitle = editText.text.toString().ifBlank { "Custom Reminder" }
                pickDateTimeForEdit(reminder, newTitle)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickDateTimeForEdit(reminder: Reminder, newTitle: String) {
        val now = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, min ->
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d, h, min, 0)

                reminder.title = newTitle
                reminder.timeMillis = cal.timeInMillis

                prefs.saveReminders(reminders)
                scheduler.cancel(reminder)
                scheduler.schedule(reminder)

                sortReminders()
                adapter.notifyDataSetChanged()

                Toast.makeText(requireContext(), "Reminder updated!", Toast.LENGTH_SHORT).show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    private fun setupReminderList() {
        adapter = ReminderAdapter(reminders,
            onDelete = { reminder ->
                reminders.remove(reminder)
                prefs.saveReminders(reminders)
                scheduler.cancel(reminder)
                adapter.notifyDataSetChanged()
            },
            onEdit = { reminder ->
                editReminder(reminder)
            }
        )

        binding.recyclerReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReminders.adapter = adapter
    }

    private fun sortReminders() {
        reminders.sortBy { it.timeMillis }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}