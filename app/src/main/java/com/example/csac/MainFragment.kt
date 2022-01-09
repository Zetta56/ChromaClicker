package com.example.csac

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.csac.databinding.FragmentMainBinding

class MainFragment : Fragment(), View.OnClickListener {
    private lateinit var mainActivity: MainActivity
    private lateinit var navController: NavController
    private lateinit var binding: FragmentMainBinding
    private lateinit var overlayIntent: Intent

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
        // ::class.java takes OverlayService's metadata and spits out its Java class
        overlayIntent = Intent(mainActivity.applicationContext, OverlayService::class.java)

        // Set power button image
        val powerImage = if(mainActivity.overlayVisible) R.drawable.power_on else R.drawable.power_off
        binding.powerButton.setImageResource(powerImage)

        // Add click listeners
        binding.powerButton.setOnClickListener(this)
        view.findViewById<Button>(R.id.savesButton).setOnClickListener(this)
        view.findViewById<Button>(R.id.settingsButton).setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id) {
            R.id.powerButton -> {
//                if(checkPermissions()) {
                    mainActivity.overlayVisible = !mainActivity.overlayVisible
                    if (mainActivity.overlayVisible) {
                        binding.powerButton.setImageResource(R.drawable.power_on)
                        if (Build.VERSION.SDK_INT >= 26) {
                            mainActivity.applicationContext.startForegroundService(overlayIntent)
                        } else {
                            mainActivity.applicationContext.startService(overlayIntent)
                        }
                    } else {
                        binding.powerButton.setImageResource(R.drawable.power_off)
                        mainActivity.applicationContext.stopService(overlayIntent)
                    }
//                }
            }
            R.id.savesButton -> navController.navigate(R.id.action_mainFragment_to_savesFragment)
            R.id.settingsButton -> navController.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    private fun checkPermissions(): Boolean {
        if(!Settings.canDrawOverlays(mainActivity)) {
            // Redirect to overlay permission screen for this app
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${mainActivity.packageName}")
            )
            mainActivity.applicationContext.startActivity(intent)
            return false
        } else if(Settings.Secure.getInt(mainActivity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 0) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            mainActivity.applicationContext.startActivity(intent)
            return false
        }
        return true
    }
}