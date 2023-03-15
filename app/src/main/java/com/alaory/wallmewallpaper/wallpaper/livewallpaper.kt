package com.alaory.wallmewallpaper.wallpaper



import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

import kotlin.math.max

class livewallpaper : WallpaperService() {


    //return my engine
    override fun onCreateEngine(): Engine {
        val type = this@livewallpaper.getSharedPreferences("LiveWallpaper",0).getString("Media_Type","video");
        var engine  : WallpaperService.Engine? = null;
        when(type){
            "gif" ->{
                engine = GifEngine();
            }
            "image" -> {
                engine = ImageEngine();
            }
            else ->{//video
                engine = VideoLiveWallpaperEngine();
            }
        }
        return engine;
    }





    // Video Engine
    inner class VideoLiveWallpaperEngine : WallpaperService.Engine(){
        var exoPlayer  = ExoPlayer.Builder(this@livewallpaper)
            .setVideoScalingMode(2)//VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
            .build()

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder);
            exoPlayer.apply {
                val prefs = this@livewallpaper.getSharedPreferences("LiveWallpaper", 0);
                val videoPath = prefs.getString("Video_Path", "")!!.toString();
//                val leftoffset = prefs.getFloat("left",0f);
//                val topoffset = prefs.getFloat("top",0f);
//                val rightoffset = prefs.getFloat("right",0f);
//                val bottomoffset = prefs.getFloat("bottom",0f);

                repeatMode = Player.REPEAT_MODE_ONE


                val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
                exoPlayer.setMediaItem(mediaItem);


                volume = 0f;
                setVideoSurfaceHolder(surfaceHolder)
            }
        }

        override fun onSurfaceCreated(surfaceHolder: SurfaceHolder?) {
            super.onSurfaceCreated(surfaceHolder);
            exoPlayer.apply {

                prepare();
                play();
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible);
            if(visible)
                exoPlayer.play();
            else
                exoPlayer.pause();

        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder);
            if(exoPlayer.isPlaying) exoPlayer.stop();
        }

        override fun onDestroy() {
            super.onDestroy();
            if(exoPlayer.isPlaying) exoPlayer.stop();
            exoPlayer.release();
        }

    }









    //gif engine
    //   ||
    //   ||
    //   ||
    //  \||/
    //   \/









    inner class GifEngine : Engine(){
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
            super.onCreate(surfaceHolder);
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
            scaleX =  width.toFloat() / moive!!.width().toFloat();
            scaleY =  height.toFloat() /moive!!.height().toFloat();
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