package com.mobiles.senecard.activitiesSignIn.activitySignInVerification

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivitySignInVerificationBinding
import com.mobiles.senecard.activitiesSignIn.activitySignInChangePassword.ActivitySignInChangePassword
import com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword.ActivitySignInForgotPassword

class ActivitySignInVerification : AppCompatActivity() {

    private lateinit var binding: ActivitySignInVerificationBinding
    private val viewModelSignInVerification: ViewModelSignInVerification by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignInVerification.backImageViewClicked()
        }
        binding.verifyButton.setOnClickListener {
            viewModelSignInVerification.verifyButtonClicked()
        }
    }

    private fun setObservers() {
        viewModelSignInVerification.navigateToActivitySignInForgotPassword.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignInForgotPassword::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                finish()
                startActivity(intent, options.toBundle())
                viewModelSignInVerification.onNavigated()
            }
        }

        viewModelSignInVerification.navigateToActivitySignInChangePassword.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignInChangePassword::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignInVerification.onNavigated()
            }
        }
    }
}