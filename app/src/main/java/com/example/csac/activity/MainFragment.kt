package com.example.csac.activity

import android.content.Intent
import android.graphics.Rect
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
import com.example.csac.databinding.FragmentMainBinding
import com.example.csac.models.Clicker

class MainFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var navController: NavController
    private lateinit var binding: FragmentMainBinding
    private lateinit var overlayIntent: Intent
    private val clickers = arrayListOf<Clicker>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = activity as MainActivity
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(LayoutInflater.from(mainActivity))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        // ::class.java gets OverlayService's java class
        overlayIntent = Intent(mainActivity.applicationContext, OverlayService::class.java)

        // Set power button image
        val powerImage = if(mainActivity.overlayVisible) R.drawable.power_on else R.drawable.power_off
        binding.powerButton.setImageResource(powerImage)

        // Add click listeners
        binding.powerButton.setOnClickListener { toggleOverlay() }
        binding.savesButton.setOnClickListener { navigateSaves() }
        binding.settingsButton.setOnClickListener { navigateSettings() }
    }

    private fun toggleOverlay() {
        if(!hasPermissions()) {
            return
        }

        val windowRect = Rect()
        mainActivity.window.decorView.getWindowVisibleDisplayFrame(windowRect)
        mainActivity.overlayVisible = !mainActivity.overlayVisible
        if (mainActivity.overlayVisible) {
            binding.powerButton.setImageResource(R.drawable.power_on)
            overlayIntent.putParcelableArrayListExtra("clickers", clickers)
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

    private fun navigateSaves() {
        navController.navigate(R.id.action_mainFragment_to_savesFragment)
    }

    private fun navigateSettings() {
        navController.navigate(R.id.action_mainFragment_to_settingsFragment)
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
        return true
    }
}