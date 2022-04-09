package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.csac.autoclick.AutoClickService
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.ClickerMenuBinding
import com.example.csac.models.Clicker
import com.example.csac.models.Detector

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

    private val binding = ClickerMenuBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(focusable=true)
    private val detectorViews = convertDetectors(clicker.detectors)
    private val detectorAdapter = DetectorAdapter(context, detectorViews, binding.root)
    private var dipping = false
    private var visible = true

    init {
        windowManager.addView(binding.root, layoutParams)
        detectorViews.forEach { view -> binding.root.addView(view, 0) }
        binding.detectorForms.adapter = detectorAdapter
        binding.detectorForms.layoutManager = LinearLayoutManager(context)
        binding.clicker.x = position[0]
        binding.clicker.y = position[1]

        // Add listeners
        binding.checkButton.setOnClickListener { confirm() }
        binding.dipperButton.setOnClickListener { toggleDipper() }
        binding.plusButton.setOnClickListener { addDetector() }
        binding.eyeButton.setOnClickListener { toggleVisibility() }
        binding.crossButton.setOnClickListener { cancel() }
        binding.root.setOnTouchListener { _, event -> dip(event) }
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

    private fun toggleDipper() {
        if(AutoClickService.instance?.projection != null) {
            dipping = true
            binding.menu.visibility = View.INVISIBLE
        }
    }

    private fun dip(event: MotionEvent): Boolean {
        if(dipping) {
            val intent = Intent(context, AutoClickService::class.java)
            // This starts from top of app (below status bar)
            intent.putExtra("x", event.x.toInt())
            intent.putExtra("y", event.y.toInt())
            intent.action = "get_pixel_color"
            context.startService(intent)
            dipping = false
            binding.menu.visibility = View.VISIBLE
        }
        return dipping
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

    private fun toggleVisibility() {
        visible = !visible
        binding.menu.alpha = if(visible) 1f else 0.1f
        windowManager.updateViewLayout(binding.root, layoutParams)
    }

    private fun cancel() {
        clickerViews.forEach { clickerView -> clickerView.visibility = View.VISIBLE }
        overlayMenu.visibility = View.VISIBLE
        onDestroy()
    }
}