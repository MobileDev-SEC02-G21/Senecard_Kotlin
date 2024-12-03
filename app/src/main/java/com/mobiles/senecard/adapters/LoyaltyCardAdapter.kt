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
    private val onCardClick: (LoyaltyCard) -> Unit // Listener para manejar clics
) : RecyclerView.Adapter<LoyaltyCardAdapter.LoyaltyCardViewHolder>() {

    companion object {
        const val EXTRA_STORE_NAME = "STORE_NAME"
        const val EXTRA_STORE_ADDRESS = "STORE_ADDRESS"
        const val EXTRA_STORE_IMAGE = "STORE_IMAGE"
        const val EXTRA_POINTS = "POINTS"
        const val EXTRA_MAX_POINTS = "MAX_POINTS"
    }

    /**
     * Permite actualizar los datos del adaptador dinámicamente sin necesidad de recrear la instancia.
     */
    fun updateData(newLoyaltyCards: List<LoyaltyCard>, newStores: Map<String, Store>) {
        this.loyaltyCards = newLoyaltyCards
        this.stores = newStores
        notifyDataSetChanged() // Notifica al adaptador para actualizar la lista.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLoyaltyCardBinding.inflate(layoutInflater, parent, false)
        return LoyaltyCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoyaltyCardViewHolder, position: Int) {
        val loyaltyCard = loyaltyCards[position]
        val store = stores[loyaltyCard.storeId]

        // Cambiar el fondo según los puntos de la tarjeta
        holder.itemView.setBackgroundResource(
            if (loyaltyCard.points == loyaltyCard.maxPoints) R.drawable.border_yellow else R.drawable.border_gray
        )

        holder.render(loyaltyCard, store)
    }

    override fun getItemCount(): Int = loyaltyCards.size

    inner class LoyaltyCardViewHolder(private val binding: ItemLoyaltyCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun render(loyaltyCard: LoyaltyCard, store: Store?) {
            // Establecer el nombre de la tienda
            binding.storeNameTextView.text = store?.name ?: "Desconocido"

            // Cargar la imagen de la tienda
            store?.image?.let {
                Glide.with(binding.storeImageView.context)
                    .load(it)
                    .placeholder(R.mipmap.icon_image_landscape) // Imagen de carga predeterminada
                    .into(binding.storeImageView)
            } ?: run {
                binding.storeImageView.setImageResource(R.mipmap.icon_image_landscape)
            }

            // Mostrar puntos (Lleno o "points/maxPoints")
            binding.loyaltyCardPointsText.text = if (loyaltyCard.points == loyaltyCard.maxPoints) {
                "Lleno"
            } else {
                "${loyaltyCard.points}/${loyaltyCard.maxPoints}"
            }

            // Manejar clics en la tarjeta
            itemView.setOnClickListener {
                Log.d("LoyaltyCardAdapter", "Card clicked: ${loyaltyCard.id}")
                onCardClick(loyaltyCard) // Llamar al listener
            }
        }
    }
}
