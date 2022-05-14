package com.example.csac.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.csac.R
import com.example.csac.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {

    private class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        val images = listOf(R.drawable.tutorial_one, R.drawable.tutorial_two, R.drawable.tutorial_three)
        val descriptions = listOf(R.string.tutorial_one, R.string.tutorial_two, R.string.tutorial_three)

        override fun getItemCount(): Int {
            return images.size
        }
        override fun createFragment(position: Int): Fragment {
            return PageFragment(images[position], descriptions[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Tutorial"

        val binding = ActivityTutorialBinding.inflate(LayoutInflater.from(this))
        binding.pager.adapter = PagerAdapter(this)
        setContentView(binding.root)
    }

}