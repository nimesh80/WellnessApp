package com.example.wellnessapp.data

data class Reminder(
    val id: Int,
    var title: String,
    var timeMillis: Long,   // exact timestamp
    var isActive: Boolean = true
)
