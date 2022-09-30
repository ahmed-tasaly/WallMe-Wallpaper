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
        val mygifengine = gifengine();
        return mygifengine
    }

    inner class gifengine : Engine(){
        val GifPath = this@gifwallpaper.getSharedPreferences("LiveWallpaper",0).getString("Gif_Path","")!!.toString();
        val gifdrawable: Drawable? = AnimatedImageDrawable.createFromPath(GifPath);
        val gifanimated = gifdrawable as? Animatable;
        var surfholder : SurfaceHolder? =null;
        val callbackHandler  = Handler(Looper.myLooper()!!);
        var scaleX = 0f;
        var scaleY = 0f;
        var isVisiable :Boolean = true;

        val drawloopfun = Runnable {
            Draw();
            Log.d("DrawCall","out: draw has been called");
        }

        fun Draw(){
            val canvas  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            { surfholder?.lockHardwareCanvas(); } else { surfholder?.lockCanvas(); }
            canvas?.let {
                it.scale(scaleY,scaleY);
                gifdrawable!!.draw(it);
                surfholder!!.unlockCanvasAndPost(it);
            }
            callbackHandler.removeCallbacks(drawloopfun);
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);
            surfholder = surfaceHolder;
        }
        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder);
            gifanimated?.start();
            callbackHandler.post(drawloopfun);
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height);
            scaleX =  width.toFloat() / gifdrawable!!.intrinsicWidth.toFloat();
            scaleY =  height.toFloat() /gifdrawable.intrinsicHeight.toFloat();
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            super.onOffsetsChanged(
                xOffset,
                yOffset,
                xOffsetStep,
                yOffsetStep,
                xPixelOffset,
                yPixelOffset
            )
            Log.d("DrawCall","new offset $xOffset")
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisiable = visible;
        }


        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder);
            callbackHandler.removeCallbacks(drawloopfun);
            gifanimated?.stop();
        }


        override fun onDestroy() {
            super.onDestroy();
            callbackHandler.removeCallbacks(drawloopfun);
            gifanimated?.stop();
        }
    }
}