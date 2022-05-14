package com.example.chromaclicker.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.chromaclicker.R
import com.example.chromaclicker.tutorial.TutorialActivity

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
            R.id.questionMark -> {
                activity?.let {
                    val intent = Intent(it.applicationContext, TutorialActivity::class.java)
                    it.startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}