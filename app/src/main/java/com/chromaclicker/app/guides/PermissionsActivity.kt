package com.chromaclicker.app.guides

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chromaclicker.app.R
import com.chromaclicker.app.autoclick.AutoClickService
import com.chromaclicker.app.databinding.ActivityPermissionsBinding
import com.chromaclicker.app.main.PermissionPage
import com.chromaclicker.app.main.PermissionsIntro

/** This dialog checks app permissions and sets up its view pager. */
class PermissionsActivity : AppCompatActivity() {
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
        private val titles = listOf(R.string.permission_intro_title, R.string.overlay_permission, R.string.accessibility_permission, R.string.projection_permission)
        private val descriptions = listOf(R.string.permission_intro_description, R.string.overlay_description, R.string.accessibility_description, R.string.projection_description)

        override fun getItemCount(): Int {
            return titles.size
        }

        override fun createFragment(position: Int): Fragment {
            return if(position == 0) {
                PermissionsIntro()
            } else {
                PermissionPage(position, binding.root, titles[position], descriptions[position])
            }
        }
    }

    private lateinit var binding: ActivityPermissionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up view binding
        binding = ActivityPermissionsBinding.inflate(LayoutInflater.from(this))
        binding.pager.adapter = PagerAdapter(this as FragmentActivity)
        binding.pager.isUserInputEnabled = false
        setupNavigation()
        setContentView(binding.root)
    }

    /** Sets up the navigation buttons associated with the view pager */
    private fun setupNavigation() {
        binding.backButton.setOnClickListener {
            if(binding.pager.currentItem == 0) {
                finish()
            } else {
                binding.pager.currentItem -= 1
            }
        }
        binding.nextButton.setOnClickListener {
            if(binding.pager.currentItem == binding.pager.adapter!!.itemCount - 1) {
                finish()
            } else {
                binding.pager.currentItem += 1
            }
        }
    }
}