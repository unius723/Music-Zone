package org.wit.musiczone.main

import android.app.Application
import com.google.firebase.FirebaseApp
import timber.log.Timber

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("MusicZone started")
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Timber.i("Firebase initialized")
    }
}