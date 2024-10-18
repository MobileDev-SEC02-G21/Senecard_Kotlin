package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerRedeemLoyalty

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_redeem_loyalty)

        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")
        userId = intent.getStringExtra("userId")

        observeViewModel()

        findViewById<Button>(R.id.redeemLoyaltyCardButton).setOnClickListener {
            if (storeId != null && userId != null) {
                viewModel.redeemLoyaltyCard(storeId!!, userId!!)
            } else {
                Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.redeemSuccess.observe(this) { success ->
            if (success == true) {
                showSuccessDialog()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerLandingPage::class.java)
            intent.putExtra("businessOwnerId", businessOwnerId)
            startActivity(intent)
            finish()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Loyalty Card Redeemed")
            .setMessage("Loyalty card has been redeemed successfully.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(this, ActivityBusinessOwnerLandingPage::class.java)
                intent.putExtra("businessOwnerId", businessOwnerId)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
