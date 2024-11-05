package com.mobiles.senecard

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    @SuppressLint("ObsoleteSdkInt")
    fun isInternetAvailable(): Boolean {
        val connectivityManager = MyApplication.applicationContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val activeNetwork = connectivityManager.activeNetworkInfo
            activeNetwork?.isConnected == true
        }
    }
}
