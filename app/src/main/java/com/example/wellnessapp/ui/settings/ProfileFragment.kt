package com.example.wellnessapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.data.UserProfile
import com.example.wellnessapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var prefs: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        prefs = SharedPrefsManager(requireContext())

        val goals = listOf("Improve Health", "Manage Stress", "Increase Productivity", "Other")
        binding.goalSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, goals)

        // Load saved profile
        val profile = prefs.loadUserProfile()
        profile?.let {
            binding.etName.setText(it.name)
            binding.etEmail.setText(it.email)
            binding.etGender.setText(it.gender)
            if (it.age != 0) binding.etAge.setText(it.age.toString())

            // Set goal spinner selection
            val index = goals.indexOf(it.goal)
            if (index >= 0) {
                binding.goalSpinner.setSelection(index)
            } else {
                binding.goalSpinner.setSelection(goals.size - 1) // "Other"
                binding.etCustomGoal.visibility = View.VISIBLE
                binding.etCustomGoal.setText(it.goal)
            }
        }

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


        // Save changes
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

            val updatedProfile = UserProfile(name, email, gender, age, goal)
            prefs.saveUserProfile(updatedProfile)

            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
}
