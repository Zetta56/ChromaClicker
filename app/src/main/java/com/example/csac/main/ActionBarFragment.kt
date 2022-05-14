package com.example.csac.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.csac.R

open class ActionBarFragment(
    private val title: String,
    private val hasOptionsMenu: Boolean
) : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = title
        // Show back button
        actionBar?.setDisplayHomeAsUpEnabled(hasOptionsMenu)
        // Populate options menu
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
//            R.id.questionMark -> {
//                println("tutorial")
//            }
        }
        return super.onOptionsItemSelected(item)
    }
}