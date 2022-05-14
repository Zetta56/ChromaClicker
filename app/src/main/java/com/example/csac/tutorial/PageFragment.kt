package com.example.csac.tutorial

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.csac.databinding.FragmentTutorialPageBinding

class PageFragment(private val image: Int, private val description: Int) : Fragment() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTutorialPageBinding.inflate(LayoutInflater.from(context))
        binding.image.setImageResource(image)
        binding.description.setText(description)
        binding.description.setOnTouchListener { _, _ ->
            // Stop view-pager from handling touch event on description
            binding.root.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        // Reset description's movement method instead of following view-pager
        binding.description.movementMethod = ScrollingMovementMethod()
        return binding.root
    }
}