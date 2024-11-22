package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.AdvertisementAdapter
import com.mobiles.senecard.LoyaltyCardsActivity.ActivityLoyaltyCards
import com.mobiles.senecard.activityQRCodeUniandesMember.ActivityQRCodeUniandesMember

import com.mobiles.senecard.R
import com.mobiles.senecard.StoreAdapter
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementDetail.ActivityHomeUniandesMemberAdvertisementDetail
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList.ActivityHomeUniandesMemberAdvertisementList
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreDetail.ActivityHomeUniandesMemberStoreDetail
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList.ActivityHomeUniandesMemberStoreList
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberBinding

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
        viewModelHomeUniandesMember.getInformation()
    }

    private fun setElements() {
        binding.storeRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.advertisementRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.qrCodeImageView.setOnClickListener {
            viewModelHomeUniandesMember.qrCodeImageViewClicked()
        }
        binding.optionsImageView.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.storesEditText.setOnClickListener {
            viewModelHomeUniandesMember.storesEditTextClicked()
        }
        binding.advertisementsEditText.setOnClickListener {
            viewModelHomeUniandesMember.advertisementsEditTextClicked()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.white)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.primary))
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModelHomeUniandesMember.getInformation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setObservers() {
        viewModelHomeUniandesMember.isUser.observe(this) { user ->
            if (user != null) {
                val firstName = user.name!!.split(" ").first()
                binding.nameTextView.text = "Hey ${firstName},"
                binding.greetingTextView.text = viewModelHomeUniandesMember.getGreeting()
            }
        }
        viewModelHomeUniandesMember.navigateToActivityQrCodeUniandesMemberImageView.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityQRCodeUniandesMember::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_down,
                    R.anim.slide_out_up
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMember.onNavigated()
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
            if (stores.size == 2) {
                binding.errorConnectionStores.visibility = View.GONE
                binding.messageNoConnectionStores.visibility = View.GONE
                binding.storeRecyclerView.adapter = StoreAdapter(stores) { store ->
                    viewModelHomeUniandesMember.storeItemClicked(store)
                }
            } else {
                binding.errorConnectionStores.visibility = View.VISIBLE
                binding.messageNoConnectionStores.visibility = View.VISIBLE
            }
        }
        viewModelHomeUniandesMember.advertisementListRecommended.observe(this) { advertisements ->
            binding.advertisementsLoadingAnimation.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            if (advertisements.size == 2) {
                binding.errorConnectionAdvertisements.visibility = View.GONE
                binding.messageNoConnectionAdvertisements.visibility = View.GONE
                binding.advertisementRecyclerView.adapter = AdvertisementAdapter(advertisements) { advertisement ->
                    viewModelHomeUniandesMember.advertisementItemClicked(advertisement)
                }
            } else {
                Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show()
                binding.errorConnectionAdvertisements.visibility = View.VISIBLE
                binding.messageNoConnectionAdvertisements.visibility = View.VISIBLE
            }
        }
        viewModelHomeUniandesMember.navigateToActivityHomeUniandesMemberStoreDetail.observe(this) { store ->
            if (store != null) {
                val intent = Intent(this, ActivityHomeUniandesMemberStoreDetail::class.java)
                intent.putExtra("storeId", store.id)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMember.onNavigated()
            }
        }
        viewModelHomeUniandesMember.navigateToActivityHomeUniandesMemberAdvertisementDetail.observe(this) { advertisement ->
            if (advertisement != null) {
                val intent = Intent(this, ActivityHomeUniandesMemberAdvertisementDetail::class.java)
                intent.putExtra("advertisementId", advertisement.id)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMember.onNavigated()
            }
        }
    }
    
    private fun setElementsMenu() {
        binding.backImageView.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.homeButton.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.qrCodeButton.setOnClickListener {
            viewModelHomeUniandesMember.qrCodeButtonClicked()
        }

        binding.loyaltyCardsButton.setOnClickListener {
            viewModelHomeUniandesMember.loyaltyCardsButtonClicked()
        }

        binding.profileButton.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }

        binding.logOutButton.setOnClickListener {
            viewModelHomeUniandesMember.logOutButtonClicked()
        }
    }

    private fun setObserversMenu() {
        viewModelHomeUniandesMember.navigateToActivityQrCodeUniandesMemberButton.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityQRCodeUniandesMember::class.java)
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
        viewModelHomeUniandesMember.navigateToActivityLoyaltyCardsUniandesMember.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityLoyaltyCards::class.java)
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
        viewModelHomeUniandesMember.navigateToActivityInitial.observe(this) { navigate ->
            if (navigate) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
        }
    }
}