package com.alaory.wallmewallpaper.wallpaper



import android.graphics.Movie
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder

import kotlin.math.max

class livewallpaper : WallpaperService() {


    //return my engine
    override fun onCreateEngine(): Engine {
        val type = this@livewallpaper.getSharedPreferences("LiveWallpaper",0).getString("Media_Type","video");
        var engine  : WallpaperService.Engine? = null;
        when(type){
            "gif" ->{
                engine = gifengine();
            }
            else ->{
                engine = VideoLiveWallpaperEngine();
            }
        }
        return engine;
    }





    // Video Engine
    inner class VideoLiveWallpaperEngine : WallpaperService.Engine(){
        var player : MediaPlayer? = null;

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder);

            player = MediaPlayer().apply {
                isLooping = true;
                setSurface(holder!!.surface);
                val prefs = this@livewallpaper.getSharedPreferences("LiveWallpaper",0);
                val videoPath = prefs.getString("Video_Path","")!!.toString();

                if(videoPath == "")
                    return;

                val left = prefs.getFloat("left",0f);
                val top = prefs.getFloat("top",0f);

                var uricontent = Uri.parse(videoPath);
                when(uricontent.scheme){
                    "content" -> {
                        val fd = this@livewallpaper.contentResolver.openFileDescriptor(uricontent,"r")
                        setDataSource(fd!!.fileDescriptor);
                    }
                    else -> {
                        setDataSource(videoPath);
                    }
                }

                setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                prepare();
                start();
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
            if(visible)
                player!!.start();
            else
                player!!.pause();

        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder);
            if(player!!.isPlaying) player!!.stop();
            player?.release();
            player = null;
        }

        override fun onDestroy() {
            super.onDestroy();

            if(player?.isPlaying == true) player!!.stop();
            player?.release();
            player = null;
        }

    }









    //gif engine












    inner class gifengine : Engine(){
        val prefs = this@livewallpaper.getSharedPreferences("LiveWallpaper",0);
        val GifPath = prefs.getString("Video_Path","")!!.toString();
        val leftoffset = prefs.getFloat("left",0f);
        val topoffset = prefs.getFloat("top",0f);
        //var gifdrawable: Drawable? = AnimatedImageDrawable.createFromPath(GifPath);
        //var gifanimated = gifdrawable as? Animatable;
        var surfholder : SurfaceHolder? =null;
        val callbackHandler  = Handler(this@livewallpaper.mainLooper);
        var scaleX = 0f;
        var scaleY = 0f;
        var largestscale = 0f;
        val moive = Movie.decodeFile(GifPath);//i know
        var isVisiable = true;

        val drawloopfun = Runnable {
            Draw();
        }

        fun Draw(){
            val canvas  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            { surfholder?.lockHardwareCanvas(); } else { surfholder?.lockCanvas(); }

            if(largestscale == 0f)
                largestscale = 1f;

            val surfgifwidth = moive.width()*largestscale;
            val surfgifheight = moive.height()*largestscale;


            //set offset
            val xtran = surfgifwidth*leftoffset/largestscale;
            val ytran = surfgifheight*topoffset/largestscale;


            canvas?.let {
                it.scale(largestscale,largestscale);
                it.translate(-xtran,-ytran);
                moive.draw(it,0f,0f);
                surfholder!!.unlockCanvasAndPost(it);
            }

            moive.setTime((System.currentTimeMillis()%moive.duration()).toInt());
            callbackHandler.removeCallbacks(drawloopfun);
            if(isVisiable)
                callbackHandler.postDelayed(drawloopfun,40);
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);

        }
        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder);
            surfholder = surfaceHolder;
            callbackHandler.post(drawloopfun);//jump start Animation
        }


        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height);
            scaleX =  width.toFloat() / moive.width().toFloat();
            scaleY =  height.toFloat() /moive.height().toFloat();
            largestscale = max(scaleX,scaleY);
        }



        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisiable = visible;
            if(visible){
                callbackHandler.post(drawloopfun);
            }else{
                callbackHandler.removeCallbacks(drawloopfun)
            }

        }


        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            callbackHandler.removeCallbacks(drawloopfun);
            surfholder = null;
            super.onSurfaceDestroyed(holder);
        }


        override fun onDestroy() {
            super.onDestroy();
            Log.d("DestoryLog",this::class.java.simpleName);
        }
    }


}