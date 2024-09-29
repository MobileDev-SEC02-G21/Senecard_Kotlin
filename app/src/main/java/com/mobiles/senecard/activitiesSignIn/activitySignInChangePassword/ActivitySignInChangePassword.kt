package com.mobiles.senecard.activitiesSignIn.activitySignInChangePassword

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivitySignInChangePasswordBinding
import com.mobiles.senecard.activitiesSignIn.activitySignInVerification.ActivitySignInVerification

class ActivitySignInChangePassword : AppCompatActivity() {

    private lateinit var binding: ActivitySignInChangePasswordBinding
    private val viewModelSignInChangePassword: ViewModelSignInChangePassword by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignInChangePassword.backImageViewClicked()
        }
    }

    private fun setObservers() {
        viewModelSignInChangePassword.navigateToActivitySignInVerification.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignInVerification::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                finish()
                startActivity(intent, options.toBundle())
                viewModelSignInChangePassword.onNavigated()
            }
        }
    }
}