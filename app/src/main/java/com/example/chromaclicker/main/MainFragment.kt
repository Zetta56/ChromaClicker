package com.example.chromaclicker.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.chromaclicker.overlay.OverlayService
import com.example.chromaclicker.R
import com.example.chromaclicker.databinding.FragmentMainBinding
import com.example.chromaclicker.getDefaultPreferences
import com.example.chromaclicker.models.Save
import com.google.gson.Gson
import java.io.File

/** Manages this app's home screen */
class MainFragment : ActionBarFragment("Chroma Clicker", false) {
    private lateinit var mainActivity: MainActivity
    private lateinit var navController: NavController
    private lateinit var binding: FragmentMainBinding
    private var selectedSave: Save? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadSave()
        mainActivity = activity as MainActivity
        binding = FragmentMainBinding.inflate(LayoutInflater.from(mainActivity))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        displaySaveName()
        addClickListeners()
        // Initialize power button color
        if(OverlayService.isRunning()) {
            binding.powerButton.setColorFilter(context!!.getColor(R.color.blue))
        }
    }

    /** Sets each button's click listeners */
    private fun addClickListeners() {
        // Toggle the overlay and update power button's color
        binding.powerButton.setOnClickListener {
            // Check if OverlayService is running at the beginning of this function call
            val isRunning = OverlayService.isRunning()
            val successful = mainActivity.toggleOverlay(!isRunning, selectedSave)
            // Make power button light blue if overlay is on and gray if off
            if(successful && !isRunning) {
                binding.powerButton.setColorFilter(context!!.getColor(R.color.blue))
            } else {
                binding.powerButton.colorFilter = null
            }
        }
        // Navigate to save fragment with the currently selected save
        binding.savesButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("selected", selectedSave?.name)
            navController.navigate(R.id.action_mainFragment_to_savesFragment, bundle)
        }
        // Navigate to the settings fragment
        binding.settingsButton.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    /** Formats and displays the currently selected save's name to the UI */
    private fun displaySaveName() {
        selectedSave?.let { selectedSave ->
            // Add ellipsis if selected is too long
            val ellipsis = if(selectedSave.name.length > 12) "..." else ""
            // Show the save name's first 12 characters and optional ellipsis
            binding.selectedSave.text = String.format("Selected: %s%s", selectedSave.name.take(12), ellipsis)
        } ?: run {
            // Show default save name if none are currently selected
            binding.selectedSave.setText(R.string.default_selected_save)
        }
    }

    /**
     * This populates the currently selected [save][selectedSave] using its name from this app's
     * shared preferences
     */
    private fun loadSave() {
        val preferences = getDefaultPreferences(activity as Context)
        val saveName = preferences.getString("saveName", "")
        // Attempt to get save data in internal storage
        try {
            val file = File("${context?.filesDir}/saves/${saveName}")
            // Parse JSON file into a Save object
            selectedSave = Gson().fromJson(file.readText(), Save::class.java)
        // Set selected save to null if save data couldn't be found
        } catch(e: Exception) {
            selectedSave = null
            // Make selected save empty in shared preferences
            preferences.edit().putString("saveName", "").apply()
        }
    }
}