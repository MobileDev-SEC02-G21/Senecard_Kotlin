package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementDetail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberAdvertisementDetailBinding
import com.mobiles.senecard.model.entities.Advertisement

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
            println(advertisement)
            if (advertisement != null) {
                this.advertisement = advertisement

                Glide.with(binding.advertisementImageView.context)
                    .load(advertisement.image)
                    .placeholder(R.mipmap.icon_image_landscape)
                    .into(binding.advertisementImageView)

                binding.titleAdvertisementTextView.text = advertisement.title
                binding.storeAdvertisementTextView.text = advertisement.store?.name ?: "null"
                binding.descriptionAdvertisementTextView.text = advertisement.description
                binding.startDateAdvertisementTextView.text = advertisement.startDate
                binding.endDateAdvertisementTextView.text = advertisement.endDate

                binding.loadingAnimation.visibility = View.GONE
                binding.backgroundLinearLayout.visibility = View.GONE
            }
        }

        viewModelHomeUniandesMemberAdvertisementDetail.navigateToActivityBack.observe(this) { navigate ->
            if (navigate) {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }

        }
    }
}