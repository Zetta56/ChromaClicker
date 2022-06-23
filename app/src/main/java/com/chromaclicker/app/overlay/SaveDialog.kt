package com.chromaclicker.app.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chromaclicker.app.R
import com.chromaclicker.app.databinding.DialogSaveBinding
import com.chromaclicker.app.toPixels
import java.io.File

/**
 * This dialog activity lets you enter a save name and broadcast it to receivers listening to
 * the intent filter "receive_save_name". You can launch this by calling SaveDialog.launch().
 */
class SaveDialog : AppCompatActivity() {

    companion object {
        /**
         * Launch the [SaveDialog] using a [context]. You can specify your save's [defaultName]
         * and whether the user is currently [renaming][isRenaming] their save
         */
        fun launch(context: Context, defaultName: String, isRenaming: Boolean = false) {
            val intent = Intent(context, SaveDialog::class.java)
            intent.putExtra("defaultName", defaultName)
            intent.putExtra("isRenaming", isRenaming)
            // Add FLAG_ACTIVITY_NEW_TASK if this is called with an application context
            if(context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /** This decides which inputs to show when an action is selected */
    inner class DestinationItemListener : AdapterView.OnItemSelectedListener {
        // Runs whenever the user selects an option in the destination dropdown
        override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(binding.actionDropdown.selectedItem) {
                // Only show the name input
                "New Save" -> {
                    binding.nameInput.visibility = View.VISIBLE
                    binding.saveDropdown.visibility = View.GONE
                    binding.positive.isEnabled = binding.nameInput.text.isNotEmpty()
                }
                // Only show the saves dropdown
                "Update Save" -> {
                    binding.nameInput.visibility = View.GONE
                    binding.saveDropdown.visibility = View.VISIBLE
                    binding.positive.isEnabled = true
                }
            }
        }
        // Don't do anything when nothing is selected
        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    private lateinit var binding: DialogSaveBinding
    private var isRenaming: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogSaveBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        // Stop user from exiting this dialog by clicking outside it
        setFinishOnTouchOutside(false)
        // Set dialog dimensions
        window.setLayout(toPixels(300), toPixels(240))

        isRenaming = intent.extras!!.getBoolean("isRenaming")
        binding.positive.isEnabled = false
        binding.positive.setOnClickListener { onSubmit() }
        // Destroy this dialog if the user clicks "cancel"
        binding.negative.setOnClickListener {
            broadcast("", false)
            finish()
        }

        // Set default value for name input
        binding.nameInput.setText(intent.extras!!.getString("defaultName"))
        // Enable the submit button if the name input is no longer empty
        binding.nameInput.doAfterTextChanged { text ->
            binding.positive.isEnabled = (text?.isNotEmpty() == true)
        }

        binding.saveDropdown.adapter = createSaveAdapter()
        setupActionDropdown()
        // Hide save dropdown until "Existing Saves" is clicked
        binding.saveDropdown.visibility = View.GONE
    }

    /**
     * Initializes the action dropdown used to either save to a new file, update an existing file,
     * or rename a specific file.
     *
     * If onlyNewSaves is true, this will instead hide the destination dropdown.
     */
    private fun setupActionDropdown() {
        // Hide the option to save to existing saves if this is the first save
        val actions = when {
            isRenaming -> { arrayListOf("Rename Save") }
            binding.saveDropdown.adapter.count == 0 -> { arrayListOf("New Save") }
            else -> { arrayListOf("New Save", "Update Save") }
        }
        binding.actionDropdown.adapter = ArrayAdapter(this, R.layout.item_spinner, actions)
        binding.actionDropdown.onItemSelectedListener = DestinationItemListener()
    }

    /** Returns an [ArrayAdapter] populated with this app's save files. */
    private fun createSaveAdapter(): ArrayAdapter<String> {
        // Get save files from internal storage
        val savesDir = File("$filesDir/saves")
        val saveFiles = arrayListOf<String>()
        savesDir.walk().forEachIndexed { index, file ->
            // Ignore the parent folder (first file in savesDir)
            if(index != 0) {
                saveFiles.add(file.name)
            }
        }
        // Sort save names alphabetically
        saveFiles.sort()
        return ArrayAdapter(this, R.layout.item_spinner, saveFiles)
    }

    /** Validates and broadcasts the save name, before closing this dialog */
    private fun onSubmit() {
        // If making a new save, validate that its file name is unique
        val name = if(isRenaming || binding.actionDropdown.selectedItem == "New Save") {
            getUniqueFileName("${filesDir}/saves", binding.nameInput.text.toString())
        // If updating an existing save, simply convert the selected save to a string
        } else {
            binding.saveDropdown.selectedItem.toString()
        }
        broadcast(name, true)
        finish()
    }

    /**
     * Broadcasts the desired [name] and whether the name was [successfully][success] submitted
     * to receivers listening for "receive_save_name".
     */
    private fun broadcast(name: String, success: Boolean) {
        val intent = Intent("receive_save_name")
        intent.putExtra("name", name)
        intent.putExtra("success", success)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
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