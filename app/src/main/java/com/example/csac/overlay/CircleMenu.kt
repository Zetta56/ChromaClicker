package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.csac.databinding.CircleFormBinding
import com.example.csac.databinding.CircleMenuBinding

class CircleMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val overlayMenuRoot: View
) {

    private val binding = CircleMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = OverlayService.createOverlayLayout(270, 60, focusable=true)

    init {
        windowManager.addView(binding.root, layoutParams)
        binding.checkButton.setOnClickListener { confirm() }
        binding.plusButton.setOnClickListener { addDetector() }
        binding.crossButton.setOnClickListener { cancel() }
        addDetector()
//        addTouchListeners()
    }

//    @SuppressLint("ClickableViewAccessibility")
//    private fun addTouchListeners() {
//        val draggable = Draggable(windowManager, layoutParams, binding.root)
//        binding.root.setOnTouchListener(draggable)
//        binding.topSection.setOnTouchListener(draggable)
//        binding.checkButton.setOnTouchListener(draggable)
//        binding.plusButton.setOnTouchListener(draggable)
//        binding.crossButton.setOnTouchListener(draggable)
//    }
//
//    fun setPosition(x: Int, y: Int) {
//        layoutParams.x = x
//        layoutParams.y = y
//        windowManager.updateViewLayout(binding.root, layoutParams)
//    }

    private fun confirm() {
        overlayMenuRoot.visibility = View.VISIBLE
        windowManager.removeView(binding.root)
    }

    private fun addDetector() {
        val form = CircleFormBinding.inflate(LayoutInflater.from(context))
        binding.root.addView(form.root)
        layoutParams.height += (40 * Resources.getSystem().displayMetrics.density).toInt()
        windowManager.updateViewLayout(binding.root, layoutParams)
    }

    private fun cancel() {
        overlayMenuRoot.visibility = View.VISIBLE
        windowManager.removeView(binding.root)
    }
}