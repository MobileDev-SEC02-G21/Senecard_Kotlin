package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner.ActivityBusinessOwnerQRScanner
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements.ActivityBusinessOwnerAdvertisements
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityBusinessOwnerLandingPageBinding

class ActivityBusinessOwnerProfile : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerLandingPageBinding
    private val viewModel: ViewModelBusinessOwnerProfile by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        setupObservers()
    }

    private fun setupBinding() {
        // Initialize the binding
        binding = ActivityBusinessOwnerLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Call getInformation to retrieve user and other data
        viewModel.getInformation()

        // Set up UI element click listeners
        binding.advertisementsButton.setOnClickListener {
            viewModel.onAdvertisementsClicked()
        }
        binding.qrButton.setOnClickListener {
            viewModel.onQrScannerClicked()
        }
        binding.loyaltyButton.setOnClickListener {
            viewModel.onQrScannerClicked()
        }
        binding.advertisementsMenuButton.setOnClickListener {
            viewModel.onAdvertisementsClicked()
        }

        // Drawer layout handling
        binding.menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.optionsMenu)
        }
        binding.backButton.setOnClickListener {
            binding.drawerLayout.closeDrawer(binding.optionsMenu)
        }

        // Logout button handling
        binding.logoutButton.setOnClickListener {
            viewModel.logOut()
            redirectToInitial()
        }
    }

    private fun setupObservers() {
        // Observe user data or other relevant data here
        viewModel.isUser.observe(this) { user ->
            // Update UI with user info if needed, e.g.:
            // binding.usernameTextView.text = user?.name
        }

        // Navigation function
        viewModel.navigateTo.observe(this) { destination ->
            destination?.let {
                when (it) {
                    NavigationDestination.ADVERTISEMENTS -> navigateToActivity(ActivityBusinessOwnerAdvertisements::class.java)
                    NavigationDestination.QR_SCANNER -> navigateToActivity(ActivityBusinessOwnerQRScanner::class.java)
                    NavigationDestination.INITIAL -> redirectToInitial()
                }
                viewModel.onNavigationHandled()
            }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    private fun redirectToInitial() {
        val initialIntent = Intent(this, ActivityInitial::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(initialIntent)
    }
}
