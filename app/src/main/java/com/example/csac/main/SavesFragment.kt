package com.example.csac.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csac.databinding.FragmentSavesBinding
import com.example.csac.models.Save
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class SavesFragment : Fragment() {
    private lateinit var binding: FragmentSavesBinding
    private lateinit var saveAdapter: SaveAdapter
    private var saves: ArrayList<Save> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadSaves()
        (activity as AppCompatActivity).supportActionBar?.title = "Saves"
        // Inflate this fragment's layout
        binding = FragmentSavesBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val divider = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
        saveAdapter = SaveAdapter(requireContext(), saves)
        binding.savesList.adapter = saveAdapter
        binding.savesList.layoutManager = LinearLayoutManager(context)
        binding.savesList.addItemDecoration(divider)
    }

    private fun loadSaves() {
        val savesDir = File(requireContext().filesDir.toString() + "/saves")
        val tempSaves = arrayListOf<Save>()
        savesDir.walk().forEachIndexed { index, file ->
            // Ignore the parent folder (first file in savesDir)
            if(index != 0) {
                tempSaves.add(Json.decodeFromString(file.readText()))
            }
        }
        // Sort alphabetically by name
        tempSaves.sortBy { save -> save.name }
        saves = tempSaves
    }
}