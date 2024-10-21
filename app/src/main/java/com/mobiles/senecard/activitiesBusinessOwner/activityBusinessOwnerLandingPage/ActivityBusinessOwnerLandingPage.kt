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
import com.mobiles.senecard.model.entities.Store

class ActivityBusinessOwnerLandingPage : AppCompatActivity() {

    private val viewModel: ViewModelBusinessOwnerLandingPage by viewModels()

    private var store: Store? = null

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_landing)

        setupObservers() // Set up the observers for LiveData in the ViewModel

        // Retrieve the current user information using Firebase Authentication
        viewModel.getCurrentUserInformation()

        // Button to handle navigating to advertisements
        findViewById<Button>(R.id.advertisementsButton).setOnClickListener {
            navigateToActivity(ActivityBusinessOwnerAdvertisements::class.java)
        }

        // Button to handle QR scanning
        findViewById<ImageButton>(R.id.qrButton).setOnClickListener {
            navigateToActivity(ActivityBusinessOwnerQRScanner::class.java)
        }

        // Button to handle loyalty programs
        findViewById<Button>(R.id.loyaltyButton).setOnClickListener {
            navigateToActivity(ActivityBusinessOwnerQRScanner::class.java)
        }

        // Drawer layout handling
        drawerLayout = findViewById(R.id.drawer_layout)

        findViewById<View>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(findViewById(R.id.options_menu))
        }

        findViewById<View>(R.id.backButton).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.options_menu))
        }

        findViewById<View>(R.id.logoutButton).setOnClickListener {
            viewModel.logOut()
        }
    }

    // Set up LiveData observers to update the UI when data changes
    private fun setupObservers() {
        // Observe the store LiveData and update the rating bar
        viewModel.store.observe(this) { storeData ->
            if (storeData != null) {
                store = storeData // Update the store object
                findViewById<RatingBar>(R.id.ratingBar).rating = (store?.rating ?: 0f).toFloat()
            }
        }

        // Observe today's transaction count and update the UI
        viewModel.transactionCount.observe(this) { count ->
            findViewById<TextView>(R.id.todayCustomersTextView).text = count.toString()
        }

        // Observe the advertisement count and update the UI
        viewModel.advertisementCount.observe(this) { adCount ->
            val advertisementsButton = findViewById<Button>(R.id.advertisementsButton)
            advertisementsButton.text = "YOU HAVE $adCount ADVERTISEMENTS ACTIVE"
        }

        // Observe user LiveData to get businessOwnerId and fetch the business owner data
        viewModel.userLiveData.observe(this) { user ->
            user?.let {
                // Fetch the business owner data based on the user ID
                user.id?.let { it1 -> viewModel.fetchBusinessOwnerData(it1) }
            }
        }

        // Observe log out state and redirect if the user is logged out
        viewModel.isLoggedOut.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
        }

        // Observe error messages
        viewModel.errorLiveData.observe(this) { errorMessage ->
            errorMessage?.let {
                // Handle the error (you could show a Toast or Snackbar here)
            }
        }
    }

    // Method to navigate to other activities, passing the user ID and store information
    private fun navigateToActivity(activityClass: Class<*>) {
        viewModel.userLiveData.value?.let { user ->
            val intent = Intent(this, activityClass)
            intent.putExtra("businessOwnerId", user.id)
            intent.putExtra("storeId", store?.id)
            intent.putExtra("storeName", store?.name)
            startActivity(intent)
        }
    }
}
