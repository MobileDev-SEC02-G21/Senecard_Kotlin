package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class ActivityBusinessOwnerEditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerEditProfile
    private val viewModel: ViewModelBusinessOwnerEditProfile by viewModels()

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
