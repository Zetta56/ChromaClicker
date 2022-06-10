package com.example.chromaclicker.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chromaclicker.R
import com.example.chromaclicker.getDefaultPreferences
import com.example.chromaclicker.models.Save
import com.example.chromaclicker.overlay.OverlayService
import com.example.chromaclicker.overlay.SaveDialog
import com.google.gson.Gson
import java.io.File

/**
 * Manages a list of save views. This displays a list of [save names][fileNames] and a check mark
 * next to the currently selected save's [name][selected].
 */
class SaveAdapter(
    private val activity: MainActivity,
    private val fileNames: ArrayList<String>,
    private val selected: String
) : RecyclerView.Adapter<SaveAdapter.ViewHolder>() {

    private var selectedSaveName = selected
    private var selectedPosition = -1

    /** Holds the views for each individual save item */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val saveName: TextView = view.findViewById(R.id.saveName)
        val check: ImageView = view.findViewById(R.id.check)
        val renameButton: ImageButton = view.findViewById(R.id.renameButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_save, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get save from the provided file name
        val file = File("${activity.filesDir}/saves/${fileNames[position]}")
        val save = Gson().fromJson(file.readText(), Save::class.java)
        // Show a white check next to the selected save
        if(selectedSaveName == save.name) {
            holder.check.setColorFilter(Color.WHITE)
            holder.check.visibility = View.VISIBLE
            selectedPosition = holder.adapterPosition
        // Hide the check if this save isn't being selected
        } else {
            holder.check.visibility = View.GONE
        }
        // Display this save's name
        holder.saveName.text = save.name

        // Toggle whether this save is being selected whenever its name is clicked
        holder.saveName.setOnClickListener {
            if(selectedSaveName == save.name) deselectSave(position) else selectSave(position, save)
        }
        // Open a renaming dialog whenever the rename button is clicked
        holder.renameButton.setOnClickListener {
            SaveDialog(activity, save.name, true, fun(name) { renameSave(position, save, name) })
        }
        // Delete the save whenever the delete button is clicked
        holder.deleteButton.setOnClickListener { deleteSave(position) }
    }

    override fun getItemCount(): Int {
        return fileNames.size
    }

    /**
     * Select the [save] at the provided [position] in the adapter and in the shared preferences.
     * This will also reload the overlay if it is currently running
     */
    private fun selectSave(position: Int, save: Save) {
        val preferences = getDefaultPreferences(activity)
        preferences.edit().putString("saveName", save.name).apply()
        selectedSaveName = save.name
        // selectedPosition must be changed after notifying the adapter to reload both save items
        notifyItemChanged(position)
        notifyItemChanged(selectedPosition)
        selectedPosition = position

        // Reload overlay if it's already running
        if(OverlayService.isRunning()) {
            activity.toggleOverlay(false, save)
            activity.toggleOverlay(true, save)
        }
    }

    /** Deselect the save at the provided [position] in the adapter and in the shared preferences */
    private fun deselectSave(position: Int) {
        val preferences = getDefaultPreferences(activity)
        preferences.edit().putString("saveName", "").apply()
        selectedSaveName = ""
        selectedPosition = -1
        notifyItemChanged(position)
    }

    /**
     *  Renames the [save] and its displayed name at the provided [position] in the adapter with
     *  the new [name]
     */
    private fun renameSave(position: Int, save: Save, name: String) {
        // Delete the save file
        val oldFile = File("${activity.filesDir}/saves/${fileNames[position]}")
        oldFile.delete()

        // Create a new save file with the new name
        val newFile = File("${activity.filesDir}/saves/${name}")
        save.name = name
        newFile.writeText(Gson().toJson(save))
        fileNames[position] = name
        notifyItemChanged(position)
    }

    /**
     * Deletes the save at the specified [position] from the save list and from this app's
     * internal storage
     */
    private fun deleteSave(position: Int) {
        val name = fileNames[position]
        val file = File("${activity.filesDir}/saves/${name}")
        file.delete()
        fileNames.remove(name)
        // Reload adapter item and item count
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}