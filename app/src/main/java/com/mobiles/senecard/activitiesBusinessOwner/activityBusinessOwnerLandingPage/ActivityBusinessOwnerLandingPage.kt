package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner.ActivityBusinessOwnerQRScanner
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements.ActivityBusinessOwnerAdvertisements
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial

class ActivityBusinessOwnerLandingPage : AppCompatActivity() {

    // Initialize ViewModel using Kotlin's 'by viewModels' delegate
    private val viewModel: ViewModelBusinessOwnerLandingPage by viewModels()

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_landing)

        setupObservers() // Set up the observers for LiveData in the ViewModel

        viewModel.fetchBusinessOwnerData() // Fetch the data from Firestore

        findViewById<Button>(R.id.advertisementsButton).setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerAdvertisements::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.qrButton).setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            startActivity(intent)
        }

        // Loyalty Button functionality
        val loyaltyButton = findViewById<Button>(R.id.loyaltyButton)
        loyaltyButton.setOnClickListener {
            // Redirect to QR scanner activity
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            startActivity(intent)
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up the menu button to open the drawer
        findViewById<View>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(findViewById(R.id.options_menu))
        }

        // Set up the back button in the options menu to close the drawer
        findViewById<View>(R.id.backButton).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.options_menu))
        }

        findViewById<View>(R.id.logoutButton).setOnClickListener {
            viewModel.logOut()
        }
    }

    // Set up LiveData observers to update the UI when data changes
    private fun setupObservers() {
        // Observe the store name LiveData
        viewModel.storeName.observe(this) { storeName ->
            // Update any UI components with the store name (if required)
        }

        // Observe the transaction count LiveData
        viewModel.transactionCount.observe(this) { count ->
            findViewById<TextView>(R.id.todayCustomersTextView).text = count.toString()
        }

        // Observe the advertisement count LiveData
        viewModel.advertisementCount.observe(this) { adCount ->
            val advertisementsButton = findViewById<Button>(R.id.advertisementsButton)
            advertisementsButton.text = "YOU HAVE $adCount ADVERTISEMENTS ACTIVE"
        }

        // Observe the store rating LiveData
        viewModel.rating.observe(this) { rating ->
            findViewById<RatingBar>(R.id.ratingBar).rating = rating
        }

        viewModel.isLoggedOut.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
        }
    }
}
