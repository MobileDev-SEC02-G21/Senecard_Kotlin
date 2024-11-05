package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementDetail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberAdvertisementDetailBinding
import com.mobiles.senecard.model.entities.Advertisement
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityHomeUniandesMemberAdvertisementDetail : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberAdvertisementDetailBinding
    private val viewModelHomeUniandesMemberAdvertisementDetail: ViewModelHomeUniandesMemberAdvertisementDetail by viewModels()

    private lateinit var advertisement: Advertisement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberAdvertisementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        viewModelHomeUniandesMemberAdvertisementDetail.getAdvertisementById(intent.getStringExtra("advertisementId")!!)

        binding.backImageView.setOnClickListener {
            viewModelHomeUniandesMemberAdvertisementDetail.backImageViewClicked()
        }
    }

    private fun setObservers() {
        viewModelHomeUniandesMemberAdvertisementDetail.advertisement.observe(this) { advertisement ->
            if (advertisement != null) {
                this.advertisement = advertisement

                Glide.with(binding.advertisementImageView.context)
                    .load(advertisement.image)
                    .placeholder(R.mipmap.icon_image_landscape)
                    .into(binding.advertisementImageView)

                binding.titleAdvertisementTextView.text = advertisement.title
                binding.descriptionAdvertisementTextView.text = advertisement.description
                binding.startDateAdvertisementTextView.text = advertisement.startDate?.let {
                    formatDate(
                        it
                    )
                }
                binding.endDateAdvertisementTextView.text = advertisement.endDate?.let {
                    formatDate(
                        it
                    )
                }

                binding.loadingAnimation.visibility = View.GONE
                binding.backgroundLinearLayout.visibility = View.GONE
            }
        }
        viewModelHomeUniandesMemberAdvertisementDetail.storeName.observe(this) { storeName ->
            binding.storeAdvertisementTextView.text = storeName ?: ""
        }
        viewModelHomeUniandesMemberAdvertisementDetail.navigateToActivityBack.observe(this) { navigate ->
            if (navigate) {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }

        }
    }
    private fun formatDate(dateStr: String): String {
        return try {

            val inputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)

            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: "")
        } catch (e: Exception) {
            dateStr
        }
    }
}