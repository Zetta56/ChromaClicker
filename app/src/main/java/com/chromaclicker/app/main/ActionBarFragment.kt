package com.chromaclicker.app.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chromaclicker.app.R
import com.chromaclicker.app.tutorial.TutorialActivity

/**
 * Fragments can inherit from this class to display an action bar. You can decide its [title] and
 * whether to [show a back button][hasBackButton].
 */
open class ActionBarFragment(
    private val title: String,
    private val hasBackButton: Boolean
) : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = title
        // Show back button
        actionBar?.setDisplayHomeAsUpEnabled(hasBackButton)
        // Populate options menu
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // Go back when pressing the back button
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
            // Show the tutorial when pressing the question mark button
            R.id.questionMark -> {
                activity?.let { activity ->
                    val intent = Intent(activity.applicationContext, TutorialActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}