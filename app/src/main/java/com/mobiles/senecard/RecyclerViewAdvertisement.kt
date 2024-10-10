package com.mobiles.senecard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.databinding.ItemAdvertisementBinding
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.entities.Advertisement

class AdvertisementAdapter(private val advertisements: List<Advertisement>, private val onClickedItemAdvertisement:(Advertisement) -> Unit) : RecyclerView.Adapter<AdvertisementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AdvertisementViewHolder(layoutInflater.inflate(R.layout.item_advertisement, parent, false))
    }

    override fun onBindViewHolder(holder: AdvertisementViewHolder, position: Int) {
        val advertisement = advertisements[position]
        holder.render(advertisement, onClickedItemAdvertisement)
    }

    override fun getItemCount(): Int {
        return advertisements.size
    }

}

class AdvertisementViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val repositoryAdvertisement = RepositoryAdvertisement.instance
    private val binding = ItemAdvertisementBinding.bind(view)

    fun render(advertisement: Advertisement, onClickedItemAdvertisement: (Advertisement) -> Unit) {

        val closed = repositoryAdvertisement.isAdvertisementStoreClosed(advertisement)

        if (closed) {
            binding.overlayView.visibility = View.VISIBLE
            binding.closedTextView.visibility = View.VISIBLE
        } else {
            binding.overlayView.visibility = View.GONE
            binding.closedTextView.visibility = View.GONE
        }

        Glide.with(binding.advertisementImageView.context)
            .load(advertisement.image)
            .placeholder(R.mipmap.icon_image_landscape)
            .into(binding.advertisementImageView)

        itemView.setOnClickListener {
            onClickedItemAdvertisement(advertisement)
        }
    }
}