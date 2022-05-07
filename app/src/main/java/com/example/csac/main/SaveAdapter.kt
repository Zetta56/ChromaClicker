package com.example.csac.main

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.getDefaultPreferences
import com.example.csac.models.Save
import com.example.csac.overlay.OverlayService
import com.example.csac.overlay.SavePopup
import com.google.gson.Gson
import java.io.File

class SaveAdapter(
    private val activity: Activity,
    private val fileNames: ArrayList<String>,
    selected: String
) : RecyclerView.Adapter<SaveAdapter.ViewHolder>() {

    private var selectedSaveName = selected
    private var selectedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val saveName: TextView = view.findViewById(R.id.saveName)
        val check: ImageView = view.findViewById(R.id.check)
        val renameButton: ImageButton = view.findViewById(R.id.renameButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.save_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = File("${activity.filesDir}/saves/${fileNames[position]}")
        val save = Gson().fromJson(file.readText(), Save::class.java)
        if(selectedSaveName == save.name) {
            holder.check.setColorFilter(Color.WHITE)
            holder.check.visibility = View.VISIBLE
            selectedPosition = holder.adapterPosition
        } else {
            holder.check.visibility = View.GONE
        }
        holder.saveName.text = save.name

        holder.saveName.setOnClickListener {
            if(selectedSaveName == save.name) deselectSave(position) else selectSave(position, save)
        }
        holder.renameButton.setOnClickListener {
            SavePopup(activity, save.name, true, fun(name) { renameSave(position, save, name) })
        }
        holder.deleteButton.setOnClickListener {
            deleteSave(position)
        }
    }

    override fun getItemCount(): Int {
        return fileNames.size
    }

    private fun selectSave(position: Int, save: Save) {
        val preferences = getDefaultPreferences(activity)
        preferences.edit().putString("saveName", save.name).apply()
        selectedSaveName = save.name
        notifyItemChanged(position)
        notifyItemChanged(selectedPosition)
        // selectedPosition must be changed last to deselect the previous save
        selectedPosition = position

        // Reload overlay if it's already running
        if(OverlayService.isRunning()) {
            val mainActivity = activity as MainActivity
            mainActivity.toggleOverlay(false, save)
            mainActivity.toggleOverlay(true, save)
        }
    }

    private fun deselectSave(position: Int) {
        val preferences = getDefaultPreferences(activity)
        preferences.edit().putString("saveName", "").apply()
        selectedSaveName = ""
        selectedPosition = -1
        notifyItemChanged(position)
    }

    private fun renameSave(position: Int, save: Save, name: String) {
        // Delete save file
        val oldFile = File("${activity.filesDir}/saves/${fileNames[position]}")
        oldFile.delete()

        // Create new save file
        val newFile = File("${activity.filesDir}/saves/${name}")
        save.name = name
        newFile.writeText(Gson().toJson(save))
        fileNames[position] = name
        notifyItemChanged(position)
    }

    private fun deleteSave(position: Int) {
        val name = fileNames[position]
        val file = File("${activity.filesDir}/saves/${name}")
        file.delete()
        fileNames.remove(name)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}