package com.example.wellnessapp.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wellnessapp.data.Mood
import com.example.wellnessapp.data.SharedPrefsManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MoodViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SharedPrefsManager(application.applicationContext)

    private val _moods = MutableLiveData<MutableList<Mood>>(prefs.loadMoods())
    val moods: LiveData<MutableList<Mood>> get() = _moods

    // Aggregates exposed for chart
    private val _todaySlotScores = MutableLiveData<List<Float>>(listOf(0f, 0f, 0f, 0f))
    val todaySlotScores: LiveData<List<Float>> = _todaySlotScores

    private val _weeklyLabels = MutableLiveData<List<String>>(emptyList())
    val weeklyLabels: LiveData<List<String>> = _weeklyLabels

    private val _weeklyValues = MutableLiveData<List<Float>>(emptyList())
    val weeklyValues: LiveData<List<Float>> = _weeklyValues

    init {
        updateAggregates()
    }

    // ---- CRUD ops ----
    fun addMood(emoji: String, note: String) {
        val mood = Mood(
            id = (_moods.value?.size ?: 0) + 1,
            emoji = emoji,
            note = note,
            date = Date().time
        )
        _moods.value?.add(0, mood)
        saveMoods()
        _moods.value = _moods.value
        updateAggregates()
    }

    fun updateMood(position: Int, emoji: String, note: String) {
        _moods.value?.get(position)?.apply {
            this.emoji = emoji
            this.note = note
        }
        saveMoods()
        _moods.value = _moods.value
        updateAggregates()
    }

    fun deleteMood(position: Int) {
        _moods.value?.removeAt(position)
        saveMoods()
        _moods.value = _moods.value
        updateAggregates()
    }

    private fun saveMoods() {
        _moods.value?.let { prefs.saveMoods(it) }
    }

    // ---- Aggregation helpers ----

    private fun updateAggregates() {
        val all = _moods.value ?: mutableListOf()
        updateTodaySlots(all)
        updateWeekly(all)
    }

    // Map emoji → numeric score 1..5 (customize if you want)
    private fun emojiToScore(emoji: String): Int = when (emoji.trim()) {
        "😀", "😁", "😃", "😄", "🥳" -> 5
        "🙂", "😊" -> 4
        "😐", "🤔" -> 3
        "😔", "😞", "😴" -> 2
        "😡", "😠" -> 1
        else -> 3 // default neutral
    }

    private fun isSameDay(ms1: Long, ms2: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(ms1)) == fmt.format(Date(ms2))
    }

    private fun getSlotLabel(ms: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ms }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        return when (h) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
    }

    private fun updateTodaySlots(all: List<Mood>) {
        val today = Date().time
        val slots = listOf("Morning", "Afternoon", "Evening", "Night")
        val scoresBySlot = mutableMapOf<String, MutableList<Int>>().apply {
            slots.forEach { put(it, mutableListOf()) }
        }

        all.forEach { mood ->
            if (isSameDay(mood.date, today)) {
                scoresBySlot[getSlotLabel(mood.date)]?.add(emojiToScore(mood.emoji))
            }
        }

        val averages = slots.map { slot ->
            val list = scoresBySlot[slot]!!
            if (list.isEmpty()) 0f else list.average().toFloat().roundToInt().toFloat()
        }

        _todaySlotScores.value = averages
    }

    private fun updateWeekly(all: List<Mood>) {
        // Build list of last 7 days (oldest -> newest)
        val labels = mutableListOf<String>()
        val values = mutableListOf<Float>()
        val fmtLabel = SimpleDateFormat("EEE", Locale.getDefault()) // Mon, Tue...
        val now = Calendar.getInstance()

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayStart = Calendar.getInstance().apply {
                set(dayCal.get(Calendar.YEAR), dayCal.get(Calendar.MONTH), dayCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayLabel = fmtLabel.format(Date(dayStart))
            labels.add(dayLabel)

            // collect moods for that exact day
            val dayList = all.filter { isSameDay(it.date, dayStart) }.map { emojiToScore(it.emoji) }
            val avg = if (dayList.isEmpty()) 0f else dayList.average().toFloat().roundToInt().toFloat()
            values.add(avg)
        }

        _weeklyLabels.value = labels
        _weeklyValues.value = values
    }
}
