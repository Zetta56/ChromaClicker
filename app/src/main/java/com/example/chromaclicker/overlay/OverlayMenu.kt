package com.example.chromaclicker.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.SensorManager
import android.view.*
import com.example.chromaclicker.*
import com.example.chromaclicker.autoclick.AutoClickService
import com.example.chromaclicker.databinding.MenuOverlayBinding
import com.example.chromaclicker.models.AppSettings
import com.example.chromaclicker.models.Clicker
import com.example.chromaclicker.models.Save
import com.google.gson.Gson
import java.io.File

/**
 * This class creates an overlay menu from a [context] object, allowing users to interact with
 * clickers and toggle the auto-click service. You can load this with a preset list of [clickers]
 * and configure this with a [settings] object.
 */
class OverlayMenu(
    private val context: Context,
    private var settings: AppSettings,
    private val clickers: ArrayList<Clicker>
) {
    private val binding = MenuOverlayBinding.inflate(LayoutInflater.from(context))
    private val autoClickIntent = Intent(context, AutoClickService::class.java)
    private val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    private var circles = mutableListOf<Circle>()
    private var playing = false

    init {
        // Dimensions and position for this menu
        val width = 55
        val y = getScreenHeight("dp") / 2 - 2 * width
        val layoutParams = createOverlayLayout(width, 4 * width, y=y, gravity=(Gravity.START or Gravity.TOP))
        windowManager.addView(binding.root, layoutParams)
        // Add an active clicker for each clicker model loaded from a save
        clickers.forEach { clicker -> addCircle(clicker) }

        // Make menu draggable
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        setRecursiveTouchListener(binding.root, draggable)
        // Add button listeners
        binding.playButton.setOnClickListener { toggleAutoClicker() }
        binding.plusButton.setOnClickListener { addCircle() }
        binding.minusButton.setOnClickListener { removeCircle() }
        binding.saveButton.setOnClickListener { SaveDialog(context, "", false, this::saveClickers) }
    }

    /** Destroys this menu's views and disables its associated services. */
    fun onDestroy() {
        circles.forEach { circle -> circle.onDestroy(windowManager) }
        windowManager.removeView(binding.root)
        // Disables AutoClickService
        autoClickIntent.putExtra("enabled", false)
        context.startService(autoClickIntent)
    }

    /** Reloads this menu's circles and auto-clicker with the [new settings][newSettings] in mind. */
    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        // Restart auto clicker
        if(playing) {
            toggleAutoClicker()
            toggleAutoClicker()
        }
        // Edit dimensions for each circle if their radii were changed
        for(circle in circles) {
            val layoutParams = circle.layoutParams
            layoutParams.width = 2 * toPixels(settings.circleRadius)
            layoutParams.height = 2 * toPixels(settings.circleRadius)
            windowManager.updateViewLayout(circle, layoutParams)
        }
    }

    /**
     * Toggles the auto-clicker if the auto-clicker was initialized with a projection. This also
     * toggles the visibility of all circles on screen.
     */
    private fun toggleAutoClicker() {
        // Stop if the auto-clicker wasn't properly initialized
        if(AutoClickService.instance?.projection == null) {
            return
        }
        playing = !playing
        if(playing) {
            // Hide all circles and start auto-clicker with necessary extras
            circles.forEach { circle -> circle.visibility = View.INVISIBLE }
            binding.playButton.setImageResource(R.drawable.pause)
            autoClickIntent.putExtra("settings", settings)
            autoClickIntent.putParcelableArrayListExtra("clickers", clickers)
        } else {
            // Show all circles and stop auto-clicker
            circles.forEach { circle -> circle.visibility = View.VISIBLE }
            binding.playButton.setImageResource(R.drawable.play)
        }
        // Label and launch auto-clicker intent
        autoClickIntent.putExtra("enabled", playing)
        autoClickIntent.action = "toggle_clicker"
        context.startService(autoClickIntent)
    }

    /**
     * Creates a circle view and its associated clicker. These will be added to [clickers] and
     * [circles], respectively.
     */
    private fun addCircle() {
        // Create a circle at the center of the screen
        val radius = settings.circleRadius
        val x = getScreenWidth("dp") / 2 - radius
        val y = getScreenHeight("dp") / 2 - radius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y,
            gravity=(Gravity.TOP or Gravity.START))
        val circle = Circle(context, null)
        // Add an associated clicker for the circle
        val clicker = Clicker(
            (clickerLayout.x + clickerLayout.width / 2).toFloat(),
            (clickerLayout.y + clickerLayout.height / 2).toFloat(),
            arrayOf()
        )
        // Add circle logic
        circle.addListeners(windowManager, clicker, clickerLayout, circles, binding.root)
        // Append clicker and circle to existing arrays
        clickers += clicker
        circles += circle
        windowManager.addView(circle, clickerLayout)
    }

    /** Creates a new circle based on your [clicker]'s coordinates and adds it to [circles] */
    private fun addCircle(clicker: Clicker) {
        val radius = settings.circleRadius
        // Get the top-left corner by subtracting radius from clicker's center
        val x = toDP(clicker.x.toInt()) - settings.circleRadius
        val y = toDP(clicker.y.toInt()) - settings.circleRadius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y,
            gravity=(Gravity.TOP or Gravity.START))
        val circle = Circle(context, null)
        circle.addListeners(windowManager, clicker, clickerLayout, circles, binding.root)
        circles += circle
        windowManager.addView(circle, clickerLayout)
    }

    /** Removes the latest circle from [circles], as well as its associated clicker */
    private fun removeCircle() {
        // Stops if there are no circles
        if(circles.size > 0) {
            windowManager.removeView(circles[circles.lastIndex])
            clickers.removeAt(clickers.lastIndex)
            circles.removeAt(circles.lastIndex)
        }
    }

    /** Saves your current [clickers] into a JSON file with a desired [name]. */
    private fun saveClickers(name: String) {
        // Make saves directory in internal storage if it doesn't exist
        val savesDir = File("${context.filesDir}/saves")
        if(!savesDir.exists()) {
            savesDir.mkdirs()
        }
        // Save clickers as a json file
        val save = Save(name, clickers)
        val json = Gson().toJson(save)
        val file = File(savesDir, name)
        file.writeText(json)
    }
}
