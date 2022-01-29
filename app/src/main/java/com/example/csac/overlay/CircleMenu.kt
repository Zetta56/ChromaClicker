package com.example.csac.overlay

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
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
    context: Context,
    private val windowManager: WindowManager,
    private val circles: MutableList<CircleView>,
    private val circleView: View,
    private val menuView: View,
    private val overlayCanvas: OverlayCanvasBinding,
) {

    private val binding = CircleMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(270, 60, focusable=true)
    private val paint = Paint()
    private val detectors = mutableListOf<DetectorParcel>()
    private val detectorAdapter = DetectorAdapter(detectors, overlayCanvas.surface)
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
        binding.crossButton.setOnClickListener { onDestroy(true) }
    }

    fun hasWindowToken(): Boolean {
        return binding.root.windowToken != null
    }

    fun onDestroy(showOverlay: Boolean = false) {
        if(showOverlay) {
            circles.forEach { circle -> circle.visibility = View.VISIBLE }
            menuView.visibility = View.VISIBLE
        }
        windowManager.removeView(overlayCanvas.root)
        windowManager.removeView(binding.root)
    }

    private fun confirm() {
        onDestroy(true)
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
        val detector = DetectorParcel(0, 0, "00ff00")
        paint.color = Color.parseColor("#${detector.color}")
        detectors.add(detector)
        detectorAdapter.notifyItemInserted(detectors.size - 1)
        if(layoutParams.height < (235 * Resources.getSystem().displayMetrics.density).toInt()) {
            layoutParams.height += (40 * Resources.getSystem().displayMetrics.density).toInt()
            windowManager.updateViewLayout(binding.root, layoutParams)
        }

        val canvas = overlayCanvas.surface.holder.lockCanvas()
        canvas.drawLine(circleView.x, circleView.y, (detector.x).toFloat(), (detector.y).toFloat(), paint)
        overlayCanvas.surface.holder.unlockCanvasAndPost(canvas)
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
}