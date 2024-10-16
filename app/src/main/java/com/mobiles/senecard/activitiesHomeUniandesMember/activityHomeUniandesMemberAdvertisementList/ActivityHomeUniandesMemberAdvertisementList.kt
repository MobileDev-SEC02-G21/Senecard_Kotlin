package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.AdvertisementAdapter
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementDetail.ActivityHomeUniandesMemberAdvertisementDetail
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberAdvertisementListBinding

class ActivityHomeUniandesMemberAdvertisementList : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberAdvertisementListBinding
    private val viewModelHomeUniandesMemberAdvertisementList: ViewModelHomeUniandesMemberAdvertisementList by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberAdvertisementListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
        viewModelHomeUniandesMemberAdvertisementList.getAllAdvertisements()
    }

    private fun setElements() {
        binding.advertisementRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModelHomeUniandesMemberAdvertisementList.filterAdvertisementsByTitle(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.backImageView.setOnClickListener {
            viewModelHomeUniandesMemberAdvertisementList.backImageViewClicked()
        }
    }

    private fun setObservers() {
        viewModelHomeUniandesMemberAdvertisementList.navigateToActivityHomeUniandesMember.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityHomeUniandesMember::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMemberAdvertisementList.onNavigated()
            }
        }
        viewModelHomeUniandesMemberAdvertisementList.advertisementList.observe(this) { advertisements ->
            binding.loadingAnimation.visibility = View.GONE
            binding.advertisementRecyclerView.adapter = AdvertisementAdapter(advertisements) { advertisement ->
                viewModelHomeUniandesMemberAdvertisementList.onClickedItemAdvertisement(advertisement)
            }
        }
        viewModelHomeUniandesMemberAdvertisementList.filteredAdvertisementList.observe(this) { filteredAdvertisements ->
            binding.advertisementRecyclerView.adapter = AdvertisementAdapter(filteredAdvertisements) { advertisement ->
                viewModelHomeUniandesMemberAdvertisementList.onClickedItemAdvertisement(advertisement)
            }
        }
        viewModelHomeUniandesMemberAdvertisementList.navigateToActivityHomeUniandesMemberAdvertisementDetail.observe(this) { advertisement ->
            if (advertisement != null) {
                val intent = Intent(this, ActivityHomeUniandesMemberAdvertisementDetail::class.java)
                intent.putExtra("advertisementId", advertisement.id)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMemberAdvertisementList.onNavigated()
            }
        }
    }
}