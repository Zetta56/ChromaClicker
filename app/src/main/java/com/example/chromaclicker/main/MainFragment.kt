package com.example.chromaclicker.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
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

class MainFragment : ActionBarFragment("Chroma Clicker", false) {
    private lateinit var mainActivity: MainActivity
    private lateinit var navController: NavController
    private lateinit var binding: FragmentMainBinding
    private lateinit var overlayIntent: Intent
    private var selectedSave: Save? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = activity as MainActivity
        loadSave()
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(LayoutInflater.from(mainActivity))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        // :: operator gets OverlayService's metadata
        overlayIntent = Intent(mainActivity.applicationContext, OverlayService::class.java)

        // Configure UI
        if(OverlayService.isRunning()) {
            binding.powerButton.setColorFilter(Color.parseColor("#2DADF4"))
        }
        if(selectedSave != null) {
            val ellipsis = if(selectedSave!!.name.length > 12) "..." else ""
            binding.selectedSave.text = String.format("Selected: %s%s", selectedSave!!.name.take(12), ellipsis)
        } else {
            binding.selectedSave.setText(R.string.default_selected_save)
        }

        // Add click listeners
        binding.powerButton.setOnClickListener { toggleOverlay() }
        binding.savesButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("selected", selectedSave?.name)
            navController.navigate(R.id.action_mainFragment_to_savesFragment, bundle)
        }
        binding.settingsButton.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    private fun loadSave() {
        val preferences = getDefaultPreferences(activity as Context)
        val saveName = preferences.getString("saveName", "")
        try {
            val file = File("${context?.filesDir}/saves/${saveName}")
            selectedSave = Gson().fromJson(file.readText(), Save::class.java)
        } catch(e: Exception) {
            selectedSave = null
            preferences.edit().putString("saveName", "").apply()
        }
    }

    private fun toggleOverlay() {
        // Check if OverlayService is running at the beginning of this function
        val isRunning = OverlayService.isRunning()
        val successful = mainActivity.toggleOverlay(!isRunning, selectedSave)
        if(successful && !isRunning) {
            binding.powerButton.setColorFilter(Color.parseColor("#2DADF4"))
        } else {
            binding.powerButton.colorFilter = null
        }
    }
}