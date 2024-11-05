package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisementsCreate.ActivityBusinessOwnerAdvertisementsCreate
import com.mobiles.senecard.adapters.AdvertisementAdapter
import com.mobiles.senecard.databinding.ActivityBusinessOwnerAdvertisementsBinding
import com.mobiles.senecard.model.entities.Advertisement

class ActivityBusinessOwnerAdvertisements : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerAdvertisementsBinding
    private lateinit var adapter: AdvertisementAdapter
    private var advertisements: MutableList<Advertisement> = mutableListOf()
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
        advertisements.addAll(
            listOf(
                Advertisement("1", "Store1", "Burrito Special", "20% OFF ALL BURRITOS", null, "01/01/2024", "01/31/2024", true),
                Advertisement("2", "Store2", "Drink Deal", "2x1 DRINKS", null, "01/01/2024", "01/31/2024", true)
            )
        )

        // Initialize the adapter with delete callback
        adapter = AdvertisementAdapter(advertisements) { advertisementToDelete ->
            deleteAdvertisement(advertisementToDelete)
        }

        // Set up RecyclerView
        binding.rvAdvertisements.layoutManager = LinearLayoutManager(this)
        binding.rvAdvertisements.adapter = adapter

        // Handle back button click
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Handle navigation to the create advertisement view
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerAdvertisementsCreate::class.java)
            intent.putExtra("businessOwnerId", businessOwnerId)
            intent.putExtra("storeId", storeId)
            intent.putExtra("storeName", storeName)
            startActivity(intent)
        }
    }

    private fun deleteAdvertisement(advertisement: Advertisement) {
        // Remove the advertisement from the list
        advertisements.remove(advertisement)
        adapter.updateAdvertisements(advertisements)

        // Optional: Perform additional actions like updating the database
        Toast.makeText(this, "${advertisement.title} deleted", Toast.LENGTH_SHORT).show()
    }
}
