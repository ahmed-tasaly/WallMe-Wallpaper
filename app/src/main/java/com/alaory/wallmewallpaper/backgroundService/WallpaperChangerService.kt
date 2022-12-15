package com.alaory.wallmewallpaper.backgroundService

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WallpaperChangerService : Service() {

    //TODO add light / black theme support.
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}