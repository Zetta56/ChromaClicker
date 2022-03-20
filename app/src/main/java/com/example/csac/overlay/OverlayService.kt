package com.example.csac.overlay

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.EditText
import com.example.csac.*
import com.example.csac.autoclick.AutoClickService
import com.example.csac.autoclick.ProjectionRequester
import com.example.csac.models.Clicker
import com.example.csac.databinding.OverlayMenuBinding
import com.example.csac.models.Save
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.ArrayList

class OverlayService : Service() {

    private lateinit var binding: OverlayMenuBinding
    private lateinit var autoClickIntent: Intent
    private lateinit var windowManager: WindowManager
    private lateinit var clickers: ArrayList<Clicker>
    private var clickerViews = mutableListOf<ClickerView>()
    private var statusBarHeight = 0
    private var playing = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        clickers = intent?.extras!!.getParcelableArrayList("clickers")!!
        statusBarHeight = intent.extras!!.getInt("statusBarHeight")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        autoClickIntent = Intent(applicationContext, AutoClickService::class.java)
        addMenu()
        makeNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    // Don't bind this service to anything
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Destroy created views when this service is stopped
    override fun onDestroy() {
        windowManager.removeView(binding.root)
        clickerViews.forEach { clickerView -> clickerView.onDestroy(windowManager) }
        autoClickIntent.putExtra("enabled", false)
        applicationContext.startService(autoClickIntent)
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addMenu() {
        val layoutParams = createOverlayLayout(55, 220, gravity=Gravity.START)
        binding = OverlayMenuBinding.inflate(LayoutInflater.from(applicationContext))
        windowManager.addView(binding.root, layoutParams)

        // Add event listeners
        val draggable = Draggable(windowManager, layoutParams, binding.root)
        setRecursiveTouchListener(binding.root, draggable)
        binding.playButton.setOnClickListener { toggleAutoClicker() }
        binding.plusButton.setOnClickListener { addClicker() }
        binding.minusButton.setOnClickListener { removeClicker() }
        binding.saveButton.setOnClickListener { saveClickers("test") }
    }

    private fun toggleAutoClicker() {
        // If AutoClickService doesn't have a projection, request it from ProjectionActivity
        if(AutoClickService.instance?.projection == null) {
            ProjectionRequester.launch(applicationContext)
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
        autoClickIntent.action = "toggle"
        applicationContext.startService(autoClickIntent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addClicker() {
        val radius = 30
        val x = toDP(Resources.getSystem().displayMetrics.widthPixels / 2) - radius
        val y = toDP(Resources.getSystem().displayMetrics.heightPixels / 2) - radius
        val clickerLayout = createOverlayLayout(radius * 2, radius * 2, x=x, y=y, gravity=(Gravity.TOP or Gravity.START))
        val clickerView = ClickerView(applicationContext, null)
        val clicker = Clicker(
            (clickerLayout.x + clickerLayout.width / 2).toFloat(),
            (clickerLayout.y + clickerLayout.height / 2 + statusBarHeight).toFloat(),
            arrayOf()
        )

        clickerView.addListeners(windowManager, clicker, clickerLayout, clickerViews, binding.root, statusBarHeight)
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

//    private fun showSaveDialog() {
//        val dialog = AlertDialog.Builder(applicationContext)
//        val editText = EditText(applicationContext)
//        dialog.setView(editText)
//        dialog.setTitle("Save Name")
//        dialog.setPositiveButton("Save") { _, _ ->
//            var name = editText.text.toString()
//            var duplicate = File("${filesDir}/saves/${name}")
//            var num = 1
//            if(duplicate.exists()) {
//                do {
//                    num++
//                    duplicate = File("${filesDir}/saves/${name}-${num}")
//                } while(duplicate.exists())
//                name += "-${num}"
//            }
//            saveClickers(name)
//        }
//        dialog.setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss()}
//        dialog.show()
//    }

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

    private fun makeNotification() {
        if(Build.VERSION.SDK_INT >= 26) {
            // Create notification channel for foreground service
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "csac_overlay",
                getString(R.string.overlay_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)

            // Send notification
            val builder = Notification.Builder(this, "csac_overlay")
            startForeground(1, builder.build())
        }
    }
}