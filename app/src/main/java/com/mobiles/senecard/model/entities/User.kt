package com.mobiles.senecard.model.entities

data class User(
    val userId: String = "",        // Unique ID
    val email: String = "",         // User email
    val name: String = "",          // Full name
    val phone: String = "",         // Phone number
    val role: String = "",          // Role: 'businessOwner' or 'uniandesMember'
    val qrCode: String = ""         // Unique QR code for loyalty program scanning
)
