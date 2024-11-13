package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditLoyalty

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile.ViewModelBusinessOwnerEditProfile

class ActivityBusinessOwnerEditLoyalty : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerEditLoyalty
    private val viewModel: ViewModelBusinessOwnerEditLoyalty by viewModels()

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
    }

}
