package com.example.csac

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment(), View.OnClickListener {
    private lateinit var mainActivity: MainActivity
    private lateinit var navController: NavController
    private lateinit var powerButton: ImageButton
    private lateinit var overlayIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        navController = findNavController()
        // ::class.java takes OverlayService's metadata and spits out its Java class
        overlayIntent = Intent(mainActivity.applicationContext, OverlayService::class.java)
        powerButton = view.findViewById<ImageButton>(R.id.powerButton)

        // Set power button color
        if(mainActivity.overlayVisible) {
            powerButton.setImageResource(R.drawable.power_on)
        } else {
            powerButton.setImageResource(R.drawable.power_off)
        }

        // Add click listeners
        powerButton.setOnClickListener(this)
        view.findViewById<Button>(R.id.savesButton).setOnClickListener(this)
        view.findViewById<Button>(R.id.settingsButton).setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id) {
            R.id.powerButton -> {
                mainActivity.overlayVisible = !mainActivity.overlayVisible
                if(mainActivity.overlayVisible) {
                    powerButton.setImageResource(R.drawable.power_on)
                    if (Build.VERSION.SDK_INT >= 26) {
                        mainActivity.applicationContext.startForegroundService(overlayIntent)
                    } else {
                        mainActivity.applicationContext.startService(overlayIntent)
                    }
                } else {
                    powerButton.setImageResource(R.drawable.power_off)
                    mainActivity.applicationContext.stopService(overlayIntent)
                }
            }
            R.id.savesButton -> navController.navigate(R.id.action_mainFragment_to_savesFragment)
            R.id.settingsButton -> navController.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }
}