package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess.ActivityBusinessOwnerQRSuccess
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityBusinessOwnerQrScannerBinding

class ActivityBusinessOwnerQRScanner : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerQrScannerBinding
    private val viewModel: ViewModelBusinessOwnerQRScanner by viewModels()

    // Dialog variables
    private lateinit var loadingDialog: Dialog
    private lateinit var informationDialog: Dialog
    private lateinit var errorDialog: Dialog

    // Sentinel to prevent redundant navigation
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        setupDialog()
        setupObservers()
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

    private fun setupDialog() {
        informationDialog = Dialog(this)
        informationDialog.setContentView(R.layout.businessowner_popup_information)
        informationDialog.findViewById<Button>(R.id.okButton).setOnClickListener {
            informationDialog.dismiss()
        }
        informationDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupObservers() {
        // Observe navigation updates
        viewModel.navigationDestination.observe(this) { destination ->
            if (isNavigating) return@observe // Prevent redundant navigation

            when (destination) {
                NavigationDestination.LANDING_PAGE -> {
                    navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
                }
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
            == PackageManager.PERMISSION_GRANTED) {
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
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        informationDialog.dismiss() // Ensure dialog is dismissed when activity pauses
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

        }

        errorDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            errorDialog.dismiss()
            redirectToInitial() // Optional logout option
        }

        errorDialog.show()
    }

    private fun redirectToInitial() {
        val initialIntent = Intent(this, ActivityInitial::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(initialIntent)
    }

}
