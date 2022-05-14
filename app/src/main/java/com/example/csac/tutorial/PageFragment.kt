package com.example.csac.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.csac.databinding.FragmentTutorialPageBinding

class PageFragment(private val image: Int) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTutorialPageBinding.inflate(LayoutInflater.from(context))
        binding.imageView.setImageResource(image)
        return binding.root
    }
}