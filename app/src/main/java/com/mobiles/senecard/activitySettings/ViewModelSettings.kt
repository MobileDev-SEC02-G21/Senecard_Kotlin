package com.mobiles.senecard.activitySettings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiles.senecard.MyApplication

class ViewModelSettings(private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _navigateToActivityBack = MutableLiveData<Boolean>()
    val navigateToActivityBack: LiveData<Boolean>
        get() = _navigateToActivityBack

    private val _language = MutableLiveData(sharedPreferences.getString("language", "en") ?: "en")
    val language: LiveData<String> get() = _language

    private val _theme = MutableLiveData(sharedPreferences.getString("theme", "Light") ?: "Light")
    val theme: LiveData<String> get() = _theme

    private val _pushNotifications = MutableLiveData<Boolean>(sharedPreferences.getBoolean("push_notifications", true))
    val pushNotifications: LiveData<Boolean> get() = _pushNotifications

    private val _rememberNotifications = MutableLiveData<Boolean>(sharedPreferences.getBoolean("remember_notifications", true))
    val rememberNotifications: LiveData<Boolean> get() = _rememberNotifications

    private val _advertisementsNotifications = MutableLiveData<Boolean>(sharedPreferences.getBoolean("advertisements_notifications", true))
    val advertisementsNotifications: LiveData<Boolean> get() = _advertisementsNotifications

    fun updateLanguage(newLanguage: String) {
        _language.value = newLanguage
        MyApplication.getInstance().setLanguage(newLanguage)
        sharedPreferences.edit().putString("language", newLanguage).apply()
    }

    fun updateTheme(newTheme: String) {
        _theme.value = newTheme
        MyApplication.getInstance().setTheme(newTheme)
        sharedPreferences.edit().putString("theme", newTheme).apply()
    }

    fun togglePushNotifications() {
        val newValue = !_pushNotifications.value!!
        _pushNotifications.value = newValue
        sharedPreferences.edit().putBoolean("push_notifications", newValue).apply()
    }

    fun toggleRememberNotifications() {
        val newValue = !_rememberNotifications.value!!
        _rememberNotifications.value = newValue
        sharedPreferences.edit().putBoolean("remember_notifications", newValue).apply()
    }

    fun toggleAdvertisementsNotifications() {
        val newValue = !_advertisementsNotifications.value!!
        _advertisementsNotifications.value = newValue
        sharedPreferences.edit().putBoolean("advertisements_notifications", newValue).apply()
    }

    fun backImageViewClicked() {
        _navigateToActivityBack.value = true
    }
}
