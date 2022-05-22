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
import com.example.chromaclicker.databinding.OverlayMenuBinding
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
    private val binding = OverlayMenuBinding.inflate(LayoutInflater.from(context))
    private val autoClickIntent = Intent(context, AutoClickService::class.java)
    private val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    private var clickerViews = mutableListOf<ClickerView>()
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
        binding.saveButton.setOnClickListener { SavePopup(context, "", false, this::saveClickers) }
    }

    fun onDestroy() {
        clickerViews.forEach { clickerView -> clickerView.onDestroy(windowManager) }
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
        for(clickerView in clickerViews) {
            val layoutParams = clickerView.layoutParams
            layoutParams.width = 2 * toPixels(settings.circleRadius)
            layoutParams.height = 2 * toPixels(settings.circleRadius)
            windowManager.updateViewLayout(clickerView, layoutParams)
        }
    }

    private fun toggleAutoClicker() {
        if(AutoClickService.instance?.projection == null) {
            return
        }
        playing = !playing
        if(playing) {
            clickerViews.forEach { clickerView -> clickerView.visibility = View.INVISIBLE }
            binding.playButton.setImageResource(R.drawable.pause)
            autoClickIntent.putExtra("settings", settings)
            autoClickIntent.putParcelableArrayListExtra("clickers", clickers)
        } else {
            clickerViews.forEach { clickerView -> clickerView.visibility = View.VISIBLE }
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
        val clickerView = ClickerView(context, null)
        val clicker = Clicker(
            (clickerLayout.x + clickerLayout.width / 2).toFloat(),
            (clickerLayout.y + clickerLayout.height / 2).toFloat(),
            arrayOf()
        )

        clickerView.addListeners(windowManager, clicker, clickerLayout, clickerViews, binding.root)
        clickers += clicker
        clickerViews += clickerView
        windowManager.addView(clickerView, clickerLayout)
    }

    private fun addClicker(clicker: Clicker) {
        val radius = settings.circleRadius
        // Move top-left corner to clicker's center minus the radius
        val x = toDP(clicker.x.toInt()) - settings.circleRadius
        val y = toDP(clicker.y.toInt()) - settings.circleRadius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y, gravity=(Gravity.TOP or Gravity.START))
        val clickerView = ClickerView(context, null)
        clickerView.addListeners(windowManager, clicker, clickerLayout, clickerViews, binding.root)
        clickerViews += clickerView
        windowManager.addView(clickerView, clickerLayout)
    }

    private fun removeClicker() {
        if(clickerViews.size > 0) {
            windowManager.removeView(clickerViews[clickerViews.lastIndex])
            clickers.removeAt(clickerViews.lastIndex)
            clickerViews.removeAt(clickerViews.lastIndex)
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
