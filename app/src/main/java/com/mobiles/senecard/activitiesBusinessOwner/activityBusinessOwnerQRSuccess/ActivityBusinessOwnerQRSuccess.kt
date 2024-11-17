package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityBusinessOwnerQrSuccessBinding

class ActivityBusinessOwnerQRSuccess : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerQrSuccessBinding
    private val viewModel: ViewModelBusinessOwnerQRSuccess by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityBusinessOwnerQrSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupUI()

        // Fetch the user ID passed from the QR scanner activity
        val userId = intent.getStringExtra("USER_ID")
        if (userId != null) {
            viewModel.loadCustomerData(userId)
        } else {
            Toast.makeText(this, "Error: No user ID found.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupUI() {
        // Back button logic
        binding.backButton.setOnClickListener {
            navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
        }

        // Make Stamp button logic
        binding.makeStampButton.setOnClickListener {
            viewModel.addStamp()
        }

        // Redeem Loyalty Card button logic
        binding.redeemLoyaltyCardButton.setOnClickListener {
            viewModel.redeemLoyaltyCard()
        }
    }

    private fun setupObservers() {
        // Observe customer name
        viewModel.customerName.observe(this) { name ->
            binding.customerNameTextView.text = "Customer: $name"
        }

        // Observe loyalty cards redeemed
        viewModel.loyaltyCards.observe(this) { count ->
            binding.loyaltyCardsTextView.text = count.toString()
        }

        // Observe current and max stamps
        viewModel.currentStamps.observe(this) { stamps ->
            val maxStamps = viewModel.maxStamps.value ?: 0
            binding.stampsAwardedTextView.text = "$stamps/$maxStamps"
        }

        viewModel.maxStamps.observe(this) { maxStamps ->
            val currentStamps = viewModel.currentStamps.value ?: 0
            binding.stampsAwardedTextView.text = "$currentStamps/$maxStamps"
        }

        // Observe redeemable status and button states
        viewModel.isRedeemable.observe(this) { isRedeemable ->
            if (isRedeemable) {
                binding.redeemLoyaltyCardButton.isEnabled = true
                binding.redeemLoyaltyCardButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.green_highlight)

                binding.makeStampButton.isEnabled = false
                binding.makeStampButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.secondary)
            } else {
                binding.makeStampButton.isEnabled = true
                binding.makeStampButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary)

                binding.redeemLoyaltyCardButton.isEnabled = false
                binding.redeemLoyaltyCardButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.secondary)
            }
        }

        // Observe UI state for information messages
        viewModel.uiState.observe(this) { uiState ->
            when (uiState) {
                UiState.INFORMATION -> {
                    viewModel.infoMessage.value?.let { message ->
                        showInformationPopup(message)
                        viewModel.onInformationAcknowledged()
                    }
                }

                else -> Unit
            }
        }

        // Observe navigation destination
        viewModel.navigationDestination.observe(this) { destination ->
            when (destination) {
                NavigationDestination.LANDING_PAGE -> {
                    navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
                }
                NavigationDestination.INITIAL -> {
                    navigateToActivity(ActivityInitial::class.java)
                }
                else -> Unit
            }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }

    private fun showInformationPopup(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.businessowner_popup_information)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val messageTextView = dialog.findViewById<TextView>(R.id.informationMessageTextView)
            val okButton = dialog.findViewById<Button>(R.id.okButton)

            messageTextView?.text = message
            okButton?.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
