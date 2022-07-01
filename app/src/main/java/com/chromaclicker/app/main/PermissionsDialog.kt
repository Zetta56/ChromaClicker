package com.chromaclicker.app.main

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chromaclicker.app.R
import com.chromaclicker.app.autoclick.AutoClickService
import com.chromaclicker.app.databinding.DialogPermissionsBinding

/** This dialog checks app permissions and sets up its view pager. */
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

    /** Manages this layout's ViewPager2 */
    inner class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        // Initialize titles and descriptions for each tutorial page
        private val titles = listOf(R.string.overlay_permission, R.string.accessibility_permission, R.string.projection_permission)
        private val descriptions = listOf(R.string.overlay_description, R.string.accessibility_description, R.string.projection_description)

        override fun getItemCount(): Int {
            return titles.size
        }

        override fun createFragment(position: Int): Fragment {
            return PermissionPage(position, binding.nextButton, titles[position], descriptions[position])
        }
    }

    private lateinit var binding: DialogPermissionsBinding
    private lateinit var activity: Activity
    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity = requireActivity()
        // Set up view binding
        binding = DialogPermissionsBinding.inflate(LayoutInflater.from(activity))
        binding.pager.adapter = PagerAdapter(activity as FragmentActivity)
        binding.pager.isUserInputEnabled = false
        binding.pager.currentItem = getFirstRejection()
        setupNavigation()

        // Customize dialog
        val builder = AlertDialog.Builder(activity, R.style.PermissionsDialog)
        builder.setTitle("Permissions")
        builder.setView(binding.root)
        return builder.create()
    }

    /** Sets up the navigation buttons associated with the view pager */
    private fun setupNavigation() {
        binding.backButton.setOnClickListener {
            if(binding.pager.currentItem == 0) {
                dismiss()
            } else {
                binding.pager.currentItem -= 1
            }
        }
        binding.nextButton.setOnClickListener {
            if(binding.pager.currentItem == binding.pager.adapter!!.itemCount - 1) {
                dismiss()
            } else {
                binding.pager.currentItem += 1
            }
        }
    }

    /**
     * Gets the index of the first rejected permission. This is useful for skipping permission
     * screens for already-obtained permissions.
     */
    private fun getFirstRejection(): Int {
        return when {
            !Settings.canDrawOverlays(activity) -> 0
            Settings.Secure.getInt(activity.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 0 -> 1
            AutoClickService.instance?.projection == null -> 2
            else -> -1
        }
    }
}