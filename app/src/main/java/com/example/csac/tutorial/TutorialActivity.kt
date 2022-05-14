package com.example.csac.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.csac.R
import com.example.csac.databinding.ActivityTutorialBinding
import com.google.android.material.tabs.TabLayoutMediator

class TutorialActivity : AppCompatActivity() {

    private class PagerAdapter(activity: FragmentActivity, private val images: List<Int>) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return images.size
        }
        override fun createFragment(position: Int): Fragment {
            return PageFragment(images[position])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val images = listOf(R.drawable.tutorial_one, R.drawable.tutorial_two, R.drawable.tutorial_three)
        val binding = ActivityTutorialBinding.inflate(LayoutInflater.from(this))
        binding.pager.adapter = PagerAdapter(this, images)
        setContentView(binding.root)
    }

}