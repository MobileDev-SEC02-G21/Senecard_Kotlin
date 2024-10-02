package com.mobiles.senecard.model.entities

data class Advertisement(
    val advertisementId: String = "",   // Unique ID for the advertisement
    val storeId: String = "",           // Foreign key for the store it belongs to
    val title: String = "",             // Advertisement title
    val description: String = "",       // Description of the ad
    val imageUrl: String = "",          // URL to the ad image
    val startDate: String = "",         // Start date (format: yyyyMMdd)
    val endDate: String = "",           // End date (format: yyyyMMdd)
    val available: Boolean = true       // Is the advertisement active?
)