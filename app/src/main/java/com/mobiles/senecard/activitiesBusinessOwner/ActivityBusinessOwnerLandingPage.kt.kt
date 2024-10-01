package com.mobiles.senecard.activitiesBusinessOwner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.mobiles.senecard.R
import android.view.View
import android.widget.Button


class ActivityBusinessOwnerLandingPage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_landing)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up the menu button to open the drawer
        findViewById<View>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(findViewById(R.id.options_menu))
        }

        // Set up the back button in the options menu to close the drawer
        findViewById<View>(R.id.backButton).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.options_menu))
        }

        // Add logic to handle buttons or other UI elements
        findViewById<Button>(R.id.advertisementsButton).setOnClickListener {
            // Add your logic here
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            // Add your logic here
        }
    }
}
