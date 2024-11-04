package com.mobiles.senecard

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}
