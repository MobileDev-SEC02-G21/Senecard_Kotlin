package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRFailure

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner.ActivityBusinessOwnerQRScanner

class ActivityBusinessOwnerQRFailure : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_failure)

        // Back arrow button should now return to the QR scanner
        val backButton = findViewById<ImageButton>(R.id.backButtonFailure)
        backButton.setOnClickListener {
            // Navigate back to the QR scanner
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            startActivity(intent)
            finish()
        }

        // Return to Main Page button
        val returnToMainButton = findViewById<Button>(R.id.returnToMainFailure)
        returnToMainButton.setOnClickListener {
            // Navigate to the landing page
            val intent = Intent(this, ActivityBusinessOwnerLandingPage::class.java)
            startActivity(intent)
            finish()
        }

        // Try Again button
        val tryAgainButton = findViewById<Button>(R.id.tryAgainButton)
        tryAgainButton.setOnClickListener {
            // Navigate back to the QR scanner to retry
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            startActivity(intent)
            finish()
        }
    }
}
