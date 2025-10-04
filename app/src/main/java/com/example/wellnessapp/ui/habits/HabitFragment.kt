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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.SharedPrefsManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var fabAddHabit: FloatingActionButton

    private val habitViewModel: HabitViewModel by activityViewModels()
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
}
