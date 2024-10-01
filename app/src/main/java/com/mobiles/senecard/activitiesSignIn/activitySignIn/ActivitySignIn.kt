package com.mobiles.senecard.activitiesSignIn.activitySignIn

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.CustomDialog
import com.mobiles.senecard.R
import com.mobiles.senecard.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.activitiesSignUp.activitySignUp.ActivitySignUp
import com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword.ActivitySignInForgotPassword
import com.mobiles.senecard.databinding.ActivitySignInBinding

class ActivitySignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModelSignIn: ViewModelSignIn by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignIn.backImageViewClicked()
        }
        binding.forgotPasswordTextView.setOnClickListener {
            viewModelSignIn.forgotPasswordTextViewClicked()
        }
        binding.enterButton.setOnClickListener {
            showMessage("Please wait one moment while processing the information", "loading")
            viewModelSignIn.enterButtonClicked(
                email = binding.emailEditText.text.toString(),
                password = binding.passwordEditText.text.toString()
            )
        }
        binding.signUpTextView.setOnClickListener {
            viewModelSignIn.signUpTextViewClicked()
        }
    }

    private fun setObservers() {
        viewModelSignIn.navigateToActivityInitial.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityInitial::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelSignIn.onNavigated()
            }
        }
        viewModelSignIn.navigateToActivitySignInForgotPassword.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignInForgotPassword::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignIn.onNavigated()
            }
        }
        viewModelSignIn.navigateToActivityHome.observe(this) { navigate -> if (navigate) {
                Toast.makeText(this, getString(R.string.sign_in_authenticate_succesfully), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ActivityHomeUniandesMember::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                viewModelSignIn.onNavigated()
            }
        }
        viewModelSignIn.navigateToActivitySignUp.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUp::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_down,
                    R.anim.slide_out_up
                )
                startActivity(intent, options.toBundle())
                viewModelSignIn.onNavigated()
            }
        }
        viewModelSignIn.message.observe(this) { message ->
            when (message) {
                "email_empty" -> showMessage(getString(R.string.sign_in_email_empty))
                "password_empty" -> showMessage(getString(R.string.sign_in_password_empty))
                "email_invalid" -> showMessage(getString(R.string.sign_in_email_invalid))
                "error_firebase_auth" -> showMessage(getString(R.string.sign_in_error_firebase_auth), "error")
            }
        }
    }

    private fun showMessage(message: String, type: String = "info") {
        CustomDialog.showCustomDialog(supportFragmentManager, message, type)
    }
}