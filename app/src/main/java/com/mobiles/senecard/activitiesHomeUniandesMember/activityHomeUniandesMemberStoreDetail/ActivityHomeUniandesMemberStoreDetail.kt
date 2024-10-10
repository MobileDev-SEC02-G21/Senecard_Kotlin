package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreDetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberStoreDetailBinding
import com.mobiles.senecard.model.entities.Store

class ActivityHomeUniandesMemberStoreDetail : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberStoreDetailBinding
    private val viewModelHomeUniandesMemberStoreDetail: ViewModelHomeUniandesMemberStoreDetail by viewModels()

    private lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberStoreDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        viewModelHomeUniandesMemberStoreDetail.getStoreById(intent.getStringExtra("storeId")!!)

        binding.backImageView.setOnClickListener {
            viewModelHomeUniandesMemberStoreDetail.backImageViewClicked()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun setObservers() {
        viewModelHomeUniandesMemberStoreDetail.store.observe(this) { store ->
            if (store != null) {
                this.store = store

                Glide.with(binding.storeImageView.context)
                    .load(store.image)
                    .placeholder(R.mipmap.icon_image_landscape)
                    .into(binding.storeImageView)

                binding.titleStoreTextView.text = store.name
                binding.categoryStoreTextView.text = store.category
                binding.ratingStoreTextView.text = store.rating.toString()
                binding.addressStoreTextView.text = store.address

                val startMondayHour = store.schedule?.get("monday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endMondayHour = store.schedule?.get("monday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleMondayStoreTextView.text = "Monday - $startMondayHour to $endMondayHour"

                val startTuesdayHour = store.schedule?.get("tuesday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endTuesdayHour = store.schedule?.get("tuesday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleTuesdayStoreTextView.text = "Tuesday - $startTuesdayHour to $endTuesdayHour"

                val startWednesdayHour = store.schedule?.get("wednesday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endWednesdayHour = store.schedule?.get("wednesday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleWednesdayStoreTextView.text = "Wednesday - $startWednesdayHour to $endWednesdayHour"

                val startThursdayHour = store.schedule?.get("thursday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endThursdayHour = store.schedule?.get("thursday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleThursdayStoreTextView.text = "Thursday - $startThursdayHour to $endThursdayHour"

                val startFridayHour = store.schedule?.get("friday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endFridayHour = store.schedule?.get("friday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleFridayStoreTextView.text = "Friday - $startFridayHour to $endFridayHour"

                val startSaturdayHour = store.schedule?.get("saturday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endSaturdayHour = store.schedule?.get("saturday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleSaturdayStoreTextView.text = "Saturday - $startSaturdayHour to $endSaturdayHour"

                val startSundayHour = store.schedule?.get("sunday")?.let { String.format("%02d:00", it[0]) } ?: "N/A"
                val endSundayHour = store.schedule?.get("sunday")?.let { String.format("%02d:00", it[1]) } ?: "N/A"
                binding.scheduleSundayStoreTextView.text = "Sunday - $startSundayHour to $endSundayHour"

                binding.loadingAnimation.visibility = View.GONE
                binding.backgroundLinearLayout.visibility = View.GONE
            }
        }

        viewModelHomeUniandesMemberStoreDetail.navigateToActivityBack.observe(this) { navigate ->
            if (navigate) {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }
}