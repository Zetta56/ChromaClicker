package com.example.csac.overlay

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.example.csac.R
import com.example.csac.autoclick.AutoClickService
import com.example.csac.createOverlayLayout
import com.example.csac.databinding.OverlayMenuBinding
import com.example.csac.main.MainActivity
import com.example.csac.models.Clicker
import com.example.csac.models.Save
import com.example.csac.setRecursiveTouchListener
import com.example.csac.toDP
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.ArrayList

class OverlayActivity : AppCompatActivity() {

    private lateinit var binding: OverlayMenuBinding
    private lateinit var autoClickIntent: Intent
    private lateinit var clickers: ArrayList<Clicker>
    private var clickerViews = mutableListOf<ClickerView>()
    private var playing = false
    private var statusBarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarHeight = intent.extras!!.getInt("statusBarHeight")
        clickers = intent?.extras!!.getParcelableArrayList("clickers")!!
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        autoClickIntent = Intent(applicationContext, AutoClickService::class.java)
        addMenu()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent?.action == "finish") {
            finish()
        }
    }

    override fun onDestroy() {
        clickerViews.forEach { clickerView -> clickerView.onDestroy(windowManager) }
        windowManager.removeView(binding.root)
        autoClickIntent.putExtra("enabled", false)
        applicationContext.startService(autoClickIntent)
        super.onDestroy()
    }

    override fun onBackPressed() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun addMenu() {
        val width = 55
        val y = toDP(Resources.getSystem().displayMetrics.heightPixels / 2) - 2 * width
        val layoutParams = createOverlayLayout(width, 4 * width, y=y, gravity=(Gravity.START or Gravity.TOP))
        binding = OverlayMenuBinding.inflate(LayoutInflater.from(this))
        windowManager.addView(binding.root, layoutParams)

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        setRecursiveTouchListener(binding.root, draggable)
        binding.playButton.setOnClickListener { toggleAutoClicker() }
        binding.plusButton.setOnClickListener { addClicker() }
        binding.minusButton.setOnClickListener { removeClicker() }
        binding.saveButton.setOnClickListener { SavePopup(this, this::saveClickers) }
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
        startService(autoClickIntent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addClicker() {
        val radius = 30
        val x = toDP(Resources.getSystem().displayMetrics.widthPixels / 2) - radius
        val y = toDP(Resources.getSystem().displayMetrics.heightPixels / 2) - radius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y, gravity=(Gravity.TOP or Gravity.START))
        val clickerView = ClickerView(this, null)
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
        val savesDir = File("${filesDir}/saves")
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