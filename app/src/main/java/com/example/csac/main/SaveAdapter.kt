package com.example.csac.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.models.Save
import com.example.csac.overlay.SavePopup
import kotlinx.serialization.json.Json
import java.io.File

class SaveAdapter(
    private val context: Context,
    private val fileNames: ArrayList<String>,
    private val navController: NavController
) : RecyclerView.Adapter<SaveAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val saveName: TextView = view.findViewById(R.id.saveName)
        val renameButton: ImageButton = view.findViewById(R.id.renameButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.save_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = File("${context.filesDir}/saves/${fileNames[position]}")
        val saveName = Json.decodeFromString(Save.serializer(), file.readText()).name
        holder.saveName.text = saveName
        holder.saveName.setOnClickListener { selectSave(position) }
        holder.renameButton.setOnClickListener {
            SavePopup(context, saveName, fun(name) { renameSave(position, name) })
        }
        holder.deleteButton.setOnClickListener { deleteSave(position) }
    }

    override fun getItemCount(): Int {
        return fileNames.size
    }

    private fun selectSave(position: Int) {
        val bundle = Bundle()
        bundle.putString("saveName", fileNames[position])
        navController.navigate(R.id.action_savesFragment_to_mainFragment, bundle)
    }

    private fun renameSave(position: Int, name: String) {
        // Delete save file
        val oldFile = File("${context.filesDir}/saves/${fileNames[position]}")
        val save = Json.decodeFromString(Save.serializer(), oldFile.readText())
        save.name = name
        oldFile.delete()

        // Create new save file
        val newFile = File("${context.filesDir}/saves/${name}")
        newFile.writeText(Json.encodeToString(Save.serializer(), save))
        fileNames[position] = name
        notifyItemChanged(position)
    }

    private fun deleteSave(position: Int) {
        val name = fileNames[position]
        val file = File("${context.filesDir}/saves/${name}")
        file.delete()
        fileNames.remove(name)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}