package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberAdvertisementListBinding
import com.mobiles.senecard.databinding.AdvertisementItemBinding
import com.mobiles.senecard.model.entities.Advertisement

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
            binding.advertisementRecyclerView.adapter = AdvertisementAdapter(advertisements)

        }
    }

    inner class AdvertisementAdapter(private val advertisements: List<Advertisement>) : RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>() {
        inner class AdvertisementViewHolder(val binding: AdvertisementItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementViewHolder {
            val binding = AdvertisementItemBinding.inflate(layoutInflater, parent, false)
            return AdvertisementViewHolder(binding)
        }

        override  fun onBindViewHolder(holder: AdvertisementViewHolder, position: Int) {
            val advertisement = advertisements[position]

            val closed = viewModelHomeUniandesMemberAdvertisementList.isAdvertisementStoreClosed(advertisement)

            if (closed) {
                holder.binding.overlayView.visibility = View.VISIBLE
                holder.binding.closedTextView.visibility = View.VISIBLE
            }

            Glide.with(holder.binding.advertisementImageView.context)
                .load(advertisement.image)
                .placeholder(R.mipmap.icon_image_landscape)
                .into(holder.binding.advertisementImageView)
        }

        override fun getItemCount(): Int = advertisements.size
    }
}