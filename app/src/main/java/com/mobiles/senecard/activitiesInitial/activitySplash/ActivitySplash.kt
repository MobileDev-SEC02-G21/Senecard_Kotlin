package com.mobiles.senecard.activitiesInitial.activitySplash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial

class ActivitySplash : AppCompatActivity() {

    private val viewModelSplash: ViewModelSplash by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModelSplash.isLoading.value
            }
        }

        super.onCreate(savedInstanceState)

        viewModelSplash.sessionValidationComplete.observe(this) { isComplete ->
            if (isComplete) {
                navigateBasedOnRole()
            }
        }
    }

    private fun navigateBasedOnRole() {
        val role = viewModelSplash.isLoggedRole.value
        val intent = when (role) {
            "uniandesMember" -> Intent(this, ActivityHomeUniandesMember::class.java)
            "businessOwner" -> Intent(this, ActivityBusinessOwnerLandingPage::class.java)
            else -> Intent(this, ActivityInitial::class.java)
        }
        startActivity(intent)
    }
}