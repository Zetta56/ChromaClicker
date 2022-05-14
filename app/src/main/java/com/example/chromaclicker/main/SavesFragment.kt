package com.example.chromaclicker.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chromaclicker.databinding.FragmentSavesBinding
import java.io.File

class SavesFragment : ActionBarFragment("Saves", true) {
    private lateinit var binding: FragmentSavesBinding
    private lateinit var saveAdapter: SaveAdapter
    private var fileNames: ArrayList<String> = arrayListOf()
    private var selected = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fetchFileNames()
        selected = arguments?.getString("selected") ?: ""
        // Inflate this fragment's layout
        binding = FragmentSavesBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val divider = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
        saveAdapter = SaveAdapter(activity as Activity, fileNames, selected)
        binding.savesList.adapter = saveAdapter
        binding.savesList.layoutManager = LinearLayoutManager(context)
        binding.savesList.addItemDecoration(divider)
    }

    private fun fetchFileNames() {
        val savesDir = File(requireContext().filesDir.toString() + "/saves")
        val names = arrayListOf<String>()
        savesDir.walk().forEachIndexed { index, file ->
            // Ignore the parent folder (first file in savesDir)
            if(index != 0) {
                names.add(file.name)
            }
        }
        // Sort names alphabetically
        names.sort()
        fileNames = names
    }
}