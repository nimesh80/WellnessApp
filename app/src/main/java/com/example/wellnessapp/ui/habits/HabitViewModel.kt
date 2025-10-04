package com.example.wellnessapp.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wellnessapp.data.Habit
import com.example.wellnessapp.data.SharedPrefsManager
import java.text.SimpleDateFormat
import java.util.*

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SharedPrefsManager(application.applicationContext)

    private val _habits = MutableLiveData<MutableList<Habit>>(prefs.loadHabits())
    val habits: LiveData<MutableList<Habit>> get() = _habits

    fun addHabit(title: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _habits.value?.add(Habit(title = title, date = today))
        saveHabits()
        _habits.value = _habits.value
    }

    fun updateHabit(position: Int, newTitle: String) {
        _habits.value?.get(position)?.title = newTitle
        saveHabits()
        _habits.value = _habits.value
    }

    fun deleteHabit(position: Int) {
        _habits.value?.removeAt(position)
        saveHabits()
        _habits.value = _habits.value
    }

    fun toggleHabit(position: Int, isChecked: Boolean) {
        _habits.value?.get(position)?.isCompleted = isChecked
        saveHabits()
        _habits.value = _habits.value
    }

    fun getProgress(): Double {
        val currentHabits = _habits.value ?: return 0.0
        if (currentHabits.isEmpty()) return 0.0
        val completed = currentHabits.count { it.isCompleted }
        return (completed.toDouble() / currentHabits.size) * 100
    }

    private fun saveHabits() {
        _habits.value?.let { prefs.saveHabits(it) }
    }
}
