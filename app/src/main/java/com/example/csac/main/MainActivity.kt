package com.example.csac.main


import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.csac.R
import com.example.csac.autoclick.AutoClickService


class MainActivity : AppCompatActivity() {
    lateinit var projectionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register callback for MainFragment's projection requests (registering here prevents
        // callback from being prematurely destroyed)
        projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(applicationContext, AutoClickService::class.java)
                val rect = Rect()
                window.decorView.getWindowVisibleDisplayFrame(rect)
                intent.action = "initialize"
                intent.putExtra("projectionResult", result)
                intent.putExtra("statusBarHeight", rect.top)
                startService(intent)
            }
        }
        setContentView(R.layout.activity_main)
    }
}