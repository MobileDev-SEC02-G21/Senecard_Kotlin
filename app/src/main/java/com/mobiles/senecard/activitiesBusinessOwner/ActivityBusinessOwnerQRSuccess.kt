package com.mobiles.senecard.activitiesBusinessOwner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R

class ActivityBusinessOwnerQRSuccess : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_success)

        // Back arrow button should now return to the QR scanner
        val backButton = findViewById<ImageButton>(R.id.backButtonSuccess)
        backButton.setOnClickListener {
            // Navigate back to the QR scanner
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            startActivity(intent)
            finish()
        }

        // Return to Main Page button
        val returnToMainButton = findViewById<Button>(R.id.returnToMainSuccess)
        returnToMainButton.setOnClickListener {
            // Navigate to the landing page
            val intent = Intent(this, ActivityBusinessOwnerLandingPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}
