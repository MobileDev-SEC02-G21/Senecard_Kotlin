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
    private val loyaltyCards: List<LoyaltyCard>,
    private val stores: Map<String, Store>,
    private val onCardClick: (LoyaltyCard) -> Unit // Agregar el listener aquí
) : RecyclerView.Adapter<LoyaltyCardAdapter.LoyaltyCardViewHolder>() {

    companion object {
        const val EXTRA_STORE_NAME = "STORE_NAME"
        const val EXTRA_STORE_ADDRESS = "STORE_ADDRESS"
        const val EXTRA_STORE_IMAGE = "STORE_IMAGE"
        const val EXTRA_POINTS = "POINTS"
        const val EXTRA_MAX_POINTS = "MAX_POINTS"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLoyaltyCardBinding.inflate(layoutInflater, parent, false)
        return LoyaltyCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoyaltyCardViewHolder, position: Int) {
        val loyaltyCard = loyaltyCards[position]
        val store = stores[loyaltyCard.storeId]

        // Aplicar borde amarillo si los puntos son iguales a maxPoints, gris si aún no llega a maxPoints
        holder.itemView.setBackgroundResource(
            if (loyaltyCard.points == loyaltyCard.maxPoints) R.drawable.border_gray else R.drawable.border_yellow
        )

        holder.render(loyaltyCard, store)
    }

    override fun getItemCount(): Int {
        return loyaltyCards.size
    }

    inner class LoyaltyCardViewHolder(private val binding: ItemLoyaltyCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun render(loyaltyCard: LoyaltyCard, store: Store?) {
            // Establecer el nombre de la tienda
            binding.storeNameTextView.text = store?.name ?: "Desconocido"

            // Cargar la imagen de la tienda
            store?.image?.let {
                Glide.with(binding.storeImageView.context)
                    .load(it)
                    .placeholder(R.mipmap.icon_image_landscape)
                    .into(binding.storeImageView)
            } ?: run {
                binding.storeImageView.setImageResource(R.mipmap.icon_image_landscape)
            }

            // Manejar el clic en la tarjeta
            itemView.setOnClickListener {
                Log.d("LoyaltyCardAdapter", "Card clicked: ${loyaltyCard.id}")
                onCardClick(loyaltyCard) // Llamar al listener aquí
            }
        }
    }
}





