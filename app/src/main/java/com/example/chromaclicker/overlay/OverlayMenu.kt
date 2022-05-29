package com.example.chromaclicker.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.chromaclicker.*
import com.example.chromaclicker.autoclick.AutoClickService
import com.example.chromaclicker.databinding.MenuOverlayBinding
import com.example.chromaclicker.models.AppSettings
import com.example.chromaclicker.models.Clicker
import com.example.chromaclicker.models.Save
import com.google.gson.Gson
import java.io.File

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
        // Dimensions for this menu, not clickers
        val width = 55
        val y = getScreenHeight("dp") / 2 - 2 * width
        val layoutParams = createOverlayLayout(width, 4 * width, y=y, gravity=(Gravity.START or Gravity.TOP))
        windowManager.addView(binding.root, layoutParams)
        clickers.forEach { clicker -> addClicker(clicker) }

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        setRecursiveTouchListener(binding.root, draggable)
        binding.playButton.setOnClickListener { toggleAutoClicker() }
        binding.plusButton.setOnClickListener { addClicker() }
        binding.minusButton.setOnClickListener { removeClicker() }
        binding.saveButton.setOnClickListener { SaveDialog(context, "", false, this::saveClickers) }
    }

    fun onDestroy() {
        circles.forEach { circle -> circle.onDestroy(windowManager) }
        windowManager.removeView(binding.root)
        autoClickIntent.putExtra("enabled", false)
        context.startService(autoClickIntent)
    }

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        if(playing) {
            toggleAutoClicker()
            toggleAutoClicker()
        }
        for(circle in circles) {
            val layoutParams = circle.layoutParams
            layoutParams.width = 2 * toPixels(settings.circleRadius)
            layoutParams.height = 2 * toPixels(settings.circleRadius)
            windowManager.updateViewLayout(circle, layoutParams)
        }
    }

    private fun toggleAutoClicker() {
        if(AutoClickService.instance?.projection == null) {
            return
        }
        playing = !playing
        if(playing) {
            circles.forEach { circle -> circle.visibility = View.INVISIBLE }
            binding.playButton.setImageResource(R.drawable.pause)
            autoClickIntent.putExtra("settings", settings)
            autoClickIntent.putParcelableArrayListExtra("clickers", clickers)
        } else {
            circles.forEach { circle -> circle.visibility = View.VISIBLE }
            binding.playButton.setImageResource(R.drawable.play)
        }
        autoClickIntent.putExtra("enabled", playing)
        autoClickIntent.action = "toggle_clicker"
        context.startService(autoClickIntent)
    }

    private fun addClicker() {
        val radius = settings.circleRadius
        val x = getScreenWidth("dp") / 2 - radius
        val y = getScreenHeight("dp") / 2 - radius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y, gravity=(Gravity.TOP or Gravity.START))
        val circle = Circle(context, null)
        val clicker = Clicker(
            (clickerLayout.x + clickerLayout.width / 2).toFloat(),
            (clickerLayout.y + clickerLayout.height / 2).toFloat(),
            arrayOf()
        )

        circle.addListeners(windowManager, clicker, clickerLayout, circles, binding.root)
        clickers += clicker
        circles += circle
        windowManager.addView(circle, clickerLayout)
    }

    private fun addClicker(clicker: Clicker) {
        val radius = settings.circleRadius
        // Move top-left corner to clicker's center minus the radius
        val x = toDP(clicker.x.toInt()) - settings.circleRadius
        val y = toDP(clicker.y.toInt()) - settings.circleRadius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y, gravity=(Gravity.TOP or Gravity.START))
        val circle = Circle(context, null)
        circle.addListeners(windowManager, clicker, clickerLayout, circles, binding.root)
        circles += circle
        windowManager.addView(circle, clickerLayout)
    }

    private fun removeClicker() {
        if(circles.size > 0) {
            windowManager.removeView(circles[circles.lastIndex])
            clickers.removeAt(clickers.lastIndex)
            circles.removeAt(circles.lastIndex)
        }
    }

    private fun saveClickers(name: String) {
        // Make saves directory in internal storage if it doesn't exist
        val savesDir = File("${context.filesDir}/saves")
        if(!savesDir.exists()) {
            savesDir.mkdirs()
        }

        val save = Save(name, clickers)
        val json = Gson().toJson(save)
        val file = File(savesDir, name)
        file.writeText(json)
    }
}
