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

    // ========== CRUD Operations ==========

    fun addMood(emoji: String, note: String) {
        val mood = Mood(
            id = (_moods.value?.size ?: 0) + 1,
            emoji = emoji,
            note = note,
            date = Date().time
        )
        _moods.value?.add(0, mood)
        saveMoods()
        _moods.value = _moods.value // Trigger LiveData update
        updateAggregates()
    }

    fun updateMood(position: Int, emoji: String, note: String) {
        _moods.value?.get(position)?.apply {
            this.emoji = emoji
            this.note = note
        }
        saveMoods()
        _moods.value = _moods.value // Trigger LiveData update
        updateAggregates()
    }

    fun deleteMood(position: Int) {
        _moods.value?.removeAt(position)
        saveMoods()
        _moods.value = _moods.value // Trigger LiveData update
        updateAggregates()
    }

    private fun saveMoods() {
        _moods.value?.let { prefs.saveMoods(it) }
    }

    // ========== Chart Aggregation Logic ==========

    private fun updateAggregates() {
        val all = _moods.value ?: mutableListOf()
        updateTodaySlots(all)
        updateWeekly(all)
    }

    /**
     * Map emoji to numeric score (1-5 scale)
     */
    private fun emojiToScore(emoji: String): Int = when (emoji.trim()) {
        "😀", "😁", "😃", "😄", "🥳" -> 5  // Very Happy
        "🙂", "😊" -> 4                      // Happy
        "😐", "🤔" -> 3                      // Neutral
        "😔", "😞", "😴" -> 2                // Sad/Tired
        "😡", "😠" -> 1                      // Angry
        else -> 3                            // Default neutral
    }

    /**
     * Check if two timestamps are on the same day
     */
    private fun isSameDay(ms1: Long, ms2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = ms1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = ms2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Get time slot label (Morning/Afternoon/Evening/Night) for a timestamp
     */
    private fun getSlotLabel(ms: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ms }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Morning"      // 5 AM - 11 AM
            in 12..16 -> "Afternoon"   // 12 PM - 4 PM
            in 17..20 -> "Evening"     // 5 PM - 8 PM
            else -> "Night"            // 9 PM - 4 AM
        }
    }

    /**
     * Update today's mood chart data (Bar Chart)
     * FIXED: Only shows moods from TODAY, resets daily
     */
    private fun updateTodaySlots(all: List<Mood>) {
        // Get start of today (00:00:00)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Get end of today (23:59:59.999)
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val slots = listOf("Morning", "Afternoon", "Evening", "Night")
        val scoresBySlot = mutableMapOf<String, MutableList<Int>>().apply {
            slots.forEach { put(it, mutableListOf()) }
        }

        // Filter moods to ONLY include TODAY
        all.forEach { mood ->
            if (mood.date in todayStart..todayEnd) {
                val slot = getSlotLabel(mood.date)
                scoresBySlot[slot]?.add(emojiToScore(mood.emoji))
            }
        }

        // Calculate average score for each slot
        val averages = slots.map { slot ->
            val scores = scoresBySlot[slot]!!
            if (scores.isEmpty()) {
                0f // No moods in this slot
            } else {
                scores.average().toFloat().roundToInt().toFloat()
            }
        }

        _todaySlotScores.value = averages
    }

    /**
     * Update weekly mood trend data (Line Chart)
     * Shows last 7 days including today
     */
    private fun updateWeekly(all: List<Mood>) {
        val labels = mutableListOf<String>()
        val values = mutableListOf<Float>()
        val labelFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Mon, Tue, Wed...
        val now = Calendar.getInstance()

        // Loop through last 7 days (6 days ago to today)
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }

            // Get start of that day
            val dayStart = Calendar.getInstance().apply {
                set(dayCal.get(Calendar.YEAR),
                    dayCal.get(Calendar.MONTH),
                    dayCal.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Add day label (Mon, Tue, etc.)
            val dayLabel = labelFormat.format(Date(dayStart))
            labels.add(dayLabel)

            // Collect all moods for this specific day
            val dayMoods = all.filter { isSameDay(it.date, dayStart) }
                .map { emojiToScore(it.emoji) }

            // Calculate average mood score for the day
            val avgScore = if (dayMoods.isEmpty()) {
                0f // No moods on this day
            } else {
                dayMoods.average().toFloat().roundToInt().toFloat()
            }

            values.add(avgScore)
        }

        _weeklyLabels.value = labels
        _weeklyValues.value = values
    }
}