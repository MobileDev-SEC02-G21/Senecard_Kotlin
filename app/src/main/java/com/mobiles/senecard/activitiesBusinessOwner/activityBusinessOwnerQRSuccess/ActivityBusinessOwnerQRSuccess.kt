package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityBusinessOwnerQrSuccessBinding

class ActivityBusinessOwnerQRSuccess : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerQrSuccessBinding
    private val viewModel: ViewModelBusinessOwnerQRSuccess by viewModels()

    // Dialog variables
    private lateinit var loadingDialog: Dialog
    private lateinit var informationDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDialogs()
        setupBinding()
        setupObservers()

        // Fetch the user ID passed from the QR scanner activity
        val userId = intent.getStringExtra("USER_ID")
        if (userId != null) {
            viewModel.loadCustomerData(userId)
        } else {
            Toast.makeText(this, "Error: No user ID found.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupBinding() {
        binding = ActivityBusinessOwnerQrSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun setupDialogs() {
        // Setup Loading Dialog
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.businessowner_popup_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Setup Information Dialog
        informationDialog = Dialog(this)
        informationDialog.setContentView(R.layout.businessowner_popup_information)
        informationDialog.setCancelable(false)
        informationDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
                // Enable redeem loyalty card button
                binding.redeemLoyaltyCardButton.isEnabled = true
                binding.redeemLoyaltyCardButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.green_highlight)

                // Disable make stamp button
                binding.makeStampButton.isEnabled = false
                binding.makeStampButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.secondary)

                // Update redeem loyalty info text
                binding.redeemLoyaltyInfoText.text = "LOYALTY CARD CAN BE REDEEMED"
                binding.redeemLoyaltyInfoText.setTextColor(ContextCompat.getColor(this, R.color.green_highlight))
            } else {
                // Enable make stamp button
                binding.makeStampButton.isEnabled = true
                binding.makeStampButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.primary)

                // Disable redeem loyalty card button
                binding.redeemLoyaltyCardButton.isEnabled = false
                binding.redeemLoyaltyCardButton.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.secondary)

                // Update redeem loyalty info text
                binding.redeemLoyaltyInfoText.text = "IS NOT AVAILABLE TO REDEEM A LOYALTY CARD"
                binding.redeemLoyaltyInfoText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
        }


        // Observe UI state for information messages
        viewModel.uiState.observe(this) { uiState ->
            when (uiState) {
                UiState.LOADING -> showLoadingPopup()
                UiState.INFORMATION -> showInformationPopup(viewModel.infoMessage.value ?: "")
                else -> hideLoadingPopup()
            }
        }

        // Observe navigation destination
        viewModel.navigationDestination.observe(this) { destination ->
            when (destination) {
                NavigationDestination.LANDING_PAGE -> navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
                NavigationDestination.INITIAL -> navigateToActivity(ActivityInitial::class.java)
                else -> Unit
            }
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }

    private fun showLoadingPopup() {
        loadingDialog.show()
    }

    private fun hideLoadingPopup() {
        loadingDialog.dismiss()
    }

    private fun showInformationPopup(message: String) {
        val messageTextView = informationDialog.findViewById<TextView>(R.id.informationMessageTextView)
        val okButton = informationDialog.findViewById<Button>(R.id.okButton)

        messageTextView?.text = message
        okButton?.setOnClickListener {
            informationDialog.dismiss()
            viewModel.onInformationAcknowledged()
        }

        informationDialog.show()
    }
}
