package com.mobiles.senecard

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ActivityHome : AppCompatActivity() {

    private lateinit var mainLayout: ConstraintLayout
    private lateinit var homeButtonLogOut: Button

    private fun setViews() {
        mainLayout = findViewById(R.id.main_layout)
        homeButtonLogOut = findViewById(R.id.home_button_log_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        setViews()
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bundle:Bundle? = intent.extras
        val user = bundle?.getString("user")
        setup(user?:"")
    }

    private fun setup(user: String) {

        homeButtonLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
        }

    }
}