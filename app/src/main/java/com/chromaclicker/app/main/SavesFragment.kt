package com.chromaclicker.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chromaclicker.app.databinding.FragmentSavesBinding
import java.io.File

/** Manages the saves screen */
class SavesFragment : ActionBarFragment("Saves", true) {
    private lateinit var binding: FragmentSavesBinding
    private lateinit var saveAdapter: SaveAdapter
    private lateinit var saveObserver: RecyclerView.AdapterDataObserver
    private var selected = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get currently selected save from bundle
        selected = arguments?.getString("selected") ?: ""
        // Inflate this fragment's layout
        binding = FragmentSavesBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveAdapter = SaveAdapter(activity as MainActivity, fetchSaveFiles(), selected)
        // Display a message if there are no saves after the user deletes a save
        saveObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                checkEmptyAdapter(saveAdapter)
            }
        }
        // Add the adapter, observer, and layout manager to savesList
        saveAdapter.registerAdapterDataObserver(saveObserver)
        binding.savesList.adapter = saveAdapter
        binding.savesList.layoutManager = LinearLayoutManager(context)
        checkEmptyAdapter(saveAdapter)
    }

    /** Returns a list of save file names */
    private fun fetchSaveFiles(): ArrayList<String> {
        val savesDir = File(context?.filesDir.toString() + "/saves")
        val files = arrayListOf<String>()
        savesDir.walk().forEachIndexed { index, file ->
            // Ignore the parent folder (first file in savesDir)
            if(index != 0) {
                files.add(file.name)
            }
        }
        // Sort names alphabetically
        files.sort()
        return files
    }

    /** Displays message if there are no saves. Otherwise, this will show the save [adapter]. */
    private fun checkEmptyAdapter(adapter: SaveAdapter) {
        if (adapter.itemCount > 0) {
            binding.emptyMessage.visibility = View.GONE
            binding.savesList.visibility = View.VISIBLE
        } else {
            binding.emptyMessage.visibility = View.VISIBLE
            binding.savesList.visibility = View.GONE
        }
    }
}