package com.mobiles.senecard.activitiesSignUp.activitySignUpUniandesMember

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.CustomDialog
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesSignUp.activitySignUp.ActivitySignUp
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.databinding.ActivitySignUpUniandesMemberBinding

class ActivitySignUpUniandesMember : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpUniandesMemberBinding
    private val viewModelSignUpUniandesMember: ViewModelSignUpUniandesMember by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpUniandesMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignUpUniandesMember.backImageViewClicked()
        }
        binding.registerButton.setOnClickListener {
            showMessage(getString(R.string.please_wait_one_moment_while_processing_the_information), "loading")
            viewModelSignUpUniandesMember.registerButtonClicked(
                name = binding.nameEditText.text.toString(),
                email = binding.emailEditText.text.toString(),
                phone = binding.phoneEditText.text.toString(),
                password = binding.passwordEditText.text.toString(),
                confirmPassword = binding.confirmPasswordEditText.text.toString()
            )
        }
    }

    private fun setObservers() {
        viewModelSignUpUniandesMember.navigateToActivitySignUp.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUp::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelSignUpUniandesMember.onNavigated()
            }
        }
        viewModelSignUpUniandesMember.navigateToActivityHomeUniandesMember.observe(this) { navigate ->
            if (navigate) {
                Toast.makeText(this, getString(R.string.sign_up_uniandes_member_register_succesfully), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ActivityHomeUniandesMember::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                viewModelSignUpUniandesMember.onNavigated()
            }
        }
        viewModelSignUpUniandesMember.message.observe(this) { message ->
            when (message) {
                "name_empty" -> showMessage(getString(R.string.sign_up_uniandes_member_name_empty))
                "name_invalid" -> showMessage(getString(R.string.sign_up_uniandes_member_name_empty))
                "email_empty" -> showMessage(getString(R.string.sign_up_uniandes_member_email_empty))
                "phone_empty" -> showMessage(getString(R.string.sign_up_uniandes_member_phone_empty))
                "password_empty" -> showMessage(getString(R.string.sign_up_uniandes_member_password_empty))
                "confirm_password_empty" -> showMessage(getString(R.string.sign_up_uniandes_member_confirm_password_empty))
                "no_spaces_allowed" -> showMessage(getString(R.string.please_spaces_are_not_allowed_here))
                "email_invalid" -> showMessage(getString(R.string.sign_up_uniandes_member_email_invalid))
                "password_short" -> showMessage(getString(R.string.sign_up_uniandes_member_password_short))
                "passwords_not_equals" -> showMessage(getString(R.string.sign_up_uniandes_member_passwords_not_equals))
                "no_internet_connection" -> showMessage(getString(R.string.no_internet_connection_please_check_your_connection_and_try_again), "error")
                "user_exists" -> showMessage(getString(R.string.sign_up_uniandes_member_user_exists), "error")
                "error_firebase_auth" -> showMessage(getString(R.string.sign_up_uniandes_member_error_firebase_auth), "error")
                "error_firebase_firestore" -> showMessage(getString(R.string.sign_up_uniandes_member_error_firebase_firestore), "error")
            }
        }
    }

    private fun showMessage(message: String, type: String = "info") {
        CustomDialog.showCustomDialog(supportFragmentManager, message, type)
    }
}