package com.example.csac.overlay

import android.app.Service
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.SavePopupBinding
import java.io.File

class SavePopup(
    private val context: Context,
    defaultName: String,
    private val callback: (name: String) -> Unit
) {
    init {
        val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        val savePopup = SavePopupBinding.inflate(LayoutInflater.from(context))
        val layoutParams = createOverlayLayout(300, 180, focusable=true)
        windowManager.addView(savePopup.root, layoutParams)

        savePopup.positive.isEnabled = false
        savePopup.positive.setOnClickListener {
            val name = getUniqueFileName("${context.filesDir}/saves", savePopup.nameInput.text.toString())
            callback(name)
            windowManager.removeView(savePopup.root)
        }
        savePopup.negative.setOnClickListener {
            windowManager.removeView(savePopup.root)
        }
        savePopup.nameInput.setText(defaultName)
        savePopup.nameInput.doAfterTextChanged { text -> savePopup.positive.isEnabled = (text?.isNotEmpty() == true) }
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