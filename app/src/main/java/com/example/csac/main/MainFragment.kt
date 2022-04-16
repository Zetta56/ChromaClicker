package com.example.csac.main

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.csac.overlay.OverlayService
import com.example.csac.R
import com.example.csac.autoclick.AutoClickService
import com.example.csac.databinding.FragmentMainBinding
import com.example.csac.models.Clicker
import com.example.csac.models.Save
import kotlinx.serialization.json.Json
import java.io.File

class MainFragment : Fragment() {
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
        mainActivity.supportActionBar?.title = "CSAC"
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
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
        val powerImage = if(OverlayService.isRunning()) R.drawable.power_on else R.drawable.power_off
        binding.powerButton.setImageResource(powerImage)
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
        binding.settingsButton.setOnClickListener { navController.navigate(R.id.action_mainFragment_to_settingsFragment) }
    }

    private fun loadSave() {
        val preferences = mainActivity.getPreferences(Context.MODE_PRIVATE)
        val saveName = preferences.getString("saveName", "")
        try {
            val file = File("${context?.filesDir}/saves/${saveName}")
            selectedSave = Json.decodeFromString(Save.serializer(), file.readText())
        } catch(e: Exception) {
            selectedSave = null
            preferences.edit().putString("saveName", "").apply()
        }
    }

    private fun toggleOverlay() {
        if(!hasPermissions()) {
            return
        }
        if (!OverlayService.isRunning()) {
            binding.powerButton.setImageResource(R.drawable.power_on)
            val clickers = selectedSave?.clickers?.map { c -> Clicker(c) } ?: arrayListOf()
            overlayIntent.putParcelableArrayListExtra("clickers", ArrayList(clickers))
            if (Build.VERSION.SDK_INT >= 26) {
                mainActivity.startForegroundService(overlayIntent)
            } else {
                mainActivity.startService(overlayIntent)
            }
        } else {
            binding.powerButton.setImageResource(R.drawable.power_off)
            mainActivity.stopService(overlayIntent)
        }
    }

    private fun hasPermissions(): Boolean {
        if(!Settings.canDrawOverlays(mainActivity)) {
            // Redirect to overlay permission screen for this app
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${mainActivity.packageName}")
            )
            mainActivity.applicationContext.startActivity(intent)
            return false
        }
        if(Settings.Secure.getInt(mainActivity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 0) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            mainActivity.applicationContext.startActivity(intent)
            return false
        }
        if(AutoClickService.instance?.projection == null) {
            val projectionManager = mainActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mainActivity.projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
            return false
        }
        return true
    }
}