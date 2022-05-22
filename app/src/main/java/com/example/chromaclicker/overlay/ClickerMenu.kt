package com.example.chromaclicker.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chromaclicker.autoclick.AutoClickService
import com.example.chromaclicker.createOverlayLayout
import com.example.chromaclicker.databinding.ClickerMenuBinding
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
    private val clickerViews: MutableList<ClickerView>,
    private val overlayMenu: View,
) {

    private var menuVisible = true
    private val binding = ClickerMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(focusable=true)
    private val detectorViews = convertDetectors(clicker.detectors)
    private val detectorAdapter = DetectorAdapter(context, detectorViews, binding.root)

    init {
        windowManager.addView(binding.root, layoutParams)
        detectorViews.forEach { view -> binding.root.addView(view, 0) }
        binding.detectorForms.adapter = detectorAdapter
        binding.detectorForms.layoutManager = LinearLayoutManager(context)
        binding.clicker.x = position[0]
        binding.clicker.y = position[1]

        // Add listeners
        binding.checkButton.setOnClickListener { confirm() }
        binding.plusButton.setOnClickListener { addDetector() }
        binding.crossButton.setOnClickListener { cancel() }
        binding.root.setOnClickListener { toggleVisibility(false) }
        setRecursiveTouchListener(binding.menu) { _, _ -> if (!menuVisible) toggleVisibility(true) else false }
    }

    fun hasWindowToken(): Boolean {
        // Returns whether this layout was added to the window manager
        return binding.root.windowToken != null
    }

    fun onDestroy() {
        windowManager.removeView(binding.root)
    }

    private fun convertDetectors(detectors: Array<Detector>): MutableList<DetectorView> {
        val viewList = detectors.map { detector ->
            val view = DetectorView(context, null)
            view.startX = clickerCenter[0]
            view.startY = clickerCenter[1]
            view.endX = detector.x
            view.endY = detector.y
            view.color = detector.color
            view.invalidate()
            return@map view
        }
        return viewList.toMutableList()
    }

    private fun confirm() {
        val detectorList = detectorViews.map { detectorView -> Detector(detectorView) }
        clicker.detectors = detectorList.toTypedArray()
        cancel()
    }

    private fun addDetector() {
        val detectorView = DetectorView(context, null)
        detectorView.startX = clickerCenter[0]
        detectorView.startY = clickerCenter[1]
        detectorView.invalidate()
        binding.root.addView(detectorView, 0)
        detectorViews += detectorView
        detectorAdapter.notifyItemInserted(detectorViews.size - 1)
    }

    private fun cancel() {
        clickerViews.forEach { clickerView -> clickerView.visibility = View.VISIBLE }
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