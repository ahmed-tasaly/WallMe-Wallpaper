package com.alaory.wallmewallpaper.wallpaper

import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.ui.geometry.Rect
import kotlin.math.max

class gifwallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine {

        return  gifengine();
    }



    private val mainthred by lazy(LazyThreadSafetyMode.NONE) {
        Handler(Looper.getMainLooper());
    }

    inner class gifengine : Engine(){
        val GifPath = this@gifwallpaper.getSharedPreferences("LiveWallpaper",0).getString("Video_Path","")!!.toString();
        var gifdrawable: Drawable? = AnimatedImageDrawable.createFromPath(GifPath);
        var gifanimated = gifdrawable as? Animatable;
        var surfholder : SurfaceHolder? =null;
        val callbackHandler  = Handler(Looper.myLooper()!!);
        var scaleX = 0f;
        var scaleY = 0f;
        var largestscale = 0f;

        //todo add rect to gif drawable


        val drawloopfun = Runnable {
            Draw();
        }

        fun Draw(){
            val canvas  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            { surfholder?.lockHardwareCanvas(); } else { surfholder?.lockCanvas(); }
            val rec = surfholder!!.surfaceFrame;
            val drawrec = Rect(0f,0f,gifdrawable!!.intrinsicWidth*largestscale,gifdrawable!!.intrinsicHeight*largestscale);
            val midpoint = (drawrec.right - rec.right) / 4f;
            canvas?.let {
                it.scale(largestscale,largestscale);
                it.translate(-midpoint,0f);
                gifdrawable!!.draw(it);
                surfholder!!.unlockCanvasAndPost(it);
            }
            callbackHandler.removeCallbacks(drawloopfun);
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);

        }
        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder);
            surfholder = surfaceHolder;
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
            scaleX =  width.toFloat() / gifdrawable?.intrinsicWidth!!.toFloat();
            scaleY =  height.toFloat() /gifdrawable?.intrinsicHeight!!.toFloat();
            largestscale = max(scaleX,scaleY);
        }



        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            callbackHandler.removeCallbacks(drawloopfun);
            if(visible){
                callbackHandler.post(drawloopfun);
            }
        }


        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            callbackHandler.removeCallbacks(drawloopfun);
            gifanimated?.stop();
            surfholder = null;
            super.onSurfaceDestroyed(holder);
        }


        override fun onDestroy() {
            callbackHandler.removeCallbacks(drawloopfun);
            gifanimated?.stop();
            gifanimated = null;
            gifdrawable = null;
            super.onDestroy();
        }
    }
}