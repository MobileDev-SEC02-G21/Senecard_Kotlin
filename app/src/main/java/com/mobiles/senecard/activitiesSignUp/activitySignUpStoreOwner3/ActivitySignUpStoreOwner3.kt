package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.CustomDialog
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner2.ActivitySignUpStoreOwner2
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.databinding.ActivitySignUpStoreOwner3Binding

class ActivitySignUpStoreOwner3 : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpStoreOwner3Binding
    private val viewModelSignUpStoreOwner3: ViewModelSignUpStoreOwner3 by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpStoreOwner3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignUpStoreOwner3.backImageViewClicked()
        }
        binding.registerButton.setOnClickListener {
            showMessage("Please wait one moment while processing the information", "loading")
            val schedule = mapOf(
                "monday" to listOf(
                    binding.mondayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.mondayEndTimeSpinner.selectedItem.toString().toInt()
                ),
                "tuesday" to listOf(
                    binding.tuesdayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.tuesdayEndTimeSpinner.selectedItem.toString().toInt()
                ),
                "wednesday" to listOf(
                    binding.wednesdayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.wednesdayEndTimeSpinner.selectedItem.toString().toInt()
                ),
                "thursday" to listOf(
                    binding.thursdayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.thursdayEndTimeSpinner.selectedItem.toString().toInt()
                ),
                "friday" to listOf(
                    binding.fridayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.fridayEndTimeSpinner.selectedItem.toString().toInt()
                ),
                "saturday" to listOf(
                    binding.saturdayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.saturdayEndTimeSpinner.selectedItem.toString().toInt()
                ),
                "sunday" to listOf(
                    binding.sundayStartTimeSpinner.selectedItem.toString().toInt(),
                    binding.sundayEndTimeSpinner.selectedItem.toString().toInt()
                )
            )
            viewModelSignUpStoreOwner3.registerButtonClicked(storeSchedule = schedule)
        }
    }

    private fun setObservers() {
        viewModelSignUpStoreOwner3.navigateToActivitySignUpStoreOwner2.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUpStoreOwner2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelSignUpStoreOwner3.onNavigated()
            }
        }
        viewModelSignUpStoreOwner3.navigateToActivityHome.observe(this) { navigate ->
            if (navigate) {
                Toast.makeText(this, getString(R.string.sign_up_store_owner_3_register_succesfully), Toast.LENGTH_SHORT).show()
                val homeIntent = Intent(this, ActivityHomeUniandesMember::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(homeIntent)
                viewModelSignUpStoreOwner3.onNavigated()
            }
        }
        viewModelSignUpStoreOwner3.message.observe(this) { message ->
            when (message) {
                "monday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_monday_error_hours))
                "tuesday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_tuesday_error_hours))
                "wednesday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_wednesday_error_hours))
                "thursday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_thursday_error_hours))
                "friday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_friday_error_hours))
                "saturday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_saturday_error_hours))
                "sunday_error_hours" -> showMessage(getString(R.string.sign_up_store_owner_3_sunday_error_hours))
                "error_firebase_auth" -> showMessage(getString(R.string.sign_up_store_owner_3_error_firebase_auth))
                "error_firebase_firestore" -> showMessage(getString(R.string.sign_up_store_owner_3_error_firebase_firestore))
            }
        }
    }

    private fun showMessage(message: String, type: String = "info") {
        CustomDialog.showCustomDialog(supportFragmentManager, message, type)
    }
}