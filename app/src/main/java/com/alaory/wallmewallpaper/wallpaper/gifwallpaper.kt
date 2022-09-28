package com.alaory.wallmewallpaper.wallpaper

import android.graphics.Movie
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class gifwallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine {
        val videoPath = this.getSharedPreferences("LiveWallpaper",0).getString("Video_Path","")!!.toString();
        val movie = Movie.decodeFile(videoPath);//great now we are using deprecated stuff >:(
        return gifengine(movie);
    }

    inner class gifengine(val movie: Movie) : Engine(){
        val frameduration: Long = 20 // in milliseconds
        var handler = Handler();//ouch
        var surfholder : SurfaceHolder? = null; //my good old friend surfaces :)
        var isVisiable : Boolean = false;

        val drawgif_runnable = Runnable(){
            drawWithMoiveAndCanvse_forsomereason();
        }

        fun drawWithMoiveAndCanvse_forsomereason(){
            val canvas = surfholder?.lockCanvas();
            canvas?.let {
                it.save();
                it.scale(2f,2f);
                movie.draw(canvas,-100f,0f);
                it.restore()
                surfholder!!.unlockCanvasAndPost(it);
                movie.setTime((System.currentTimeMillis() % movie.duration()).toInt());
                handler.removeCallbacks(drawgif_runnable);
                handler.postDelayed(drawgif_runnable,frameduration);
            }
        }


        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);
            surfholder = surfaceHolder;
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawgif_runnable);
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
            isVisiable = visible;
            if(visible){
                handler.post(drawgif_runnable);
            }else{
                handler.removeCallbacks(drawgif_runnable);
            }
        }
    }
}