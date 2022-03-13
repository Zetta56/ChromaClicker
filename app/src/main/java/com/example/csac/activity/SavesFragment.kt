package com.example.csac.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.csac.R
import com.example.csac.models.Save
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class SavesFragment : Fragment() {
    private var saves: ArrayList<Save>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val savesDir = File(requireContext().filesDir.toString() + "/saves")
        val tempSaves = arrayListOf<Save>()
        savesDir.walk().forEachIndexed { index, file ->
            if(index != 0) {
                tempSaves.add(Json.decodeFromString<Save>(file.readText()))
            }
        }
        saves = tempSaves
//        val inputStream = File(filesDir, "save-test name").inputStream()
//        val inputString = inputStream.bufferedReader().use { it.readText() }
//        println(inputString)
        // Inflate this fragment's layout
        return inflater.inflate(R.layout.fragment_saves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println(saves)
    }
}