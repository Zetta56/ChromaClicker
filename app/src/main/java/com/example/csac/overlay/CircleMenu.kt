package com.example.csac.overlay

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csac.R
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.CircleMenuBinding
import com.example.csac.databinding.OverlayCanvasBinding
import com.example.csac.models.DetectorParcel

class CircleMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val circles: MutableList<CircleView>,
    private val circleCenter: FloatArray,
    private val menuView: View,
    private val overlayCanvas: OverlayCanvasBinding,
) {

    private val binding = CircleMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(270, 60, focusable=true)
//    private val paint = Paint()
    private val detectorParcels = mutableListOf<DetectorParcel>()
    private val detectors = mutableListOf<DetectorView>()
    private val detectorAdapter = DetectorAdapter(detectors, detectorParcels)
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
        windowManager.removeView(overlayCanvas.root)
        windowManager.removeView(binding.root)
    }

    private fun confirm() {
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
        val detector = DetectorView(context, null)
        detector.startX = circleCenter[0]
        detector.startY = circleCenter[1]
        detector.invalidate()
        detectors += detector
        overlayCanvas.root.addView(detector)

        detectorAdapter.notifyItemInserted(detectors.size - 1)
        if(layoutParams.height < (235 * Resources.getSystem().displayMetrics.density).toInt()) {
            layoutParams.height += (40 * Resources.getSystem().displayMetrics.density).toInt()
            windowManager.updateViewLayout(binding.root, layoutParams)
        }
    }

    private fun toggleVisibility() {
        visible = !visible
        if(visible) {
            layoutParams.alpha = 1f
        } else {
            layoutParams.alpha = 0.1f
        }
        windowManager.updateViewLayout(binding.root, layoutParams)
    }

    private fun cancel() {
        circles.forEach { circle -> circle.visibility = View.VISIBLE }
        menuView.visibility = View.VISIBLE
        onDestroy()
    }
}