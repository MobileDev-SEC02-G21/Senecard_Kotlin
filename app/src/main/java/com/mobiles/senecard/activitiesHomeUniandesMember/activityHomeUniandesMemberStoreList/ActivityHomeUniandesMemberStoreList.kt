package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberStoreListBinding
import com.mobiles.senecard.databinding.StoreItemBinding
import com.mobiles.senecard.model.entities.Store

class ActivityHomeUniandesMemberStoreList : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberStoreListBinding
    private val viewModelHomeUniandesMemberStoreList: ViewModelHomeUniandesMemberStoreList by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberStoreListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
        viewModelHomeUniandesMemberStoreList.getAllStores()
    }

    private fun setElements() {
        binding.storeRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.backImageView.setOnClickListener {
            viewModelHomeUniandesMemberStoreList.backImageViewClicked()
        }
    }

    private fun setObservers() {
        viewModelHomeUniandesMemberStoreList.navigateToActivityHomeUniandesMember.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityHomeUniandesMember::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMemberStoreList.onNavigated()
            }
        }
        viewModelHomeUniandesMemberStoreList.storeList.observe(this) { stores ->
            binding.loadingAnimation.visibility = View.GONE
            binding.storeRecyclerView.adapter = StoreAdapter(stores)
        }
    }

    inner class StoreAdapter(private val stores: List<Store>) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {
        inner class StoreViewHolder(val binding: StoreItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
            val binding = StoreItemBinding.inflate(layoutInflater, parent, false)
            return StoreViewHolder(binding)
        }

        override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
            val store = stores[position]

            val closed = viewModelHomeUniandesMemberStoreList.isStoreClosed(store)

            if (closed) {
                holder.binding.closedTextView.visibility = View.VISIBLE
                holder.binding.storeNameTextView.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.secondary))
                holder.binding.storeRatingTextView.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.secondary))
                holder.binding.starImageView.setImageResource(R.mipmap.icon_star_closed)
            }

            holder.binding.storeNameTextView.text = store.name
            holder.binding.storeRatingTextView.text = store.rating.toString()

            Glide.with(holder.binding.storeImageView.context)
                .load(store.image)
                .placeholder(R.mipmap.icon_image_landscape)
                .into(holder.binding.storeImageView)
        }

        override fun getItemCount(): Int = stores.size
    }
}