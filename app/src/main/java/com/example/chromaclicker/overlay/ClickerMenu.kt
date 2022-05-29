package com.example.chromaclicker.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chromaclicker.createOverlayLayout
import com.example.chromaclicker.databinding.MenuClickerBinding
import com.example.chromaclicker.models.Clicker
import com.example.chromaclicker.models.Detector
import com.example.chromaclicker.setRecursiveTouchListener

@SuppressLint("ClickableViewAccessibility")
class ClickerMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val clicker: Clicker,
    position: List<Float>,
    private val clickerCenter: Array<Float>,
    private val circles: MutableList<Circle>,
    private val overlayMenu: View,
) {

    private var menuVisible = true
    private val binding = MenuClickerBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(focusable=true)
    private val lines = convertDetectors(clicker.detectors)
    private val detectorAdapter = DetectorAdapter(context, lines, binding.root, clickerCenter)

    init {
        layoutParams.windowAnimations = android.R.style.Animation_Toast
        windowManager.addView(binding.root, layoutParams)
        lines.forEach { line -> binding.root.addView(line, 0) }
        binding.detectorForms.adapter = detectorAdapter
        binding.detectorForms.layoutManager = LinearLayoutManager(context)
        binding.clicker.x = position[0]
        binding.clicker.y = position[1]

        // Add listeners
        binding.confirmButton.setOnClickListener { confirm() }
        binding.cancelButton.setOnClickListener { cancel() }
        binding.root.setOnClickListener { toggleVisibility(false) }
        // Making menu clickable stops click events from propagating up to the root view
        binding.menu.isClickable = true
        setRecursiveTouchListener(binding.menu) { _, _ -> if (!menuVisible) toggleVisibility(true) else false }
    }

    fun hasWindowToken(): Boolean {
        // Returns whether this layout was added to the window manager
        return binding.root.windowToken != null
    }

    fun onDestroy() {
        windowManager.removeView(binding.root)
    }

    private fun convertDetectors(detectors: Array<Detector>): MutableList<Line> {
        val lines = detectors.map { detector ->
            val line = Line(context, null)
            line.startX = clickerCenter[0]
            line.startY = clickerCenter[1]
            line.endX = detector.x
            line.endY = detector.y
            line.color = detector.color
            line.invalidate()
            return@map line
        }
        return lines.toMutableList()
    }

    private fun confirm() {
        val detectorList = lines.map { line -> Detector(line) }
        clicker.detectors = detectorList.toTypedArray()
        cancel()
    }

    private fun cancel() {
        circles.forEach { circle -> circle.visibility = View.VISIBLE }
        overlayMenu.visibility = View.VISIBLE
        onDestroy()
    }

    private fun toggleVisibility(toggle: Boolean): Boolean {
        menuVisible = toggle
        binding.menu.alpha = if(menuVisible) 1f else 0.1f
        windowManager.updateViewLayout(binding.root, layoutParams)
        return true
    }
}