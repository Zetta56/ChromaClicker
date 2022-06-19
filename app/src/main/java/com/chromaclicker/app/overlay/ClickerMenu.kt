package com.chromaclicker.app.overlay

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.chromaclicker.app.R
import com.chromaclicker.app.createOverlayLayout
import com.chromaclicker.app.databinding.MenuClickerBinding
import com.chromaclicker.app.models.Clicker
import com.chromaclicker.app.models.Detector
import com.chromaclicker.app.setRecursiveTouchListener

/**
 * This class creates a clicker menu from a [context] object and a [windowManager], allowing you
 * to configure an individual [clicker]. This will also draw a copy of the clicker's circle,
 * using its top-left [position] and its [clickerCenter].
 *
 * This will call [onCancel] when the user either clicks on the cancel button or confirm button.
 */
class ClickerMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val clicker: Clicker,
    private val position: List<Float>,
    private val clickerCenter: Array<Float>,
    private val onCancel: () -> Unit
) {

    private var menuTransparent = false
    private val binding = MenuClickerBinding.inflate(LayoutInflater.from(context))
    private val layoutParams = createOverlayLayout(focusable=true)
    private val lines = convertDetectors(clicker.detectors)
    private val detectorAdapter = DetectorAdapter(context, lines, binding.root, clickerCenter)

    init {
        // Add an animation to this menu
        layoutParams.windowAnimations = android.R.style.Animation_Toast
        windowManager.addView(binding.root, layoutParams)
        // Add detector lines to container
        lines.forEach { line -> binding.root.addView(line, 0) }
        // Add dimming effect
        binding.root.setBackgroundColor(context.getColor(R.color.dim))
        // Initialize the RecyclerView for detectors
        binding.detectorForms.adapter = detectorAdapter
        binding.detectorForms.layoutManager = LinearLayoutManager(context)
        // Position the circle
        binding.circle.x = position[0]
        binding.circle.y = position[1]
        addListeners()
    }

    /** Returns whether this layout was added to the window manager */
    fun hasWindowToken(): Boolean {
        return binding.root.windowToken != null
    }

    /** Destroys this menu */
    fun onDestroy() {
        windowManager.removeView(binding.root)
    }

    /**
     * Returns a list of lines with colors and endpoints matching those of the desired [detectors].
     * The starting points for these lines are at the [clickerCenter].
     */
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

    /** Adds click and touch listeners to this menu's view */
    private fun addListeners() {
        binding.confirmButton.setOnClickListener { confirm() }
        binding.cancelButton.setOnClickListener {
            onCancel()
            onDestroy()
        }
        // When the user clicks on the container, make this menu transparent
        binding.root.setOnClickListener { toggleTransparency(true) }
        // Make menu clickable to stop click events from propagating up to the container
        binding.menu.isClickable = true
        // When the user clicks on the menu, make this menu translucent
        setRecursiveTouchListener(binding.menu) { view, event ->
            if(event.action == MotionEvent.ACTION_UP) {
                view.performClick()
            }
            // Returns whether this touch event was handled
            if (menuTransparent) toggleTransparency(false) else false
        }
    }

    /** Sets the [clicker]'s detectors to an array of detectors, converted from [lines]. */
    private fun confirm() {
        val detectorList = lines.map { line -> Detector(line) }
        clicker.detectors = detectorList.toTypedArray()
        onCancel()
        onDestroy()
    }

    /** [Toggles][toggle] whether this menu is transparent or translucent */
    private fun toggleTransparency(toggle: Boolean): Boolean {
        menuTransparent = toggle
        binding.menu.alpha = if(menuTransparent) 0.1f else 1f
        windowManager.updateViewLayout(binding.root, layoutParams)
        return true
    }
}