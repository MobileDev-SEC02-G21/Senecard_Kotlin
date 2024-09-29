package com.mobiles.senecard.activityHome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial

class ActivityHome : AppCompatActivity() {

    private lateinit var homeMainLayout: ConstraintLayout
    private lateinit var homeTextViewTest: TextView
    private lateinit var homeButtonLogOut: Button

    private val viewModelHome: ViewModelHome by viewModels()

    private fun initializeElements() {
        homeMainLayout = findViewById(R.id.home_main_layout)
        homeTextViewTest = findViewById(R.id.home_text_view)
        homeButtonLogOut = findViewById(R.id.home_button_log_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        initializeElements()

        setValidateSession()

        ViewCompat.setOnApplyWindowInsetsListener(homeMainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val email = viewModelHome.getUserEmail()
        homeTextViewTest.text = email

        setLogOutButton()
    }

    private fun setValidateSession() {
        viewModelHome.validateSession()

        viewModelHome.isLogged.observe(this) { logged ->
            if (!logged) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            } else {
                homeTextViewTest.text = viewModelHome.getUserEmail()
            }
        }
    }

    private fun setLogOutButton() {
        homeButtonLogOut.setOnClickListener {
            viewModelHome.logOut()
        }

        viewModelHome.isLoggedOut.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
        }
    }
}