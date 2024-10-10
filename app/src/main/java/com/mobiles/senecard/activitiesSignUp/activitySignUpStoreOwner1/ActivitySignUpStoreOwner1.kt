package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner1

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.CustomDialog
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesSignUp.activitySignUp.ActivitySignUp
import com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner2.ActivitySignUpStoreOwner2
import com.mobiles.senecard.databinding.ActivitySignUpStoreOwner1Binding

class ActivitySignUpStoreOwner1 : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpStoreOwner1Binding
    private val viewModelSignUpStoreOwner1: ViewModelSignUpStoreOwner1 by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpStoreOwner1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignUpStoreOwner1.backImageViewClicked()
        }
        binding.nextButton.setOnClickListener {
            showMessage("Please wait one moment while processing the information", "loading")
            viewModelSignUpStoreOwner1.nextButtonClicked(
                name = binding.nameEditText.text.toString(),
                email = binding.emailEditText.text.toString(),
                phone = binding.phoneEditText.text.toString(),
                password = binding.passwordEditText.text.toString(),
                confirmPassword = binding.confirmPasswordEditText.text.toString()
            )
        }
    }

    private fun setObservers() {
        viewModelSignUpStoreOwner1.navigateToActivitySignUp.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUp::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelSignUpStoreOwner1.onNavigated()
            }
        }
        viewModelSignUpStoreOwner1.navigateToActivitySignUpStoreOwner2.observe(this) { navigate ->
            if (navigate) {
                CustomDialog.hideCustomDialog()
                val intent = Intent(this, ActivitySignUpStoreOwner2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignUpStoreOwner1.onNavigated()
            }
        }
        viewModelSignUpStoreOwner1.message.observe(this) { message ->
            when (message) {
                "name_empty" -> showMessage(getString(R.string.sign_up_store_owner_1_name_empty))
                "email_empty" -> showMessage(getString(R.string.sign_up_store_owner_1_email_empty))
                "phone_empty" -> showMessage(getString(R.string.sign_up_store_owner_1_phone_empty))
                "password_empty" -> showMessage(getString(R.string.sign_up_store_owner_1_password_empty))
                "confirm_password_empty" -> showMessage(getString(R.string.sign_up_store_owner_1_confirm_password_empty))
                "email_invalid" -> showMessage(getString(R.string.sign_up_store_owner_1_email_invalid))
                "password_short" -> showMessage(getString(R.string.sign_up_store_owner_1_password_short))
                "passwords_not_equals" -> showMessage(getString(R.string.sign_up_store_owner_1_passwords_not_equals))
                "user_exists" -> showMessage(getString(R.string.sign_up_store_owner_1_user_exists))
            }
        }
    }

    private fun showMessage(message: String, type: String = "info") {
        CustomDialog.showCustomDialog(supportFragmentManager, message, type)
    }
}