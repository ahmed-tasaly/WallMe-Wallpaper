package com.alaory.wallmewallpaper.wallpaper

import android.graphics.Canvas
import android.graphics.Movie
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.io.File

class gifwallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine {
        val mygifengine = gifengine()
        return mygifengine
    }

    inner class gifengine : Engine(){
        val GifPath = this@gifwallpaper.getSharedPreferences("LiveWallpaper",0).getString("Gif_Path","")!!.toString();
        val gifdrawable: Drawable? = AnimatedImageDrawable.createFromPath(GifPath);
        var surfholder : SurfaceHolder? =null;

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder);
            val canvas  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            { surfholder!!.lockHardwareCanvas(); } else { surfholder!!.lockCanvas(); }

            canvas?.let {

                gifdrawable.let { gif ->
                    Log.d("animateable","yee");
                    (gif as Animatable).start();
                }
                gifdrawable!!.draw(it);
                (gifdrawable as Animatable).stop();
                surfholder!!.unlockCanvasAndPost(it);
            }
        }
        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder);
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);
            surfholder = surfaceHolder;

        }
    }
}