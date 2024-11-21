package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess.ActivityBusinessOwnerQRSuccess
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityBusinessOwnerQrScannerBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityBusinessOwnerQRScanner : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerQrScannerBinding
    private val viewModel: ViewModelBusinessOwnerQRScanner by viewModels()

    // Dialog variables
    private lateinit var loadingDialog: Dialog
    private lateinit var informationDialog: Dialog
    private lateinit var errorDialog: Dialog

    // Sentinel to prevent redundant navigation
    private var isNavigating = false

    private var isActive = true

    private var isInformationPopupVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDialogs()
        setupBinding()
        setupObservers()

        // Start the repeated connection check
        startRepeatingConnectionCheck()
    }

    private fun startRepeatingConnectionCheck() {
        lifecycleScope.launch {
            while (isActive) {
                viewModel.fiveSecConnectionTest() // Call the ViewModel method
                delay(60000) // Wait for 1 minute
            }
        }
    }

    private fun setupBinding() {
        binding = ActivityBusinessOwnerQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
        }

        binding.previewView.post {
            checkCameraPermission()
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
        // Observe navigation updates
        viewModel.navigationDestination.observe(this) { destination ->
            if (isNavigating) return@observe // Prevent redundant navigation

            when (destination) {
                NavigationDestination.LANDING_PAGE -> navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
                NavigationDestination.QR_SUCCESS -> {
                    isNavigating = true // Prevent further navigation
                    val intent = Intent(this, ActivityBusinessOwnerQRSuccess::class.java).apply {
                        putExtra("USER_ID", viewModel.scannedUserId.value)
                    }
                    startActivity(intent)
                    finish()
                }
                else -> Unit
            }
        }

        // Observe UI state updates for popups
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
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            initializeCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun initializeCamera() {
        viewModel.initializeCamera(binding.previewView, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initializeCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        loadingDialog.dismiss()
        informationDialog.dismiss()
        errorDialog.dismiss() // Ensure dialogs are dismissed when activity pauses
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    // Popup functions
    private fun showLoadingPopup() {
        loadingDialog.show()
    }

    private fun hideLoadingPopup() {
        loadingDialog.dismiss()
    }

    private fun showInformationPopup(message: String) {
        if (isInformationPopupVisible) return

        isInformationPopupVisible = true
        val messageTextView = informationDialog.findViewById<TextView>(R.id.informationMessageTextView)
        val okButton = informationDialog.findViewById<Button>(R.id.okButton)

        messageTextView?.text = message
        okButton?.setOnClickListener {
            informationDialog.dismiss()
            isInformationPopupVisible = false // Reset visibility tracker
            viewModel.onInformationAcknowledged()
        }

        informationDialog.show()
    }

    private fun showErrorPopup(message: String) {
        val messageTextView = errorDialog.findViewById<TextView>(R.id.errorMessageTextView)
        val retryButton = errorDialog.findViewById<Button>(R.id.retryButton)
        val cancelButton = errorDialog.findViewById<Button>(R.id.cancelButton)

        messageTextView?.text = message ?: "An error occurred"

        retryButton?.setOnClickListener {
            errorDialog.dismiss()
            viewModel.clearErrorMessage()
            navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
        }

        cancelButton?.setOnClickListener {
            errorDialog.dismiss()
            redirectToInitial() // Navigate to initial activity
        }

        errorDialog.show()
    }

    private fun redirectToInitial() {
        val initialIntent = Intent(this, ActivityInitial::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(initialIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false // Stop the loop when the activity ends
    }
}
