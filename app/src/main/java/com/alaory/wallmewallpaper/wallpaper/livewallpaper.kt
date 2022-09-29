package com.alaory.wallmewallpaper.wallpaper


import android.app.WallpaperManager
import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class livewallpaper : WallpaperService() {

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
                val videoPath = this@livewallpaper.getSharedPreferences("LiveWallpaper",0).getString("Video_Path","")!!.toString();
                if(videoPath == "")
                    return;
                setDataSource(videoPath);
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
    //return my engine
    override fun onCreateEngine(): Engine {
        val myengine = VideoLiveWallpaperEngine();
        return myengine;
    }
}