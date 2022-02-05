package com.example.csac.overlay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csac.R
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.ClickerMenuBinding
import com.example.csac.models.Clicker
import com.example.csac.models.Detector
import com.example.csac.toPixels

class ClickerMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val clicker: Clicker,
    private val drawing: ViewGroup,
    private val clickerCenter: Array<Float>,
    private val clickerViews: MutableList<ClickerView>,
    private val overlayMenu: View,
) {

    private val binding = ClickerMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(270, 235, focusable=true)
    private val detectorViews = mutableListOf<DetectorView>()
    private val detectorAdapter = DetectorAdapter(detectorViews, drawing)
    private var dipping = false
    private var visible = true

    init {
        windowManager.addView(binding.root, layoutParams)
        binding.detectorForms.adapter = detectorAdapter
        binding.detectorForms.layoutManager = LinearLayoutManager(context)

        // Add listeners
        binding.checkButton.setOnClickListener { confirm() }
        binding.dipperButton.setOnClickListener { toggleDipper() }
        binding.plusButton.setOnClickListener { addDetector() }
        binding.eyeButton.setOnClickListener { toggleVisibility() }
        binding.crossButton.setOnClickListener { cancel() }
    }

    fun hasWindowToken(): Boolean {
        return binding.root.windowToken != null
    }

    fun onDestroy() {
        windowManager.removeView(drawing)
        windowManager.removeView(binding.root)
    }

    private fun confirm() {
        val detectorList = detectorViews.map { detectorView -> Detector(detectorView) }
        clicker.detectors = detectorList.toTypedArray()
        cancel()
    }

    private fun toggleDipper() {
        dipping = !dipping
        if(dipping) {
            binding.dipperButton.setImageResource(R.drawable.dipper_on)
        } else {
            binding.dipperButton.setImageResource(R.drawable.dipper_off)
        }
    }

    private fun addDetector() {
        val detectorView = DetectorView(context, null)
        detectorView.startX = clickerCenter[0]
        detectorView.startY = clickerCenter[1]
        detectorView.invalidate()
        detectorViews += detectorView
        drawing.addView(detectorView)
        detectorAdapter.notifyItemInserted(detectorViews.size - 1)
    }

    private fun toggleVisibility() {
        visible = !visible
        layoutParams.alpha = if(visible) 1f else 0.1f
        windowManager.updateViewLayout(binding.root, layoutParams)
    }

    private fun cancel() {
        clickerViews.forEach { clickerView -> clickerView.visibility = View.VISIBLE }
        overlayMenu.visibility = View.VISIBLE
        onDestroy()
    }
}