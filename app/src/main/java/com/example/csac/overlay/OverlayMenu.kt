package com.example.csac.overlay

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.*
import com.example.csac.*
import com.example.csac.autoclick.AutoClickService
import com.example.csac.models.Clicker
import com.example.csac.databinding.OverlayMenuBinding
import com.example.csac.models.Save
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.ArrayList

class OverlayMenu(
    private val context: Context,
    private val clickers: ArrayList<Clicker>,
    private val clickerViews: MutableList<ClickerView>,
    private val autoClickIntent: Intent,
    private val statusBarHeight: Int
) {

    private val binding: OverlayMenuBinding
    private val windowManager: WindowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    private var playing = false

    init {
        val width = 55
        val y = toDP(Resources.getSystem().displayMetrics.heightPixels / 2) - 2 * width
        val layoutParams = createOverlayLayout(width, 4 * width, y=y, gravity=(Gravity.START or Gravity.TOP))
        binding = OverlayMenuBinding.inflate(LayoutInflater.from(context))
        windowManager.addView(binding.root, layoutParams)

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        setRecursiveTouchListener(binding.root, draggable)
        binding.playButton.setOnClickListener { toggleAutoClicker() }
        binding.plusButton.setOnClickListener { addClicker() }
        binding.minusButton.setOnClickListener { removeClicker() }
        binding.saveButton.setOnClickListener { SavePopup(context, this::saveClickers) }
    }

    fun onDestroy() {
        windowManager.removeView(binding.root)
    }

    private fun toggleAutoClicker() {
        if(AutoClickService.instance?.projection == null) {
            return
        }
        playing = !playing
        if(playing) {
            clickerViews.forEach { clickerView -> clickerView.visibility = View.INVISIBLE }
            binding.playButton.setImageResource(R.drawable.pause)
            autoClickIntent.putParcelableArrayListExtra("clickers", clickers)
            autoClickIntent.putExtra("statusBarHeight", statusBarHeight)
        } else {
            clickerViews.forEach { clickerView -> clickerView.visibility = View.VISIBLE }
            binding.playButton.setImageResource(R.drawable.play)
        }
        autoClickIntent.putExtra("enabled", playing)
        autoClickIntent.action = "toggle_clicker"
        context.startService(autoClickIntent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addClicker() {
        val radius = 30
        val x = toDP(Resources.getSystem().displayMetrics.widthPixels / 2) - radius
        val y = toDP(Resources.getSystem().displayMetrics.heightPixels / 2) - radius
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
        val file = File(savesDir, name)
        // Store serializable saves as JSON
        val saveJson = Json.encodeToString(Save.serializer(), save)
        file.writeText(saveJson)
    }
}