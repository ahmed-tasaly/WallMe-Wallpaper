package com.alaory.wallmewallpaper

import android.app.WallpaperManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.ImageRequest
import coil.target.Target
import okhttp3.Request
import kotlin.random.Random

class wallpaper_changer_service : JobService() {

    companion object{
        var resused = 1;
    }
    var Database = database(this);




    fun change_Wallpaper(){
        Database.update_image_info_list_from_database();
        if(database.imageinfo_list.isEmpty())//if empty dont change
            return;


        val imageInfo = database.imageinfo_list[(0..database.imageinfo_list.lastIndex).random()];



        var imagerequest = ImageRequest.Builder(this)
            .data(imageInfo.Image_url)
            .target(
                object : Target{
                    override fun onError(error: Drawable?) {
                        super.onError(error)
                    }

                    override fun onStart(placeholder: Drawable?) {
                        super.onStart(placeholder);
                    }

                    override fun onSuccess(result: Drawable) {
                        super.onSuccess(result);
                        try{
                            val wallpapermanager = WallpaperManager.getInstance(this@wallpaper_changer_service);
                            val screenWidth = resources.displayMetrics.widthPixels;
                            val screenHeight = resources.displayMetrics.heightPixels;
                            wallpapermanager.suggestDesiredDimensions(screenWidth,screenHeight);
                            wallpapermanager.setBitmap(result.toBitmap());
                        }catch (e : Exception){
                            Log.e(WallpaperManager::class.java.simpleName,e.toString());
                        }
                    }
                }
            )
            .build()

        val imageloader = ImageLoader.Builder(this@wallpaper_changer_service)
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("imagesaved"))
                    .build()
            }.build();

       imageloader.enqueue(imagerequest);
    }

    override fun onStartJob(param: JobParameters?): Boolean {
        Toast.makeText(this,"wallpaper background service $resused",Toast.LENGTH_SHORT).show();
        Log.i("Jobmeout","called $resused");
        change_Wallpaper();
        return true;
    }

    override fun onStopJob(param: JobParameters?): Boolean {
        jobFinished(param,true);
        return true;
    }

}