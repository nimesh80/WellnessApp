package com.example.wellnessapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveHabits(habits: List<Habit>) {
        prefs.edit().putString("habits", gson.toJson(habits)).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString("habits", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveMoods(moods: List<Mood>) {
        prefs.edit().putString("moods", gson.toJson(moods)).apply()
    }

    fun loadMoods(): MutableList<Mood> {
        val json = prefs.getString("moods", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Mood>>() {}.type
        return Gson().fromJson(json, type)
    }
    fun saveReminders(reminders: List<Reminder>) {
        prefs.edit().putString("reminders", gson.toJson(reminders)).apply()
    }

    fun loadReminders(): MutableList<Reminder> {
        val json = prefs.getString("reminders", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Reminder>>() {}.type
        return Gson().fromJson(json, type)
    }
    fun saveUserProfile(profile: UserProfile) {
        prefs.edit().putString("user_profile", gson.toJson(profile)).apply()
    }

    fun loadUserProfile(): UserProfile? {
        val json = prefs.getString("user_profile", null) ?: return null
        return gson.fromJson(json, UserProfile::class.java)
    }

    fun setUserOnboarded(onboarded: Boolean) {
        prefs.edit().putBoolean("user_onboarded", onboarded).apply()
    }

    fun isUserOnboarded(): Boolean {
        return prefs.getBoolean("user_onboarded", false)
    }
    fun saveHydrationInterval(minutes: Int) {
        prefs.edit().putInt("hydration_interval", minutes).apply()
    }

    fun loadHydrationInterval(): Int {
        return prefs.getInt("hydration_interval", 60) // default 60 mins
    }

    fun saveHydrationStatus(isActive: Boolean) {
        prefs.edit().putBoolean("hydration_status", isActive).apply()
    }

    fun loadHydrationStatus(): Boolean {
        return prefs.getBoolean("hydration_status", false)
    }




}
