package com.example.csac.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
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
        loadSave(arguments)
        println("creating")

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
        val powerImage = if(mainActivity.overlayVisible) R.drawable.power_on else R.drawable.power_off
        binding.powerButton.setImageResource(powerImage)
        if(selectedSave != null) {
            val ellipsis = if(selectedSave!!.name.length > 12) "..." else ""
            binding.selectedSave.text = String.format("Selected: %s%s", selectedSave!!.name.take(12), ellipsis)
        } else {
            binding.selectedSave.setText(R.string.default_selected_save)
        }

        // Add click listeners
        binding.powerButton.setOnClickListener { toggleOverlay() }
        binding.savesButton.setOnClickListener { navController.navigate(R.id.action_mainFragment_to_savesFragment) }
        binding.settingsButton.setOnClickListener { navController.navigate(R.id.action_mainFragment_to_settingsFragment) }
    }

    private fun loadSave(bundle: Bundle?) {
        val preferences = mainActivity.getPreferences(Context.MODE_PRIVATE)
        val saveName = if(bundle != null) {
            bundle.getString("saveName")!!
        } else {
            preferences.getString("saveName", null)
        }

        if(saveName != null) {
            val file = File("${context?.filesDir}/saves/${saveName}")
            with(preferences.edit()) {
                if(file.exists()) {
                    selectedSave = Json.decodeFromString(Save.serializer(), file.readText())
                    putString("saveName", saveName)
                } else {
                    selectedSave = null
                    putString("saveName", null)
                }
                apply()
            }
        }
    }

    private fun toggleOverlay() {
        if(!hasPermissions()) {
            return
        }

        // Setup intent extras
        val clickers = selectedSave?.clickers?.map { c -> Clicker(c) } ?: arrayListOf()
        val windowRect = Rect()
        mainActivity.window.decorView.getWindowVisibleDisplayFrame(windowRect)
        mainActivity.overlayVisible = !mainActivity.overlayVisible

        if (mainActivity.overlayVisible) {
            binding.powerButton.setImageResource(R.drawable.power_on)
            overlayIntent.putParcelableArrayListExtra("clickers", ArrayList(clickers))
            overlayIntent.putExtra("statusBarHeight", windowRect.top)
            if (Build.VERSION.SDK_INT >= 26) {
                mainActivity.applicationContext.startForegroundService(overlayIntent)
            } else {
                mainActivity.applicationContext.startService(overlayIntent)
            }
        } else {
            binding.powerButton.setImageResource(R.drawable.power_off)
            mainActivity.applicationContext.stopService(overlayIntent)
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