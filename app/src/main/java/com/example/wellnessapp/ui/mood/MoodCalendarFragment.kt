package com.example.wellnessapp.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellnessapp.databinding.FragmentMoodCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.*
import com.example.wellnessapp.data.Mood

class MoodCalendarFragment : Fragment() {

    private lateinit var binding: FragmentMoodCalendarBinding
    private val moodViewModel: MoodViewModel by viewModels()
    private lateinit var adapter: MoodAdapter
    private var selectedDateMoods = mutableListOf<Mood>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoodCalendarBinding.inflate(inflater, container, false)

        adapter = MoodAdapter(
            mutableListOf(),
            onEdit = { _, _ -> }, // optional editing
            onDelete = { position -> moodViewModel.deleteMood(position) }
        )

        binding.recyclerMoodHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMoodHistory.adapter = adapter

        moodViewModel.moods.observe(viewLifecycleOwner) { moods ->
            filterMoodsForDate(CalendarDay.today())
        }

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            filterMoodsForDate(date)
        }

        return binding.root
    }

    private fun filterMoodsForDate(date: CalendarDay) {
        moodViewModel.moods.value?.let { allMoods ->
            selectedDateMoods = allMoods.filter {
                isSameDay(it.date, date.date.time)
            }.toMutableList()

            adapter.setMoods(selectedDateMoods)
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(time1)) == fmt.format(Date(time2))
    }
}
