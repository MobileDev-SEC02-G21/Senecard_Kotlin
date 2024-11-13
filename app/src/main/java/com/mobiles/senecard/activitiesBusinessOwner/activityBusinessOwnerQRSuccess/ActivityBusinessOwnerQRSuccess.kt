package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerProfile
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerRedeemLoyalty.ActivityBusinessOwnerRedeemLoyalty

class ActivityBusinessOwnerQRSuccess : AppCompatActivity() {

    private val viewModel: ViewModelBusinessOwnerQRSuccess by viewModels()

    private var businessOwnerId: String? = null
    private var storeId: String? = null
    private var userId: String? = null
    private var hasNavigatedToRedeem: Boolean = false // Flag to prevent multiple navigations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_success)

        // Retrieve the businessOwnerId, storeId, and userId passed from the previous activity
        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")
        userId = intent.getStringExtra("userId")

        // Observe ViewModel data and handle navigation when necessary
        observeViewModel()

        // Load the current loyalty card and purchase info
        if (storeId != null && userId != null) {
            viewModel.getLoyaltyCardAndPurchases(storeId!!, userId!!)
            viewModel.getUserName(userId!!) // Fetch the user name using userId
        } else {
            Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
        }

        // Button to make the stamp (create purchase and update loyalty card)
        findViewById<Button>(R.id.makeStampButton).setOnClickListener {
            if (storeId != null && userId != null) {
                viewModel.makeStamp(storeId!!, userId!!)
            } else {
                Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle back button functionality
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            navigateToLandingPage()
        }
    }

    private fun observeViewModel() {
        // Observe loyalty card info and navigate if points meet the requirement
        viewModel.loyaltyCardInfo.observe(this) { loyaltyCardInfo ->
            loyaltyCardInfo?.let {
                // Update the UI
                findViewById<TextView>(R.id.loyaltyCardsTextView).text = it.loyaltyCardsRedeemed.toString()
                findViewById<TextView>(R.id.stampsAwardedTextView).text = "${it.currentPoints}/${it.maxPoints}"

                // Navigate to RedeemLoyalty view if points meet the requirement and prevent multiple navigations
                if (it.currentPoints >= it.maxPoints && !hasNavigatedToRedeem) {
                    hasNavigatedToRedeem = true // Set the flag to true to prevent re-triggering navigation
                    navigateToRedeemLoyaltyCard()
                }
            }
        }

        // Observe purchase success and check points again after a purchase is registered
        viewModel.purchaseSuccess.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Purchase registered successfully", Toast.LENGTH_SHORT).show()
                // Reload loyalty card info after purchase
                viewModel.getLoyaltyCardAndPurchases(storeId!!, userId!!)
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        // Observe user name and update the UI
        viewModel.userName.observe(this) { name ->
            findViewById<TextView>(R.id.customerNameTextView).text = "Customer: $name"
        }
    }

    // Navigate to the RedeemLoyalty view if loyalty card points meet the requirement
    private fun navigateToRedeemLoyaltyCard() {
        val intent = Intent(this, ActivityBusinessOwnerRedeemLoyalty::class.java).apply {
            putExtra("businessOwnerId", businessOwnerId)
            putExtra("storeId", storeId)
            putExtra("userId", userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK // Clear activity stack
        }
        startActivity(intent)
        finish() // End current activity to prevent returning to it
    }

    // Navigate to the landing page
    private fun navigateToLandingPage() {
        val intent = Intent(this, ActivityBusinessOwnerProfile::class.java).apply {
            putExtra("businessOwnerId", businessOwnerId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK // Clear activity stack
        }
        startActivity(intent)
        finish() // End current activity
    }
}
