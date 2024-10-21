package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerRedeemLoyalty

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage

class ActivityBusinessOwnerRedeemLoyalty : AppCompatActivity() {

    private val viewModel: ViewModelBusinessOwnerRedeemLoyalty by viewModels()

    private var businessOwnerId: String? = null
    private var storeId: String? = null
    private var userId: String? = null
    private var hasNavigated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_redeem_loyalty)

        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")
        userId = intent.getStringExtra("userId")

        observeViewModel()

        // Load user and loyalty card details
        if (storeId != null && userId != null) {
            viewModel.getUserAndLoyaltyCardDetails(storeId!!, userId!!)
        }

        findViewById<Button>(R.id.redeemLoyaltyCardButton).setOnClickListener {
            if (storeId != null && userId != null) {
                viewModel.redeemLoyaltyCard(storeId!!, userId!!)
            } else {
                Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            navigateToLandingPage()
        }
    }

    private fun observeViewModel() {
        viewModel.redeemSuccess.observe(this) { success ->
            if (success == true && !hasNavigated) {
                showSuccessDialog()
            }
        }

        viewModel.loyaltyCardInfo.observe(this) { info ->
            info?.let {
                findViewById<TextView>(R.id.loyaltyCardsTextView).text = it.loyaltyCardsRedeemed.toString()
                findViewById<TextView>(R.id.stampsAwardedTextView).text = "${it.currentPoints}/${it.maxPoints}"
            }
        }

        viewModel.userName.observe(this) { userName ->
            findViewById<TextView>(R.id.customerNameTextView).text = "Customer: $userName"
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Loyalty Card Redeemed")
            .setMessage("Loyalty card has been redeemed successfully.")
            .setPositiveButton("OK") { _, _ ->
                navigateToLandingPage()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLandingPage() {
        if (!hasNavigated) {
            hasNavigated = true
            val intent = Intent(this, ActivityBusinessOwnerLandingPage::class.java).apply {
                putExtra("businessOwnerId", businessOwnerId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
