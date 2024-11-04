package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisementsCreate

import androidx.lifecycle.ViewModel
import com.mobiles.senecard.model.entities.Advertisement

class ViewModelBusinessOwnerAdvertisementsCreate : ViewModel() {

    fun saveAdvertisement(title: String, description: String, startDate: String, endDate: String, storeId: String) {
        // Create a new Advertisement object
        val advertisement = Advertisement(
            storeId = storeId, // Replace with actual store ID if needed
            title = title,
            description = description,
            image = null, // Replace with actual image URL if you have image uploading
            startDate = startDate,
            endDate = endDate,
            available = true
        )

        // Here, you would typically add the advertisement to a repository or database
        println("Advertisement saved: $advertisement")
        // Add your logic to save to a database, Firestore, etc.
    }

    private fun generateAdvertisementId(): String {
        // Generate a unique ID for the advertisement (replace with actual logic if needed)
        return System.currentTimeMillis().toString()
    }
}
