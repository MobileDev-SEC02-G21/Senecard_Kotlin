package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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
                    NavigationDestination.INITIAL -> redirectToInitial()
                }
                viewModel.onNavigationHandled()
            }
        }
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
        val messageTextView = informationDialog.findViewById<TextView>(R.id.informationMessageTextView)
        val okButton = informationDialog.findViewById<Button>(R.id.okButton)

        messageTextView?.text = message
        okButton?.setOnClickListener {
            informationDialog.dismiss()
            viewModel.onInformationAcknowledged()
        }

        informationDialog.show()
    }

    private fun showErrorPopup(message: String) {
        val messageTextView = errorDialog.findViewById<TextView>(R.id.errorMessageTextView)
        val retryButton = errorDialog.findViewById<Button>(R.id.retryButton)
        val cancelButton = errorDialog.findViewById<Button>(R.id.cancelButton)

        messageTextView?.text = message
        retryButton?.setOnClickListener {
            errorDialog.dismiss()
            viewModel.getAdvertisements() // Retry fetching advertisements
        }
        cancelButton?.setOnClickListener {
            errorDialog.dismiss()
            viewModel.logOut() // Log out the user
            redirectToInitial()
        }

        errorDialog.show()
    }

    private fun navigateToCreateAdvertisement() {
        val intent = Intent(this, ActivityBusinessOwnerAdvertisementsCreate::class.java)
        startActivity(intent)
    }

    private fun redirectToInitial() {
        val initialIntent = Intent(this, ActivityInitial::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(initialIntent)
    }
}
