package com.example.csac.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.getUniqueFileName
import com.example.csac.models.Save
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
        holder.renameButton.setOnClickListener { buildRenameDialog(position) }
        holder.deleteButton.setOnClickListener { deleteSave(position) }
    }

    override fun getItemCount(): Int {
        return saves.size
    }

    private fun buildRenameDialog(position: Int) {
        val editText = EditText(context)
        editText.setTextColor(Color.WHITE)
        val builder = AlertDialog.Builder(context, R.style.saveDialog)
        builder.setView(editText)
        builder.setTitle("Rename Save")
        builder.setPositiveButton("Save") { _, _ -> renameSave(position, editText.text.toString()) }
        builder.setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss()}
        val dialog = builder.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
        dialog.window?.setLayout(750, 475)

        editText.doAfterTextChanged { text ->
            val hasText = text?.isNotEmpty() == true
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = hasText
        }
    }

    private fun renameSave(position: Int, newName: String) {
        val path = "${context.filesDir}/saves"
        val name = getUniqueFileName(path, newName)
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