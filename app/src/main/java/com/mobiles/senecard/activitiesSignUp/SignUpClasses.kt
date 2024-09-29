package com.mobiles.senecard.activitiesSignUp

import android.net.Uri

class SignUpUser private constructor() {

    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var password: String? = null

    companion object {
        val instance: SignUpUser by lazy { SignUpUser() }
    }

    fun reset() {
        name = null
        email = null
        phone = null
        password = null
    }
}

class SignUpStore private constructor() {

    var name: String? = null
    var address: String? = null
    var category: String? = null
    var image: Uri? = null

    companion object {
        val instance: SignUpStore by lazy { SignUpStore() }
    }

    fun reset() {
        name = null
        address = null
        category = null
        image = null
    }
}