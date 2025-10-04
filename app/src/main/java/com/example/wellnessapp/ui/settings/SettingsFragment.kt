package com.example.wellnessapp.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessapp.R
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var prefs: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = SharedPrefsManager(requireContext())

        binding.btnSetReminder.setOnClickListener {
            findNavController().navigate(com.example.wellnessapp.R.id.action_settingsFragment_to_reminderFragment)
        }
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
        }


        binding.btnClearData.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to erase all data? This cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    clearAllData()
                    Toast.makeText(requireContext(), "All data cleared!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return binding.root
    }

    private fun clearAllData() {
        requireContext().getSharedPreferences("wellness_prefs", 0).edit().clear().apply()
        prefs.saveHabits(emptyList())
        prefs.saveMoods(emptyList())
        prefs.saveReminders(emptyList())

        // Restart app
        val intent = requireActivity().packageManager
            .getLaunchIntentForPackage(requireActivity().packageName)

        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            requireActivity().finish()
        }
    }

}
