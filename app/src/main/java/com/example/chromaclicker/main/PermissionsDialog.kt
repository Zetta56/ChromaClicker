package com.example.chromaclicker.main

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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.chromaclicker.R
import com.example.chromaclicker.autoclick.AutoClickService
import com.example.chromaclicker.databinding.DialogPermissionsBinding

/** This dialog checks app permissions and requests missing permissions. */
class PermissionsDialog : DialogFragment() {
    companion object {
        /** Returns whether user has all permissions needed to enable the overlay. */
        fun hasPermissions(activity: Activity): Boolean {
            return (
                // Check overlay permission
                Settings.canDrawOverlays(activity) &&
                // Check accessibility permission
                Settings.Secure.getInt(activity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1 &&
                // Check media projection permission
                AutoClickService.instance?.projection != null
            )
        }
    }

    private lateinit var binding: DialogPermissionsBinding
    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var activity: Activity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity = requireActivity()
        binding = DialogPermissionsBinding.inflate(LayoutInflater.from(activity))
        // Runs after user interacts with projection dialog
        projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            initializeAutoClicker(result)
        }
        setClickListeners()

        // Configure alert dialog settings
        val builder = AlertDialog.Builder(activity, R.style.PermissionsDialog)
        builder.setTitle("Permissions")
        builder.setView(binding.root)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        loadButtons()
    }

    /**
     * Initializes [AutoClickService] with a projection and status bar height. The projection must
     * come from the [result] of an accepted screen capture intent
     */
    private fun initializeAutoClicker(result: ActivityResult) {
        if(result.resultCode == Activity.RESULT_OK) {
            val intent = Intent(activity, AutoClickService::class.java)
            // Get the status bar's height
            val rect = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(rect)
            // Configure intent
            intent.action = "initialize"
            intent.putExtra("statusBarHeight", rect.top)
            intent.putExtra("projectionResult", result)
            activity.startService(intent)

            // Reload buttons after a delay, compensating for time needed to load the projection
            Handler(Looper.getMainLooper()).postDelayed({
                loadButtons()
            }, 500)
        }
    }

    /** Sets each button's click listeners */
    private fun setClickListeners() {
        // Launch this app's overlay permission screen
        binding.overlayButton.setOnClickListener {
            val uri = Uri.parse("package:${activity.packageName}")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
            activity.applicationContext.startActivity(intent)
        }
        // Launch this app's accessibility screen
        binding.accessibilityButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.applicationContext.startActivity(intent)
        }
        // Launch a dialog asking for a media projection
        binding.projectionButton.setOnClickListener {
            val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        }
    }

    /**
     * Updates all the buttons based on this app's current permissions. If all permission have
     * been granted, this will dismiss the entire dialog.
     */
    private fun loadButtons() {
        val accessibilityGranted = Settings.Secure.getInt(activity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1
        loadButton(binding.overlayButton, Settings.canDrawOverlays(context))
        loadButton(binding.accessibilityButton, accessibilityGranted)
        loadButton(binding.projectionButton, AutoClickService.instance?.projection != null, accessibilityGranted)

        if(hasPermissions(activity)) {
            this.dismiss()
        }
    }

    /**
     * Updates [button] image, color, and interactivity, based on whether its associated
     * [permission] and [prerequisites] have been granted
     */
    private fun loadButton(button: ImageButton, permission: Boolean, prerequisites: Boolean = true) {
        // If unfulfilled prerequisites, show a greyed-out cross
        if(!prerequisites) {
            button.setImageResource(R.drawable.cross)
            binding.projectionButton.setColorFilter(Color.parseColor("#888888"))
            button.isClickable = false
        // If unfulfilled permission, show a red cross
        } else if(!permission) {
            button.setImageResource(R.drawable.cross)
            binding.projectionButton.colorFilter = null
            button.isClickable = true
        // If fulfilled permission, show a green check
        } else {
            button.setImageResource(R.drawable.check)
            binding.projectionButton.colorFilter = null
            button.isClickable = false
        }
    }
}