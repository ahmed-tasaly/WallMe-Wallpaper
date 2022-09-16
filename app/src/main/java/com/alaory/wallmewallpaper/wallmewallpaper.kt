package com.alaory.wallmewallpaper

import android.app.Application
import com.alaory.wallmewallpaper.settings.Reddit_settings
import com.alaory.wallmewallpaper.settings.wallhaven_settings

class wallmewallpaper : Application() {
    override fun onCreate() {
        super.onCreate()
        //update settings
        Reddit_settings.loadprefs(this);
        wallhaven_settings.loadprefs(this);
    }
}