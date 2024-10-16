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

    private var businessOwnerId: String = "93AqPXLaN42cT8OxvewX" // Default ID for development purposes
    private var store: Store? = null

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_landing)

        setupObservers() // Set up the observers for LiveData in the ViewModel

        // Check if a businessOwnerId was passed via the Intent
        val passedId = intent.getStringExtra("businessOwnerId")
        if (passedId != null) {
            businessOwnerId = passedId // Use the passed ID
        }

        // Fetch data from Firestore
        viewModel.fetchBusinessOwnerData(businessOwnerId)

        findViewById<Button>(R.id.advertisementsButton).setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerAdvertisements::class.java)
            intent.putExtra("businessOwnerId", businessOwnerId)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.qrButton).setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            intent.putExtra("businessOwnerId", businessOwnerId)
            intent.putExtra("storeId", store?.id) // Pass store ID to the next activity
            intent.putExtra("storeName", store?.name)
            startActivity(intent)
        }

        val loyaltyButton = findViewById<Button>(R.id.loyaltyButton)
        loyaltyButton.setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerQRScanner::class.java)
            intent.putExtra("businessOwnerId", businessOwnerId)
            intent.putExtra("storeId", store?.id) // Pass store ID to the next activity
            intent.putExtra("storeName", store?.name)
            startActivity(intent)
        }

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
        // Observe the store LiveData
        viewModel.store.observe(this) { storeData ->
            if (storeData != null) {
                store = storeData // Update the store object

                findViewById<RatingBar>(R.id.ratingBar).rating = (store?.rating ?: 0f).toFloat()
            }
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
