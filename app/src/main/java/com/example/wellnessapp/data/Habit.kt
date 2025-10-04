package com.example.wellnessapp.data

data class Habit(
    var id: Long = System.currentTimeMillis(),
    var title: String,
    var isCompleted: Boolean = false,
    var date: String = ""
) {
    fun isFromToday(): Boolean {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        return date == today
    }
}
