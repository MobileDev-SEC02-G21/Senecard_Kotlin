package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerProfile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile.ActivityBusinessOwnerEditProfile
import com.mobiles.senecard.databinding.ActivityBusinessOwnerProfileBinding

class ActivityBusinessOwnerProfile : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerProfileBinding
    private val viewModel: ViewModelBusinessOwnerProfile by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        setupObservers()
    }

    private fun setupBinding() {
    }

    private fun setupObservers() {
        // Observe user data or other relevant data here

        // Navigation function
        viewModel.navigateTo.observe(this) { destination ->
            destination?.let {
                when (it) {
                    NavigationDestination.EDIT_PROFILE -> navigateToActivity(ActivityBusinessOwnerEditProfile::class.java)
                }
                viewModel.onNavigationHandled()
            }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

}
