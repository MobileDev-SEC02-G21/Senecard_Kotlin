package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerProfile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile.ActivityBusinessOwnerEditProfile
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.databinding.ActivityBusinessOwnerProfileBinding

class ActivityBusinessOwnerProfile : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerProfileBinding
    private val viewModel: ViewModelBusinessOwnerProfile by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupObservers()
        setupListeners()
        viewModel.fetchProfileData() // Trigger data fetch
    }

    private fun setupBinding() {
        binding = ActivityBusinessOwnerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservers() {
        // Observe navigation LiveData
        viewModel.navigateTo.observe(this) { destination ->
            destination?.let {
                when (it) {
                    NavigationDestination.EDIT_PROFILE -> navigateToActivity(ActivityBusinessOwnerEditProfile::class.java)
                    NavigationDestination.LANDING_PAGE -> navigateToActivity(ActivityBusinessOwnerLandingPage::class.java)
                }
                viewModel.onNavigationHandled()
            }
        }

        // Observe user data
        viewModel.user.observe(this) { user ->
            user?.let {
                binding.apply {
                    fullNameTextView.text = it.name ?: "No name available"
                    emailTextView.text = it.email ?: "No email available"
                    phoneTextView.text = it.phone ?: "No phone number available"
                }
            } ?: run {
                binding.apply {
                    fullNameTextView.text = "No name available"
                    emailTextView.text = "No email available"
                    phoneTextView.text = "No phone number available"
                }
            }
        }

        // Observe store data
        viewModel.store.observe(this) { store ->
            store?.let {
                binding.apply {
                    storeNameTextView.text = it.name ?: "No store name"
                    addressTextView.text = it.address ?: "No address available"

                    // Load store image
                    Glide.with(this@ActivityBusinessOwnerProfile)
                        .load(it.image)
                        .placeholder(R.drawable.no_image_placeholder)
                        .into(storeImageView)

                    // Populate schedule
                    it.schedule?.let { schedule ->
                        mondayScheduleTextView.text = formatSchedule(schedule["monday"])
                        tuesdayScheduleTextView.text = formatSchedule(schedule["tuesday"])
                        wednesdayScheduleTextView.text = formatSchedule(schedule["wednesday"])
                        thursdayScheduleTextView.text = formatSchedule(schedule["thursday"])
                        fridayScheduleTextView.text = formatSchedule(schedule["friday"])
                        saturdayScheduleTextView.text = formatSchedule(schedule["saturday"])
                        sundayScheduleTextView.text = formatSchedule(schedule["sunday"])
                    } ?: run {
                        // If schedule is null, mark all days as "Closed"
                        mondayScheduleTextView.text = "Closed"
                        tuesdayScheduleTextView.text = "Closed"
                        wednesdayScheduleTextView.text = "Closed"
                        thursdayScheduleTextView.text = "Closed"
                        fridayScheduleTextView.text = "Closed"
                        saturdayScheduleTextView.text = "Closed"
                        sundayScheduleTextView.text = "Closed"
                    }
                }
            } ?: run {
                // If store data is null, set default placeholders
                binding.apply {
                    storeNameTextView.text = "No store name"
                    addressTextView.text = "No address available"
                    mondayScheduleTextView.text = "Closed"
                    tuesdayScheduleTextView.text = "Closed"
                    wednesdayScheduleTextView.text = "Closed"
                    thursdayScheduleTextView.text = "Closed"
                    fridayScheduleTextView.text = "Closed"
                    saturdayScheduleTextView.text = "Closed"
                    sundayScheduleTextView.text = "Closed"

                    // Use placeholder image for the store
                    Glide.with(this@ActivityBusinessOwnerProfile)
                        .load(R.drawable.no_image_placeholder)
                        .into(storeImageView)
                }
            }
        }
    }

    // Utility function to format the schedule
    private fun formatSchedule(hours: List<Int>?): String {
        return if (hours != null && hours.size == 2) {
            "${hours[0]}:00 - ${hours[1]}:00"
        } else {
            "Closed"
        }
    }

    private fun setupListeners() {
        // Edit Button Click Listener
        binding.editButton.setOnClickListener {
            viewModel.onEditButtonClicked()
        }

        // Back Button Click Listener
        binding.backButton.setOnClickListener {
            viewModel.onBackButtonClicked()
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }
}
