package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisementsCreate.ActivityBusinessOwnerAdvertisementsCreate
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.adapters.AdvertisementAdapter
import com.mobiles.senecard.databinding.ActivityBusinessOwnerAdvertisementsBinding
import com.mobiles.senecard.model.entities.Advertisement

class ActivityBusinessOwnerAdvertisements : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerAdvertisementsBinding
    private val viewModel: ViewModelBusinessOwnerAdvertisements by viewModels()

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
        binding = ActivityBusinessOwnerAdvertisementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Call getAdvertisements to fetch advertisements
        viewModel.getAdvertisements()

        // Set up click listeners
        binding.btnAdd.setOnClickListener {
            viewModel.onAddAdvertisementClicked()
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        // Observe advertisements and update RecyclerView
        viewModel.advertisements.observe(this) { advertisements ->
            setupRecyclerView(advertisements)
        }

        // Observe UI state for loading, error, or information popups
        viewModel.uiState.observe(this) { state ->
            when (state) {
                UiState.LOADING -> showLoadingPopup()
                UiState.SUCCESS -> hideLoadingPopup()
                UiState.ERROR -> {
                    hideLoadingPopup()
                    showErrorPopup(viewModel.errorMessage.value ?: "An error occurred")
                }
                UiState.INFORMATION -> {
                    hideLoadingPopup()
                    showInformationPopup(viewModel.infoMessage.value ?: "Information message")
                }
            }
        }

        // Observe navigation actions
        viewModel.navigateTo.observe(this) { destination ->
            destination?.let {
                when (it) {
                    NavigationDestination.ADVERTISEMENT_CREATE -> navigateToCreateAdvertisement()
                    NavigationDestination.INITIAL -> redirectToLogin()
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
        informationDialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            informationDialog.dismiss()
        }
        informationDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Setup Error Dialog
        errorDialog = Dialog(this)
        errorDialog.setContentView(R.layout.businessowner_popup_error)
        errorDialog.findViewById<Button>(R.id.retryButton).setOnClickListener {
            viewModel.getAdvertisements() // Retry fetching advertisements
            errorDialog.dismiss()
        }
            errorDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
                errorDialog.dismiss()
                viewModel.logOut() // Log out the user
                redirectToLogin()   // Redirect to the login screen
            }
        errorDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupRecyclerView(advertisements: List<Advertisement>) {
        binding.rvAdvertisements.layoutManager = LinearLayoutManager(this)
        val adapter = AdvertisementAdapter(advertisements.toMutableList()) { advertisement ->
            viewModel.deleteAdvertisement(advertisement)
        }
        binding.rvAdvertisements.adapter = adapter
    }

    private fun showLoadingPopup() {
        loadingDialog.show()
    }

    private fun hideLoadingPopup() {
        loadingDialog.dismiss()
    }

    private fun showInformationPopup(message: String) {
        informationDialog.findViewById<TextView>(R.id.informationMessageTextView).text = message
        informationDialog.show()
    }

    private fun showErrorPopup(message: String) {
        errorDialog.findViewById<TextView>(R.id.errorMessageTextView).text = message
        errorDialog.show()
    }

    private fun navigateToCreateAdvertisement() {
        val intent = Intent(this, ActivityBusinessOwnerAdvertisementsCreate::class.java)
        startActivity(intent)
    }

    private fun redirectToLogin() {
        val initialIntent = Intent(this, ActivityInitial::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(initialIntent)
    }

}
