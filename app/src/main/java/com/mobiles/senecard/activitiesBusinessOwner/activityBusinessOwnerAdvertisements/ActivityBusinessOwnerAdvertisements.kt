package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.adapters.AdvertisementAdapter
import com.mobiles.senecard.databinding.ActivityBusinessOwnerAdvertisementsBinding
import com.mobiles.senecard.model.entities.Advertisement

class ActivityBusinessOwnerAdvertisements : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerAdvertisementsBinding
    private var businessOwnerId: String? = null
    private var storeId: String? = null
    private var storeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding
        binding = ActivityBusinessOwnerAdvertisementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the extras
        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")
        storeName = intent.getStringExtra("storeName")

        // Sample advertisement data (for testing purposes)
        val sampleAds = listOf(
            Advertisement("1", "Store1", "Burrito Special", "20% OFF ALL BURRITOS", null, "01/01/2024", "01/31/2024", true),
            Advertisement("2", "Store2", "Drink Deal", "2x1 DRINKS", null, "01/01/2024", "01/31/2024", true)
        )

        // Set up RecyclerView
        binding.rvAdvertisements.layoutManager = LinearLayoutManager(this)
        binding.rvAdvertisements.adapter = AdvertisementAdapter(sampleAds)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
