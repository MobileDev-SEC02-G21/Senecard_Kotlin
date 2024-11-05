package com.mobiles.senecard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivityBusinessOwnerItemAdvertisementBinding
import com.mobiles.senecard.model.entities.Advertisement

class AdvertisementAdapter(
    private var advertisements: MutableList<Advertisement>,
    private val onDeleteClick: (Advertisement) -> Unit
) : RecyclerView.Adapter<AdvertisementAdapter.AdViewHolder>() {

    inner class AdViewHolder(private val binding: ActivityBusinessOwnerItemAdvertisementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: Advertisement) {
            // Load image or set a placeholder
            Glide.with(binding.advertisementImageView.context)
                .load(ad.image ?: R.drawable.no_image_placeholder)
                .into(binding.advertisementImageView)

            // Set title, description, availability, and dates
            binding.advertisementTitle.text = ad.title ?: "No title"
            binding.advertisementDescription.text = ad.description ?: "No description"
            binding.advertisementAvailability.text = if (ad.available == true) "Yes" else "No"
            binding.advertisementDates.text = "${ad.startDate} - ${ad.endDate}"

            // Delete button functionality
            binding.deleteButton.setOnClickListener {
                onDeleteClick(ad) // Call the delete callback with the current advertisement
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

    // Method to update the list after deletion
    fun updateAdvertisements(newAdvertisements: MutableList<Advertisement>) {
        advertisements = newAdvertisements
        notifyDataSetChanged()
    }
}
