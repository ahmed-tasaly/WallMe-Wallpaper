package com.alaory.wallmewallpaper.wallpaper



import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Process.myPid
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

import kotlin.math.max

class livewallpaper : WallpaperService() {

    companion object{
        var  killwallpaper : () -> Unit = {};
    }
    var type = "";

    //return my engine
    override fun onCreateEngine(): Engine {
        return ManagmentEngine();
    }

    interface SubEngine {
        fun onCreate(surfaceHolder: SurfaceHolder?);
        fun onSurfaceCreated(holder: SurfaceHolder?);
        fun onVisibilityChanged(visible: Boolean);
        fun onSurfaceDestroyed(holder: SurfaceHolder?);
        fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        )
        fun setScale(width: Int,height: Int);
        fun onDestroy();
    }

    inner class ManagmentEngine : WallpaperService.Engine(){
        var engine : SubEngine? = null;
        var surfcangeWidth = 0;
        var surfcangeHeight = 0;
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);
            killwallpaper = {
                engine?.onDestroy();
                super.onCreate(surfaceHolder);
                onCreate(surfaceHolder);
                engine?.onSurfaceCreated(surfaceHolder);
                engine?.setScale(surfcangeWidth,surfcangeHeight);
            }
            val type = this@livewallpaper.getSharedPreferences("LiveWallpaper",0).getString("Media_Type","video");
            when(type){
                "video" -> {engine = VideoLiveWallpaperEngine()}
                "gif" -> {engine = GifEngine()}
                else -> {}
            }
            engine?.onCreate(surfaceHolder);
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            engine?.onSurfaceCreated(holder);
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height);
            surfcangeWidth = width;
            surfcangeHeight = height
            engine?.onSurfaceChanged(holder,format,width,height);
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            engine?.onVisibilityChanged(visible);
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            engine?.onSurfaceDestroyed(holder);
        }

        override fun onDestroy() {
            super.onDestroy()
            engine?.onDestroy();
        }
    }

    // Video Engine
    inner class VideoLiveWallpaperEngine : SubEngine{
        var exoPlayer  : ExoPlayer? = null;

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            if(exoPlayer == null){
                exoPlayer = ExoPlayer.Builder(this@livewallpaper)
                    .setVideoScalingMode(2)//VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
                    .build()
            }


            exoPlayer!!.apply {
                val prefs = this@livewallpaper.getSharedPreferences("LiveWallpaper", 0);
                val videoPath = prefs.getString("Video_Path", "")!!.toString();
                repeatMode = Player.REPEAT_MODE_ONE


                val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
                exoPlayer!!.setMediaItem(mediaItem);


                volume = 0f;
                setVideoSurfaceHolder(surfaceHolder);
                prepare();
                play();
            }
        }

        override fun onSurfaceCreated(surfaceHolder: SurfaceHolder?) {
            exoPlayer!!.apply {
                prepare();
                play();
            }
        }

        override fun setScale(width: Int, height: Int) {
            //sweet f.a
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {

        }

        override fun onVisibilityChanged(visible: Boolean) {
            if(visible)
                exoPlayer!!.play();
            else
                exoPlayer!!.pause();

        }

         override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            if(exoPlayer!!.isPlaying) exoPlayer!!.stop();
        }

         override fun onDestroy() {
            if(exoPlayer!!.isPlaying) exoPlayer!!.stop();
            exoPlayer!!.release();
             exoPlayer = null;
        }

    }









    //gif engine
    //   ||
    //   ||
    //   ||
    //  \||/
    //   \/









    inner class GifEngine : SubEngine{
        val prefs = this@livewallpaper.getSharedPreferences("LiveWallpaper",0);
        val GifPath = prefs.getString("Video_Path","")!!.toString();
        val leftoffset = prefs.getFloat("left",0f);
        val topoffset = prefs.getFloat("top",0f);
        var surfholder : SurfaceHolder? =null;
        val callbackHandler  = Handler(this@livewallpaper.mainLooper);
        var scaleX = 0f;
        var scaleY = 0f;
        var largestscale = 0f;

        var moive : Movie? = null;
        var isVisiable = true;

        val drawloopfun = Runnable {
            Draw();
        }

        fun Draw(){
            val canvas  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            { surfholder?.lockHardwareCanvas(); } else { surfholder?.lockCanvas(); }

            if(largestscale == 0f)
                largestscale = 1f;

            val surfgifwidth = moive!!.width()*largestscale;
            val surfgifheight = moive!!.height()*largestscale;


            //set offset
            val xtran = surfgifwidth*leftoffset/largestscale;
            val ytran = surfgifheight*topoffset/largestscale;


            canvas?.let {
                it.scale(largestscale,largestscale);
                it.translate(-xtran,-ytran);
                moive?.draw(it,0f,0f);
                surfholder!!.unlockCanvasAndPost(it);
            }

            moive!!.setTime((System.currentTimeMillis()%moive!!.duration()).toInt());
            callbackHandler.removeCallbacks(drawloopfun);
            if(isVisiable)
                callbackHandler.postDelayed(drawloopfun,30);//
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            var sourceuri = Uri.parse(GifPath);
            when(sourceuri.scheme){
                "content" -> {
                    val  contentstream = this@livewallpaper.contentResolver.openInputStream(sourceuri);
                    moive = Movie.decodeStream(contentstream);
                }
                else -> {
                    moive = Movie.decodeFile(GifPath);//i know
                }
            }

        }
        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            surfholder = holder;
            callbackHandler.post(drawloopfun);//jump start Animation
        }

        override  fun setScale(width: Int,height: Int){
            scaleX =  width.toFloat() / moive!!.width().toFloat();
            scaleY =  height.toFloat() /moive!!.height().toFloat();
            largestscale = max(scaleX,scaleY);
        }


        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            scaleX =  width.toFloat() / moive!!.width().toFloat();
            scaleY =  height.toFloat() /moive!!.height().toFloat();
            largestscale = max(scaleX,scaleY);
        }


        override fun onVisibilityChanged(visible: Boolean) {
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

        }

        override fun onDestroy() {
            callbackHandler.removeCallbacks(drawloopfun)
            moive = null;
            surfholder?.surface?.release();
            surfholder = null;
            Log.d("DestoryLog",this::class.java.simpleName);
        }
    }
















    //image engine
    //   ||
    //   ||
    //   ||
    //  \||/
    //   \/

    inner class ImageEngine : Engine(){
        val prefs = this@livewallpaper.getSharedPreferences("LiveWallpaper",0);
        val imagepath = prefs.getString("Video_Path","")!!.toString();
        val imageUri = Uri.parse(imagepath);
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            val canvas  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {  holder?.lockHardwareCanvas(); } else { holder?.lockCanvas(); }
            when (imageUri.scheme){
                "content" -> {
                    val  contentstream = this@livewallpaper.contentResolver.openInputStream(imageUri);
                    val image = BitmapFactory.decodeStream(contentstream);
                    canvas?.setBitmap(image);

                }
                else -> {
                    val image = BitmapFactory.decodeFile(imagepath);
                    canvas?.setBitmap(image);
                }
            }
            holder!!.unlockCanvasAndPost(canvas);
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
        }
    }


}