package com.example.csac.main


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.csac.R
import com.example.csac.autoclick.AutoClickService


class MainActivity : AppCompatActivity() {
    lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    var overlayVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register callback for projection requests from MainFragment (registering here prevents callback from being destroyed)
        projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(applicationContext, AutoClickService::class.java)
                intent.action = "receive_projection"
                intent.putExtra("projectionResult", result)
                startService(intent)
            }
        }
        setContentView(R.layout.activity_main)
    }
}