package org.wit.musiczone.main

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

import timber.log.Timber
import java.util.Calendar

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        Timber.i("MusicZone started")

        applyDayNightMode()

    }

    private fun applyDayNightMode() {
        val isNight = isNightByTime()

        AppCompatDelegate.setDefaultNightMode(
            if (isNight)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
        Timber.i("Night mode = $isNight")
    }

    private fun isNightByTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 18
    }
}