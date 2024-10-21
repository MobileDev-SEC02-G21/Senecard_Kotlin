package com.mobiles.senecard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ItemLoyaltyCardBinding
import com.mobiles.senecard.model.entities.RoyaltyCard
import com.mobiles.senecard.model.entities.Store
class LoyaltyCardAdapter(
    private val royaltyCards: List<RoyaltyCard>,
    private val stores: Map<String, Store>,
    private val onClickedLoyaltyCard: (RoyaltyCard) -> Unit
) : RecyclerView.Adapter<LoyaltyCardAdapter.LoyaltyCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoyaltyCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLoyaltyCardBinding.inflate(layoutInflater, parent, false)
        return LoyaltyCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoyaltyCardViewHolder, position: Int) {
        val royaltyCard = royaltyCards[position]
        val store = stores[royaltyCard.storeId]

        // Verificar si los puntos son iguales al máximo permitido (maxPoints)
        if (royaltyCard.points == royaltyCard.maxPoints) {
            // Si los puntos son iguales a maxPoints, aplicar el borde amarillo
            holder.itemView.setBackgroundResource(R.drawable.border_yellow)
        } else {
            // Si no, eliminar cualquier fondo especial (sin borde)
            holder.itemView.setBackgroundResource(0)  // o usa el fondo predeterminado
        }

        holder.render(royaltyCard, store, onClickedLoyaltyCard)
    }

    override fun getItemCount(): Int {
        return royaltyCards.size
    }

    class LoyaltyCardViewHolder(private val binding: ItemLoyaltyCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun render(royaltyCard: RoyaltyCard, store: Store?, onClickedLoyaltyCard: (RoyaltyCard) -> Unit) {
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
                onClickedLoyaltyCard(royaltyCard)
            }
        }
    }
}



