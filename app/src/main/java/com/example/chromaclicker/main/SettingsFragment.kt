package com.example.chromaclicker.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.chromaclicker.databinding.FragmentSettingsBinding
import com.example.chromaclicker.getDefaultPreferences
import com.example.chromaclicker.models.AppSettings
import com.example.chromaclicker.overlay.OverlayService
import com.google.android.material.slider.Slider

class SettingsFragment : ActionBarFragment("Settings", true) {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(LayoutInflater.from(activity))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Default values
        val settings = AppSettings(activity as Context)
        binding.randomSwitch.isChecked = settings.random
        binding.clickInterval.setText(settings.clickInterval.toString())
        binding.detectInterval.setText(settings.detectInterval.toString())
        binding.radiusSlider.value = settings.circleRadius.toFloat()

        // Set listeners
        binding.randomSwitch.setOnCheckedChangeListener { _, _ -> toggleApplyButton(true) }
        binding.applyButton.setOnClickListener { applySettings() }
        binding.clickInterval.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) { validateInterval(v as EditText, 1000, 200) }
        }
        binding.detectInterval.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) { validateInterval(v as EditText, 5000, 2000) }
        }
        binding.radiusSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) { toggleApplyButton(true) }
        })
        toggleApplyButton(false)
    }

    private fun validateInterval(editText: EditText, default: Int, min: Int) {
        val text = editText.text.toString()
        val validated = when {
            text.isEmpty() -> default.toString()
            Integer.parseInt(text) < min -> min.toString()
            else -> text
        }
        if(validated != text) {
            editText.setText(validated)
            val message = "Interval must be an integer greater than or equal to $min"
            Toast.makeText(activity?.applicationContext, message, Toast.LENGTH_SHORT).show()
        }
        toggleApplyButton(true)
    }

    private fun toggleApplyButton(toggle: Boolean) {
        val textColor = if(toggle) "#2DADF4" else "#555555"
        binding.applyButton.setTextColor(Color.parseColor(textColor))
        binding.applyButton.isEnabled = toggle
    }

    private fun applySettings() {
        activity?.let { activity ->
            val editor = getDefaultPreferences(activity).edit()
            editor.putBoolean("setting_random", binding.randomSwitch.isChecked)
            editor.putInt("setting_click_interval", Integer.parseInt(binding.clickInterval.text.toString()))
            editor.putInt("setting_detect_interval", Integer.parseInt(binding.detectInterval.text.toString()))
            editor.putInt("setting_circle_radius", binding.radiusSlider.value.toInt())
            editor.apply()

            if(OverlayService.isRunning()) {
                val intent = Intent(activity.applicationContext, OverlayService::class.java)
                intent.action = "update_settings"
                intent.putExtra("settings", AppSettings(activity))
                activity.startService(intent)
            }
        }
        toggleApplyButton(false)
    }
}