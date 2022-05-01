package com.example.csac.main

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.csac.R
import com.example.csac.autoclick.AutoClickService
import com.example.csac.databinding.PermissionsDialogBinding

class PermissionsDialog : DialogFragment() {

    companion object {
        fun hasPermissions(activity: Activity): Boolean {
            return (
                Settings.canDrawOverlays(activity) &&
                Settings.Secure.getInt(activity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1 &&
                AutoClickService.instance?.projection != null
            )
        }
    }

    private lateinit var binding: PermissionsDialogBinding
    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var activity: Activity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity = requireActivity()
        projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(activity, AutoClickService::class.java)
                val rect = Rect()
                activity.window.decorView.getWindowVisibleDisplayFrame(rect)
                intent.action = "initialize"
                intent.putExtra("projectionResult", result)
                intent.putExtra("statusBarHeight", rect.top)
                activity.startService(intent)

                // Reload buttons after a delay, compensating for time needed to load the projection
                Handler(Looper.getMainLooper()).postDelayed({
                    loadButtons()
                }, 500)
            }
        }
        binding = PermissionsDialogBinding.inflate(LayoutInflater.from(activity))
        setClickListeners()

        val builder = AlertDialog.Builder(activity, R.style.permissionsDialog)
        builder.setTitle("Permissions")
        builder.setView(binding.root)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        loadButtons()
    }

    private fun setClickListeners() {
        binding.overlayButton.setOnClickListener {
            val uri = Uri.parse("package:${activity.packageName}")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
            activity.applicationContext.startActivity(intent)
        }
        binding.accessibilityButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.applicationContext.startActivity(intent)
        }
        binding.projectionButton.setOnClickListener {
            val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        }
    }

    private fun loadButtons() {
        val accessibilityGranted = Settings.Secure.getInt(activity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1
        loadButton(binding.overlayButton, Settings.canDrawOverlays(context))
        loadButton(binding.accessibilityButton, accessibilityGranted)
        loadButton(binding.projectionButton, AutoClickService.instance?.projection != null, accessibilityGranted)

        if(hasPermissions(activity)) {
            this.dismiss()
        }
    }

    private fun loadButton(button: ImageButton, granted: Boolean, prerequisites: Boolean = true) {
        if(!prerequisites) {
            button.setImageResource(R.drawable.cross)
            binding.projectionButton.setColorFilter(Color.parseColor("#888888"))
            button.isClickable = false
        } else if(!granted) {
            button.setImageResource(R.drawable.cross)
            binding.projectionButton.colorFilter = null
            button.isClickable = true
        } else {
            button.setImageResource(R.drawable.check)
            binding.projectionButton.colorFilter = null
            button.isClickable = false
        }
    }
}