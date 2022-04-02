package com.example.csac.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csac.databinding.FragmentSavesBinding
import java.io.File

class SavesFragment : Fragment() {
    private lateinit var binding: FragmentSavesBinding
    private lateinit var saveAdapter: SaveAdapter
    private var fileNames: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = "Saves"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        fetchFileNames()
        // Inflate this fragment's layout
        binding = FragmentSavesBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val divider = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
        saveAdapter = SaveAdapter(requireContext(), fileNames, navController)
        binding.savesList.adapter = saveAdapter
        binding.savesList.layoutManager = LinearLayoutManager(context)
        binding.savesList.addItemDecoration(divider)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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