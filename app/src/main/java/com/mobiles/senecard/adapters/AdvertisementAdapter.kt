package com.mobiles.senecard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivityBusinessOwnerItemAdvertisementBinding
import com.mobiles.senecard.model.entities.Advertisement
import com.bumptech.glide.Glide

class AdvertisementAdapter(private val advertisements: List<Advertisement>) : RecyclerView.Adapter<AdvertisementAdapter.AdViewHolder>() {

    inner class AdViewHolder(private val binding: ActivityBusinessOwnerItemAdvertisementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: Advertisement) {
            // Set placeholder or load image using Glide
            Glide.with(binding.advertisementImageView.context)
                .load(ad.image ?: R.drawable.no_image_placeholder)
                .into(binding.advertisementImageView)

            // Set title, description, availability, dates
            binding.advertisementTitle.text = ad.title ?: "No title"
            binding.advertisementDescription.text = ad.description ?: "No description"
            binding.advertisementAvailability.text = if (ad.available == true) "Yes" else "No"
            binding.advertisementDates.text = "${ad.startDate} - ${ad.endDate}"

            // Delete button functionality (if needed)
            binding.deleteButton.setOnClickListener {
                // Handle delete action here
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = ActivityBusinessOwnerItemAdvertisementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.bind(advertisements[position])
    }

    override fun getItemCount(): Int = advertisements.size
}
