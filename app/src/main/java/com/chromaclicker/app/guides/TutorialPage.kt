package com.chromaclicker.app.guides

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chromaclicker.app.databinding.FragmentTutorialPageBinding

/** Renders a page in the tutorial populated with an [image] and [description] */
class TutorialPage(
    private val image: Int,
    private val description: Int
) : Fragment() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTutorialPageBinding.inflate(LayoutInflater.from(context))
        binding.image.setImageResource(image)
        binding.description.setText(description)
        // Stop view-pager from handling touch event on the description, allowing you to scroll
        // through a long description
        binding.description.setOnTouchListener { _, _ ->
            binding.root.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        // Reset description's movement method instead of following the view-pager
        binding.description.movementMethod = ScrollingMovementMethod()
        return binding.root
    }

}