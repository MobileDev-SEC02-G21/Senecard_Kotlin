package com.mobiles.senecard.utils

import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {
    private const val PREFERENCES_FILE = "UserSessionPreferences"
    private const val USER_ID_KEY = "userId"

    fun saveUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        prefs.edit().putString(USER_ID_KEY, userId).apply()
    }

    fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        return prefs.getString(USER_ID_KEY, null)
    }

    fun clearUserId(context: Context) {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        prefs.edit().remove(USER_ID_KEY).apply()
    }
}
