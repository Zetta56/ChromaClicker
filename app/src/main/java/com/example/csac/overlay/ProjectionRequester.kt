package com.example.csac.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.csac.AutoClickService

class ProjectionRequester : AppCompatActivity() {
    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ProjectionRequester::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(applicationContext, AutoClickService::class.java)
                intent.action = "send_projection"
                intent.putExtra("projectionResult", result)
                startService(intent)
                this.finish()
            }
        }
        activityLauncher.launch(projectionManager.createScreenCaptureIntent())
    }
}