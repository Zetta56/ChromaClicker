package com.chromaclicker.app.guides

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chromaclicker.app.R
import com.chromaclicker.app.autoclick.AutoClickService
import com.chromaclicker.app.databinding.FragmentPermissionPageBinding

class PermissionsIntro : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permission_intro, container, false)
    }
}

/**
 * This fragment displays each individual page, including its [title] and [description], in the
 * permissions activity and modifies its [container]'s navigation buttons accordingly. This also
 * requests the permission associated with its [page index][position].
 */
class PermissionPage(
    private val position: Int,
    private val container: ViewGroup,
    private val title: Int,
    private val description: Int
) : Fragment() {

    companion object {
        // Constants represent the permissions associated with each dialog page
        const val OVERLAY = 1
        const val ACCESSIBILITY = 2
        const val PROJECTION = 3
    }

    private lateinit var activity: Activity
    private lateinit var binding: FragmentPermissionPageBinding
    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private val nextButton = container.findViewById<Button>(R.id.nextButton)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = requireActivity()
        projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            setupAutoClicker(result)
        }
        // Configure general layout
        binding = FragmentPermissionPageBinding.inflate(LayoutInflater.from(context))
        binding.title.setText(title)
        binding.description.setText(description)
        binding.description.movementMethod = LinkMovementMethod.getInstance()
        binding.agreeButton.setOnClickListener { requestPermission() }
        binding.disagreeButton.setOnClickListener { (context as Activity).finish() }
        displayConsent()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        displayConsent()
    }

    /**
     * Initializes [AutoClickService] with a projection and status bar height. The projection must
     * come from the [result] of an accepted screen capture intent
     */
    private fun setupAutoClicker(result: ActivityResult) {
        val intent = Intent(activity, AutoClickService::class.java)
        if(result.resultCode == Activity.RESULT_OK) {
            intent.action = "setup"
            intent.putExtra("projectionResult", result)
            // Get the status bar's height
            val rect = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(rect)
            intent.putExtra("statusBarHeight", rect.top)
        } else {
            intent.action = "remove_projection"
        }
        activity.startService(intent)
        // Reload buttons after a delay, compensating for time needed to load the projection
        Handler(Looper.getMainLooper()).postDelayed({ displayConsent() }, 500)
    }

    /**
     * Updates the request button's appearance to match the current permission's approval state.
     * This also enables the "Next" button when the user consents and disables it otherwise.
     */
    private fun displayConsent() {
        if(hasPermission()) {
            binding.agreeButton.setText(R.string.granted)
            binding.agreeButton.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.green)
            binding.disagreeButton.visibility = View.GONE
        } else {
            binding.agreeButton.setText(R.string.agree)
            binding.agreeButton.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.dark_blue)
            binding.disagreeButton.visibility = View.VISIBLE
        }
        nextButton.isEnabled = hasPermission()
    }

    /**
     * Returns whether the permission associated with this page has been given */
    private fun hasPermission(): Boolean {
        return when(position) {
            OVERLAY -> Settings.canDrawOverlays(activity)
            ACCESSIBILITY -> (Settings.Secure.getInt(activity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1)
            PROJECTION -> (AutoClickService.instance?.projection != null)
            else -> true
        }
    }

    /** Requests this page's associated permission */
    private fun requestPermission() {
        when(position) {
            OVERLAY -> {
                val uri = Uri.parse("package:${activity.packageName}")
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                activity.startActivity(intent)
            }
            ACCESSIBILITY -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                activity.startActivity(intent)
            }
            PROJECTION -> {
                val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
            }
        }
    }
}