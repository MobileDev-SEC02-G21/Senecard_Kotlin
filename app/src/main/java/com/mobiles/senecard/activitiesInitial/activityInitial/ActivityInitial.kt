package com.mobiles.senecard.activitiesInitial.activityInitial

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesSignUp.activitySignUp.ActivitySignUp
import com.mobiles.senecard.databinding.ActivityInitialBinding
import com.mobiles.senecard.activitiesSignIn.activitySignIn.ActivitySignIn

class ActivityInitial : AppCompatActivity() {

    private lateinit var binding: ActivityInitialBinding
    private val viewModelInitial: ViewModelInitial by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.initialButtonSingIn.setOnClickListener {
            viewModelInitial.onSignInClicked()
        }
        binding.initialButtonSingUp.setOnClickListener {
            viewModelInitial.onSignUpClicked()
        }
    }

    private fun setObservers() {
        viewModelInitial.navigateToActivitySignIn.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignIn::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelInitial.onNavigated()
            }
        }
        viewModelInitial.navigateToActivitySignUp.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUp::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelInitial.onNavigated()
            }
        }
    }
}