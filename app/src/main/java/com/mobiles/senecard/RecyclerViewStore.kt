package com.mobiles.senecard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.databinding.ItemStoreBinding
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Store

class StoreAdapter(private val stores: List<Store>, private val onClickedItemStore:(Store) -> Unit) : RecyclerView.Adapter<StoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StoreViewHolder(layoutInflater.inflate(R.layout.item_store, parent, false))
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val store = stores[position]
        holder.render(store, onClickedItemStore)
    }

    override fun getItemCount(): Int {
        return stores.size
    }

}

class StoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val repositoryStore = RepositoryStore.instance
    private val binding = ItemStoreBinding.bind(view)

    fun render(store: Store, onClickedItemStore:(Store) -> Unit) {

        val closed = repositoryStore.isStoreClosed(store)

        if (closed) {
            binding.closedTextView.visibility = View.VISIBLE
            binding.storeNameTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.secondary))
            binding.storeRatingTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.secondary))
            binding.starImageView.setImageResource(R.mipmap.icon_star_closed)
        } else {
            binding.closedTextView.visibility = View.GONE
            binding.storeNameTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text))
            binding.storeRatingTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text))
            binding.starImageView.setImageResource(R.mipmap.icon_star)
        }

        binding.storeNameTextView.text = store.name
        binding.storeRatingTextView.text = store.rating.toString()

        Glide.with(binding.storeImageView.context)
            .load(store.image)
            .placeholder(R.mipmap.icon_image_landscape)
            .into(binding.storeImageView)

        itemView.setOnClickListener {
            onClickedItemStore(store)
        }
    }
}