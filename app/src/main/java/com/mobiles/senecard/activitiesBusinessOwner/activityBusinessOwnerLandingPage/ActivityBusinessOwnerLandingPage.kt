package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner.ActivityBusinessOwnerQRScanner
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements.ActivityBusinessOwnerAdvertisements
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityBusinessOwnerLandingPageBinding

class ActivityBusinessOwnerLandingPage : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerLandingPageBinding
    private val viewModel: ViewModelBusinessOwnerLandingPage by viewModels()

    // Dialog variables
    private lateinit var loadingDialog: Dialog
    private lateinit var informationDialog: Dialog
    private lateinit var errorDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDialogs()
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

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // Update todayCustomersTextView with customer count
        viewModel.todayCustomers.observe(this) { count ->
            binding.todayCustomersTextView.text = count.toString()
        }

        // Update ratingBar with average rating
        viewModel.averageRating.observe(this) { rating ->
            binding.ratingBar.rating = rating?.toFloat()!!
        }

        // Update advertisementsButton with the number of active advertisements
        viewModel.advertisementsCount.observe(this) { count ->
            binding.advertisementsButton.text = "YOU HAVE $count ADVERTISEMENTS ACTIVE"
        }

        // Logic to show popups
        viewModel.uiState.observe(this) { state ->
            when (state) {
                UiState.LOADING -> showLoadingPopup()
                UiState.SUCCESS -> hideLoadingPopup()
                UiState.ERROR -> {
                    hideLoadingPopup()
                    showErrorPopup(viewModel.errorMessage.value ?: "An unknown error occurred")
                }
                UiState.INFORMATION -> {
                    hideLoadingPopup()
                    showInformationPopup(viewModel.infoMessage.value ?: "Information message")
                }
            }
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

        // Setup Error Dialog
        errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.businessowner_popup_error)
        errorDialog.setCancelable(false)
        errorDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
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

    // Popup functions
    private fun showLoadingPopup() {
        loadingDialog.show()
    }

    private fun hideLoadingPopup() {
        loadingDialog.dismiss()
    }

    private fun showInformationPopup(message: String) {
        informationDialog.findViewById<TextView>(R.id.informationMessageTextView).text = message

        informationDialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            informationDialog.dismiss()
            viewModel.onInformationAcknowledged() // Proceed with the next action in the ViewModel
        }

        informationDialog.show()
    }

    private fun showErrorPopup(message: String) {
        errorDialog.findViewById<TextView>(R.id.errorMessageTextView).text = message

        errorDialog.findViewById<Button>(R.id.retryButton).setOnClickListener {
            errorDialog.dismiss()
            viewModel.clearErrorMessage()
            viewModel.getInformation() // Retry Get Information
        }

        errorDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            errorDialog.dismiss()
            viewModel.clearErrorMessage()
            redirectToInitial() // Optional logout option
        }

        errorDialog.show()
    }


}
