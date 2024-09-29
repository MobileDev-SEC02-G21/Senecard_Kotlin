package com.mobiles.senecard.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseClient private constructor() {

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    val storage: StorageReference get() = FirebaseStorage.getInstance().reference

    companion object {
        val instance: FirebaseClient by lazy { FirebaseClient() }
    }
}