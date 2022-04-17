package com.example.csac.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.csac.databinding.FragmentSettingsBinding
import com.example.csac.getDefaultPreferences
import com.example.csac.models.AppSettings
import com.example.csac.overlay.OverlayService

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = "Settings"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(LayoutInflater.from(activity))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        binding.randomSwitch.setOnCheckedChangeListener { _, _ -> toggleApplyButton(true) }
        binding.applyButton.setOnClickListener { applySettings() }
        toggleApplyButton(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleApplyButton(toggle: Boolean) {
        val textColor = if(toggle) "#2DADF4" else "#555555"
        binding.applyButton.setTextColor(Color.parseColor(textColor))
        binding.applyButton.isEnabled = toggle
    }

    private fun loadSettings() {
        activity?.let { activity ->
            val preferences = getDefaultPreferences(activity)
            binding.randomSwitch.isChecked = preferences.getBoolean("setting_random", false)
        } ?: run {
            binding.randomSwitch.isChecked = false
        }
    }

    private fun applySettings() {
        activity?.let { activity ->
            val preferences = getDefaultPreferences(activity)
            preferences.edit().putBoolean("setting_random", binding.randomSwitch.isChecked).apply()
            if(OverlayService.isRunning()) {
                val intent = Intent(activity.applicationContext, OverlayService::class.java)
                intent.action = "update_settings"
                intent.putExtra("settings", AppSettings())
                activity.startService(intent)
            }
        }
        toggleApplyButton(false)
    }
}