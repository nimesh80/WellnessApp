package com.example.wellnessapp.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.Mood
import com.example.wellnessapp.databinding.FragmentMoodBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration


data class MoodOption(val emoji: String, val label: String)

class MoodFragment : Fragment() {

    private lateinit var binding: FragmentMoodBinding
    private val moodViewModel: MoodViewModel by activityViewModels()
    private lateinit var adapter: MoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoodBinding.inflate(inflater, container, false)

        adapter = MoodAdapter(
            mutableListOf(),
            onEdit = { mood, position -> showEditMoodDialog(mood, position) },
            onDelete = { position -> moodViewModel.deleteMood(position) }
        )

        binding.recyclerMoods.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMoods.adapter = adapter

        moodViewModel.moods.observe(viewLifecycleOwner) { moods ->
            adapter.setMoods(moods)
        }

        binding.fabAddMood.setOnClickListener { showAddMoodDialog() }

        binding.btnCalendarView.setOnClickListener {
            findNavController().navigate(R.id.moodCalendarFragment)
        }
        binding.btnChartView.setOnClickListener {
            findNavController().navigate(R.id.moodChartFragment)
        }

        return binding.root
    }
    private fun showAddMoodDialog() {
        val moodOptions = listOf(
            MoodOption("😀", "Happy"),
            MoodOption("😐", "Neutral"),
            MoodOption("😔", "Sad"),
            MoodOption("😡", "Angry"),
            MoodOption("🥳", "Excited"),
            MoodOption("😴", "Sleepy")
        )

        val dialogBinding = layoutInflater.inflate(R.layout.dialog_mood_selection, null)
        val recyclerView = dialogBinding.findViewById<RecyclerView>(R.id.recyclerMoodSelection)
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // list view

        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )


        val adapter = MoodSelectionAdapter(moodOptions) { selectedMood ->
            showAddNoteDialog(selectedMood)
        }
        recyclerView.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding)
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showAddNoteDialog(selectedMood: MoodOption) {
        val input = EditText(requireContext()).apply {
            hint = "Add a note (optional)"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Note for ${selectedMood.label}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                moodViewModel.addMood(selectedMood.emoji, input.text.toString())
            }
            .setNegativeButton("Skip") { _, _ ->
                moodViewModel.addMood(selectedMood.emoji, "")
            }
            .show()
    }

    private fun showEditMoodDialog(mood: Mood, position: Int) {
        val emojis = arrayOf("😀", "😐", "😔", "😡", "🥳", "😴")
        val input = EditText(requireContext()).apply {
            setText(mood.note)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Mood")
            .setItems(emojis) { _, which ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Edit Note")
                    .setView(input)
                    .setPositiveButton("Update") { _, _ ->
                        moodViewModel.updateMood(position, emojis[which], input.text.toString())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }
}
