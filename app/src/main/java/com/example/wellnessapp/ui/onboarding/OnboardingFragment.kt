package com.example.wellnessapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessapp.R
import com.example.wellnessapp.data.SharedPrefsManager
import com.example.wellnessapp.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)

        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(com.example.wellnessapp.R.id.action_onboardingFragment_to_profileFormFragment)
        }

        binding.btnAlreadyHaveAccount.setOnClickListener {
            val prefs = SharedPrefsManager(requireContext())   // create instance
            val profile = prefs.loadUserProfile()              // call instance method

            if (profile != null && profile.name.isNotEmpty() && profile.email.isNotEmpty()) {
                // Profile exists → Go to Habit page
                findNavController().navigate(R.id.action_onboardingFragment_to_habitFragment)
            } else {
                Toast.makeText(requireContext(), "Please set up your profile first!", Toast.LENGTH_SHORT).show()
            }
        }



        return binding.root
    }
}
