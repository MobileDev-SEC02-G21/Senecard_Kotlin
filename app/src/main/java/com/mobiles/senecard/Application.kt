package com.mobiles.senecard

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        val sharedPreferences = getSharedPreferences("SettingsPreferences", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("language", "en") ?: "en"
        val theme = sharedPreferences.getString("theme", "Light") ?: "Light"

        setLanguage(languageCode)
        setTheme(theme)
    }

    fun setLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun setTheme(theme: String) {
        if (theme == "Dark") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (theme == "Light") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun getInstance(): MyApplication {
            return instance!!
        }
    }
}