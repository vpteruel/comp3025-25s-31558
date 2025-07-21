package com.example.facebook_app

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        // Enable automatic event logging
        AppEventsLogger.activateApp(this)
    }
}