package com.chromaclicker.app.overlay

import android.app.Service
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import com.chromaclicker.app.R
import com.chromaclicker.app.createOverlayLayout
import com.chromaclicker.app.databinding.DialogSaveBinding
import java.io.File

/**
 * This dialog lets you name and rename saves. You can specify a starting [name][defaultName] and
 * whether this can [only create saves][onlyNewSaves]. Additionally, the passed [callback] function
 * will be called with the submitted name when the user clicks the submit button.
 */
class SaveDialog(
    private val context: Context,
    private val defaultName: String,
    private val onlyNewSaves: Boolean = false,
    private val callback: (name: String) -> Unit
) {

    /** This decides which elements to show when an item is selected in the destination dropdown */
    inner class DestinationItemListener : AdapterView.OnItemSelectedListener {
        // Runs whenever the user selects an option in the destination dropdown
        override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(binding.destDropdown.selectedItem) {
                // Only show the name input
                "New Save" -> {
                    binding.nameInput.visibility = View.VISIBLE
                    binding.saveDropdown.visibility = View.GONE
                    binding.positive.isEnabled = binding.nameInput.text.isNotEmpty()
                }
                // Only show the saves dropdown
                "Existing Save" -> {
                    binding.nameInput.visibility = View.GONE
                    binding.saveDropdown.visibility = View.VISIBLE
                    binding.positive.isEnabled = true
                }
            }
        }
        // Don't do anything when nothing is selected
        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    private val binding = DialogSaveBinding.inflate(LayoutInflater.from(context))
    private val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager

    init {
        val layoutParams = createOverlayLayout(focusable=true)
        // Add fade-in and fade-out animations
        layoutParams.windowAnimations = android.R.style.Animation_Toast
        windowManager.addView(binding.root, layoutParams)

        initializeDestDropdown()
        binding.positive.isEnabled = false
        binding.positive.setOnClickListener { onSubmit() }
        // Destroy this dialog if the user clicks "cancel"
        binding.negative.setOnClickListener { windowManager.removeView(binding.root) }
        // Set default value for name input
        binding.nameInput.setText(defaultName)
        // Enable the submit button if the name input is no longer empty
        binding.nameInput.doAfterTextChanged { text -> binding.positive.isEnabled = (text?.isNotEmpty() == true) }
        binding.saveDropdown.adapter = createSaveAdapter()
        // Hide save dropdown until "Existing Saves" is clicked
        binding.saveDropdown.visibility = View.GONE
    }

    /**
     * Initializes the destination dropdown used to either save to a new file or an existing file.
     *
     * If onlyNewSaves is true, this will instead hide the destination dropdown.
     */
    private fun initializeDestDropdown() {
        if(!onlyNewSaves) {
            val destinations = arrayListOf("New Save", "Existing Save")
            binding.destDropdown.adapter = ArrayAdapter(context, R.layout.item_spinner, destinations)
            binding.destDropdown.onItemSelectedListener = DestinationItemListener()
        } else {
            binding.destDropdown.visibility = View.GONE
        }
    }

    /** Returns an [ArrayAdapter] populated with this app's save files. */
    private fun createSaveAdapter(): ArrayAdapter<String> {
        // Get save files from internal storage
        val savesDir = File(context.filesDir.toString() + "/saves")
        val saveFiles = arrayListOf<String>()
        savesDir.walk().forEachIndexed { index, file ->
            // Ignore the parent folder (first file in savesDir)
            if(index != 0) {
                saveFiles.add(file.name)
            }
        }
        // Sort save names alphabetically
        saveFiles.sort()
        // Add the default option
        saveFiles.add(0, "None")
        return ArrayAdapter(context, R.layout.item_spinner, saveFiles)
    }

    /** Calls the [callback] function with a validated save name and then removes this dialog */
    private fun onSubmit() {
        // If making a new save, validate that its file name is unique
        val name = if(onlyNewSaves || binding.destDropdown.selectedItem == "New Save") {
            getUniqueFileName("${context.filesDir}/saves", binding.nameInput.text.toString())
        // If updating an existing save, simply convert the selected save to a string
        } else {
            binding.saveDropdown.selectedItem.toString()
        }
        callback(name)
        // Destroy this dialog
        windowManager.removeView(binding.root)
    }

    /**
     * Returns a unique file name within a specified [path], given a desired [fileName]. This will
     * append a dash and number at the end until a unique name is found.
     */
    private fun getUniqueFileName(path: String, fileName: String): String {
        // Check if a file with the desired name already exists
        var duplicate = File(path, fileName)
        return if(duplicate.exists()) {
            // Add and increment a number at the end of the file name until it's unique
            var num = 1
            do {
                num++
                duplicate = File("${path}/${fileName}-${num}")
            } while(duplicate.exists())
            "${fileName}-${num}"
        // If a file with the desired name doesn't exist, return the file name as is
        } else {
            fileName
        }
    }
}