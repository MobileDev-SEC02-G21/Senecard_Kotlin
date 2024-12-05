package com.mobiles.senecard.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ItemLoyaltyCardBinding
import com.mobiles.senecard.model.entities.LoyaltyCard
import com.mobiles.senecard.model.entities.Store

class LoyaltyCardAdapter(
    private var loyaltyCards: List<LoyaltyCard>,
    private var stores: Map<String, Store>,
    private val onCardClick: (LoyaltyCard) -> Unit
) : RecyclerView.Adapter<LoyaltyCardAdapter.LoyaltyCardViewHolder>() {

    companion object {
        const val EXTRA_STORE_NAME = "STORE_NAME"
        const val EXTRA_STORE_ADDRESS = "STORE_ADDRESS"
        const val EXTRA_STORE_IMAGE = "STORE_IMAGE"
        const val EXTRA_POINTS = "POINTS"
        const val EXTRA_MAX_POINTS = "MAX_POINTS"
    }


    fun updateData(newLoyaltyCards: List<LoyaltyCard>, newStores: Map<String, Store>) {
        this.loyaltyCards = newLoyaltyCards
        this.stores = newStores
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLoyaltyCardBinding.inflate(layoutInflater, parent, false)
        return LoyaltyCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoyaltyCardViewHolder, position: Int) {
        val loyaltyCard = loyaltyCards[position]
        val store = stores[loyaltyCard.storeId]

        holder.itemView.setBackgroundResource(
            if (loyaltyCard.points == loyaltyCard.maxPoints) R.drawable.border_yellow else R.drawable.border_gray
        )

        holder.render(loyaltyCard, store)
    }

    override fun getItemCount(): Int = loyaltyCards.size

    inner class LoyaltyCardViewHolder(private val binding: ItemLoyaltyCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun render(loyaltyCard: LoyaltyCard, store: Store?) {
            binding.storeNameTextView.text = store?.name ?: "Desconocido"

            store?.image?.let {
                Glide.with(binding.storeImageView.context)
                    .load(it)
                    .placeholder(R.mipmap.icon_image_landscape)
                    .into(binding.storeImageView)
            } ?: run {
                binding.storeImageView.setImageResource(R.mipmap.icon_image_landscape)
            }

            binding.loyaltyCardPointsText.text = if (loyaltyCard.points == loyaltyCard.maxPoints) {
                "full"
            } else {
                "${loyaltyCard.points}/${loyaltyCard.maxPoints}"
            }

            itemView.setOnClickListener {
                Log.d("LoyaltyCardAdapter", "Card clicked: ${loyaltyCard.id}")
                onCardClick(loyaltyCard)
            }
        }
    }
}
