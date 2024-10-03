package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList.ActivityHomeUniandesMemberAdvertisementList
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList.ActivityHomeUniandesMemberStoreList
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberBinding
import com.mobiles.senecard.databinding.AdvertisementItemBinding
import com.mobiles.senecard.databinding.StoreItemBinding
import com.mobiles.senecard.model.entities.Advertisement
import com.mobiles.senecard.model.entities.Store

class ActivityHomeUniandesMember : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberBinding
    private val viewModelHomeUniandesMember: ViewModelHomeUniandesMember by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
        setElementsMenu()
        setObserversMenu()
        viewModelHomeUniandesMember.validateSession()
        viewModelHomeUniandesMember.getStoresRecommended()
        viewModelHomeUniandesMember.getAdvertisementRecommended()
    }

    private fun setElements() {
        binding.storeRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.advertisementRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.optionsImageView.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.storesEditText.setOnClickListener {
            viewModelHomeUniandesMember.storesEditTextClicked()
        }
        binding.advertisementsEditText.setOnClickListener {
            viewModelHomeUniandesMember.advertisementsEditTextClicked()
        }
    }

    private fun setObservers() {
        viewModelHomeUniandesMember.isUserLogged.observe(this) { user ->
            if (user == null) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            } else {
                binding.nameTextView.text = "Hey ${user.name},"
                binding.greetingTextView.text = viewModelHomeUniandesMember.getGreeting()
            }
        }
        viewModelHomeUniandesMember.navigateToActivityHomeUniandesMemberStoreList.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityHomeUniandesMemberStoreList::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMember.onNavigated()
            }
        }
        viewModelHomeUniandesMember.navigateToActivityHomeUniandesMemberAdvertisementList.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityHomeUniandesMemberAdvertisementList::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMember.onNavigated()
            }
        }
        viewModelHomeUniandesMember.storeListRecommended.observe(this) { stores ->
            binding.storesLoadingAnimation.visibility = View.GONE
            binding.storeRecyclerView.adapter = StoreAdapter(stores)
        }
        viewModelHomeUniandesMember.advertisementListRecommended.observe(this) { advertisements ->
            binding.advertisementsLoadingAnimation.visibility = View.GONE
            binding.advertisementRecyclerView.adapter = AdvertisementAdapter(advertisements)
        }
    }
    
    private fun setElementsMenu() {
        binding.backImageView.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.homeButton.setOnClickListener {
            Toast.makeText(this, "Hole", Toast.LENGTH_SHORT).show()
        }

        binding.qrCodeButton.setOnClickListener {
            Toast.makeText(this, "QR Code", Toast.LENGTH_SHORT).show()
        }

        binding.loyaltyCardsButton.setOnClickListener {
            Toast.makeText(this, "Loyalty Cards", Toast.LENGTH_SHORT).show()
        }

        binding.profileButton.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }

        binding.logOutButton.setOnClickListener {
            viewModelHomeUniandesMember.logOut()
        }
    }

    private fun setObserversMenu() {
        viewModelHomeUniandesMember.isLoggedOut.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
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

            val closed = viewModelHomeUniandesMember.isStoreClosed(store)

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

    inner class AdvertisementAdapter(private val advertisements: List<Advertisement>) : RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>() {
        inner class AdvertisementViewHolder(val binding: AdvertisementItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementViewHolder {
            val binding = AdvertisementItemBinding.inflate(layoutInflater, parent, false)
            return AdvertisementViewHolder(binding)
        }

        override  fun onBindViewHolder(holder: AdvertisementViewHolder, position: Int) {
            val advertisement = advertisements[position]

            val closed = viewModelHomeUniandesMember.isAdvertisementStoreClosed(advertisement)

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