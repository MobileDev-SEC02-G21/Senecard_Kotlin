package com.mobiles.senecard.activitiesBusinessOwner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.mobiles.senecard.R
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import org.json.JSONObject
import java.io.IOException

class ActivityBusinessOwnerLandingPage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_landing)

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

        // Load the JSON data
        val jsonString = loadJSONFromAssets("mock_data_business_owner.json")
        val jsonObject = JSONObject(jsonString)

        // Access the store object
        val store = jsonObject.getJSONObject("store")

        // Display the number of transactions
        val transactionsArray = store.getJSONArray("transactions")
        val transactionCount = transactionsArray.length()
        val todayCustomersTextView = findViewById<TextView>(R.id.todayCustomersTextView)
        todayCustomersTextView.text = transactionCount.toString()

        // Display the number of advertisements
        val advertisementsArray = store.getJSONArray("advertisements")
        val advertisementCount = advertisementsArray.length()
        val advertisementsButton = findViewById<Button>(R.id.advertisementsButton)
        advertisementsButton.text = "YOU HAVE $advertisementCount ADVERTISEMENTS ACTIVE"

        // Display the store rating
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val rating = store.optDouble("rating", 0.0)
        ratingBar.rating = rating.toFloat()

        // Add logic to handle other buttons or UI elements
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            // Add your logic here
        }
    }

    // Function to load JSON from the assets folder
    private fun loadJSONFromAssets(fileName: String): String {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            ""
        }
    }

}
