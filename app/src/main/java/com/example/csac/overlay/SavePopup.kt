package com.example.csac.overlay

import android.app.Service
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import com.example.csac.R
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.SavePopupBinding
import java.io.File

class SavePopup(
    private val context: Context,
    defaultName: String,
    private val onlyNewSaves: Boolean = false,
    private val callback: (name: String) -> Unit
) {
    private val binding = SavePopupBinding.inflate(LayoutInflater.from(context))
    private val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager

    init {
        val layoutParams = createOverlayLayout(300, 200, focusable=true)
        // Add fade-in and fade-out animations
        layoutParams.windowAnimations = android.R.style.Animation_Toast
        windowManager.addView(binding.root, layoutParams)

        if(!onlyNewSaves) {
            initializeDestOptions()
        } else {
            binding.destOptions.visibility = View.GONE
        }
        binding.positive.isEnabled = false
        binding.positive.setOnClickListener { onSaveClick() }
        binding.negative.setOnClickListener { windowManager.removeView(binding.root) }
        binding.nameInput.setText(defaultName)
        binding.nameInput.doAfterTextChanged { text -> binding.positive.isEnabled = (text?.isNotEmpty() == true) }
        binding.saveOptions.adapter = createSaveAdapter()
        binding.saveOptions.visibility = View.GONE
    }

    private fun initializeDestOptions() {
        val types = arrayListOf("New Save", "Existing Save")
        binding.destOptions.adapter = ArrayAdapter(context, R.layout.spinner_item, types)
        binding.destOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(binding.destOptions.selectedItem) {
                    "New Save" -> {
                        binding.nameInput.visibility = View.VISIBLE
                        binding.saveOptions.visibility = View.GONE
                        binding.positive.isEnabled = binding.nameInput.text.isNotEmpty()
                    }
                    "Existing Save" -> {
                        binding.nameInput.visibility = View.GONE
                        binding.saveOptions.visibility = View.VISIBLE
                        binding.positive.isEnabled = true
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun createSaveAdapter(): ArrayAdapter<String> {
        val savesDir = File(context.filesDir.toString() + "/saves")
        val saveFiles = arrayListOf<String>()
        savesDir.walk().forEachIndexed { index, file ->
            // Ignore the parent folder (first file in savesDir)
            if(index != 0) {
                saveFiles.add(file.name)
            }
        }
        // Sort names alphabetically
        saveFiles.sort()
        saveFiles.add(0, "None")
        return ArrayAdapter(context, R.layout.spinner_item, saveFiles)
    }

    private fun onSaveClick() {
        val name =  if(onlyNewSaves || binding.destOptions.selectedItem == "New Save") {
            getUniqueFileName("${context.filesDir}/saves", binding.nameInput.text.toString())
        } else {
            binding.saveOptions.selectedItem.toString()
        }
        callback(name)
        windowManager.removeView(binding.root)
    }

    private fun getUniqueFileName(path: String, fileName: String): String {
        var duplicate = File(path, fileName)
        return if(duplicate.exists()) {
            var num = 1
            do {
                num++
                duplicate = File("${path}/${fileName}-${num}")
            } while(duplicate.exists())
            "${fileName}-${num}"
        } else {
            fileName
        }
    }
}