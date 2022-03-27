package com.example.csac.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.models.Save
import com.example.csac.overlay.SavePopup
import kotlinx.serialization.json.Json
import java.io.File

class SaveAdapter(
    private val context: Context,
    private val saves: ArrayList<Save>
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
        holder.saveName.text = saves[position].name
        holder.renameButton.setOnClickListener {
            SavePopup(context, fun(name) {
                renameSave(position, name)
            })
        }
        holder.deleteButton.setOnClickListener { deleteSave(position) }
    }

    override fun getItemCount(): Int {
        return saves.size
    }

    private fun renameSave(position: Int, name: String) {
        val path = "${context.filesDir}/saves"
        // Delete save file
        val oldFile = File("${path}/${saves[position].name}")
        oldFile.delete()
        // Make a new file with the updated name
        val newFile = File("${path}/${name}")
        saves[position].name = name
        newFile.writeText(Json.encodeToString(Save.serializer(), saves[position]))
        notifyItemChanged(position)

    }

    private fun deleteSave(position: Int) {
        val save = saves[position]
        val file = File("${context.filesDir}/saves/${save.name}")
        file.delete()
        saves.remove(save)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}