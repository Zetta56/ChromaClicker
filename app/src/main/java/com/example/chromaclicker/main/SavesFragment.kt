package com.example.chromaclicker.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chromaclicker.databinding.FragmentSavesBinding
import java.io.File

class SavesFragment : ActionBarFragment("Saves", true) {
    private lateinit var binding: FragmentSavesBinding
    private lateinit var saveAdapter: SaveAdapter
    private lateinit var saveObserver: RecyclerView.AdapterDataObserver
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
        saveObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                checkEmptyAdapter(saveAdapter)
            }
        }
        saveAdapter.registerAdapterDataObserver(saveObserver)
        binding.savesList.adapter = saveAdapter
        binding.savesList.layoutManager = LinearLayoutManager(context)
        binding.savesList.addItemDecoration(divider)
        checkEmptyAdapter(saveAdapter)
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