package com.example.wellnessapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.data.UserProfile
import com.example.wellnessapp.databinding.FragmentProfileFormBinding

class ProfileFormFragment : Fragment() {

    private lateinit var binding: FragmentProfileFormBinding
    private lateinit var prefs: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileFormBinding.inflate(inflater, container, false)
        prefs = SharedPrefsManager(requireContext())

        val goals = listOf("Improve Health", "Manage Stress", "Increase Productivity", "Other")
        binding.goalSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, goals)

        // Show/hide custom goal input when "Other" selected
        binding.goalSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.etCustomGoal.visibility =
                    if (goals[position] == "Other") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // Do nothing
            }
        }

        // Save profile on button click
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val age = binding.etAge.text.toString().toIntOrNull() ?: 0
            var goal = binding.goalSpinner.selectedItem.toString()

            if (goal == "Other") {
                goal = binding.etCustomGoal.text.toString().trim()
            }

            // ✅ Validation
            if (name.isBlank() || email.isBlank() || gender.isBlank() || age <= 0 || goal.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (age !in 5..120) {
                Toast.makeText(requireContext(), "Enter a valid age (5–120)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profile = UserProfile(name, email, gender, age, goal)
            prefs.saveUserProfile(profile)
            prefs.setUserOnboarded(true)

            findNavController().navigate(com.example.wellnessapp.R.id.action_profileFormFragment_to_habitFragment)
        }

        return binding.root
    }
}
