package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivitySignInForgotPasswordBinding
import com.mobiles.senecard.activitiesSignIn.activitySignIn.ActivitySignIn
import com.mobiles.senecard.activitiesSignIn.activitySignInVerification.ActivitySignInVerification

class ActivitySignInForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivitySignInForgotPasswordBinding
    private val viewModelSignInForgotPassword: ViewModelSignInForgotPassword by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignInForgotPassword.backImageViewClicked()
        }
        binding.sendCodeButton.setOnClickListener {
            viewModelSignInForgotPassword.sendCodeButtonClicked()
        }
    }

    private fun setObservers() {
        viewModelSignInForgotPassword.navigateToActivitySignIn.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignIn::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                finish()
                startActivity(intent, options.toBundle())
                viewModelSignInForgotPassword.onNavigated()
            }
        }
        viewModelSignInForgotPassword.navigateToActivitySignInVerification.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignInVerification::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignInForgotPassword.onNavigated()
            }
        }
    }
}