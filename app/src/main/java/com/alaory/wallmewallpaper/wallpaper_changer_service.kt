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
import kotlin.math.floor
import kotlin.math.min
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
                    override fun onSuccess(result: Drawable) {
                        super.onSuccess(result);
                        try{
                            val wallpapermanager = WallpaperManager.getInstance(this@wallpaper_changer_service);
                            val screenWidth = resources.displayMetrics.widthPixels;
                            val screenHeight = resources.displayMetrics.heightPixels;
                            val Image = result.toBitmap();
                            wallpapermanager.suggestDesiredDimensions(screenWidth,screenHeight);
                            val scaleRatiowidth = screenWidth.toFloat()/Image.width.toFloat();
                            val scaleRatioheight =  screenHeight.toFloat()/Image.height.toFloat();//FIND RATIO BETWEEN the two rectangles

                            val imagescreenWidth = Image.width*scaleRatiowidth;
                            val imagescreenHeight = Image.height*scaleRatioheight;

                            var leftStart = (Image.width/2)-(imagescreenWidth/2);
                            var topStart = (Image.height/2)-(imagescreenHeight/2);

                            if(leftStart < 0)
                                leftStart = 0f;
                            if(topStart < 0)
                                topStart = 0f;


                            val rectImage = Rect(
                                leftStart.toInt(),
                                topStart.toInt(),
                                imagescreenWidth.toInt(),
                                imagescreenHeight.toInt()
                            );
                            Log.i(wallpaper_changer_service::class.java.simpleName,"top ${rectImage.top} bottom ${rectImage.bottom} left ${rectImage.left} right ${rectImage.right}");
                            wallpapermanager.setBitmap(Image,rectImage,true,WallpaperManager.FLAG_SYSTEM);
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
//        Toast.makeText(this,"wallpaper background service $resused",Toast.LENGTH_SHORT).show();
//        Log.i("Jobmeout","called $resused");
        change_Wallpaper();
        return true;
    }

    override fun onStopJob(param: JobParameters?): Boolean {
        jobFinished(param,true);
        return true;
    }

}