package com.chromaclicker.app.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chromaclicker.app.R
import com.chromaclicker.app.databinding.ActivityTutorialBinding

/** This activity populates and displays the tutorial. */
class TutorialActivity : AppCompatActivity() {

    /** Manages this layout's ViewPager2 */
    private class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        // Initialize images and descriptions for each tutorial page
        val images = listOf(R.drawable.tutorial_one, R.drawable.tutorial_two, R.drawable.tutorial_three)
        val descriptions = listOf(R.string.tutorial_one, R.string.tutorial_two, R.string.tutorial_three)

        override fun getItemCount(): Int {
            return images.size
        }

        override fun createFragment(position: Int): Fragment {
            return TutorialPage(images[position], descriptions[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Tutorial"
        // Show back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = ActivityTutorialBinding.inflate(LayoutInflater.from(this))
        binding.pager.adapter = PagerAdapter(this)
        setContentView(binding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Go back when pressing the back button
        if(item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}