package com.alaory.wallmewallpaper

import android.app.WallpaperManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
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




    fun change_Wallpaper(wallpaperFlag : Int){
        Database.update_image_info_list_from_database();
        if(database.imageinfo_list.isEmpty())//if empty dont change
            return;


        val imageInfo = database.imageinfo_list[(0..database.imageinfo_list.lastIndex).random()];

        if(this.getSharedPreferences("wallpaper_changer_service", MODE_PRIVATE).getString("wallpapername","0") == imageInfo.Image_name)
            return;
        this.getSharedPreferences("wallpaper_changer_service", MODE_PRIVATE).edit().putString("wallpapername",imageInfo.Image_name).apply();

        val imagerequest = ImageRequest.Builder(this)
            .data(imageInfo.Image_url)
            .target(
                object : Target{
                    override fun onSuccess(result: Drawable) {
                        super.onSuccess(result);
                        try{
                            val wallpapermanager = WallpaperManager.getInstance(this@wallpaper_changer_service);

                            var screenWidth = resources.displayMetrics.widthPixels;
                            var screenHeight = resources.displayMetrics.heightPixels;

                            if(Resources.getSystem().configuration.orientation != Configuration.ORIENTATION_PORTRAIT){
                                screenWidth = resources.displayMetrics.heightPixels;
                                screenHeight =  resources.displayMetrics.widthPixels;//i know its bad i'll fix it later ;(
                            }


                            val Image = result.toBitmap();
                            wallpapermanager.suggestDesiredDimensions(screenWidth,screenHeight);

                            val scalescreen = screenWidth.toFloat()/Image.width.toFloat();
                            val scaleimage =  screenHeight.toFloat()/Image.height.toFloat();//FIND RATIO BETWEEN the two rectangles


                            val imagescreenWidth = Image.width*scaleimage;
                            val imagescreenHeight = Image.height*scaleimage;

//                            var leftStart = (Image.width/2)-(Image.width*scalescreen/2);
//                            var topStart = (Image.height/2)-(Image.height*scalescreen/2);
//
//
//                            if(leftStart < 0)
//                                leftStart = 0f;
//                            if(topStart < 0)
//                                topStart = 0f;


                            val rectImage = Rect(
                                0,
                                0,
                                imagescreenWidth.toInt(),
                                imagescreenHeight.toInt()
                            );


                            //Log.i(wallpaper_changer_service::class.java.simpleName,"top ${rectImage.top} bottom ${rectImage.bottom} left ${rectImage.left} right ${rectImage.right}");
                            wallpapermanager.setBitmap(Image,rectImage,true,wallpaperFlag);
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
        //Toast.makeText(this,"wallpaper background service $resused",Toast.LENGTH_SHORT).show();
        //Log.i("Jobmeout","called $resused");



        val screenSelection = this.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("screenSelection",0);
        when (screenSelection){
            0 ->{
                change_Wallpaper(WallpaperManager.FLAG_SYSTEM);
            }
            1 ->{
                change_Wallpaper(WallpaperManager.FLAG_LOCK);
            }
            2 ->{
                change_Wallpaper(WallpaperManager.FLAG_SYSTEM);
                change_Wallpaper(WallpaperManager.FLAG_LOCK);
            }
        }

        return true;
    }

    override fun onStopJob(param: JobParameters?): Boolean {
        jobFinished(param,true);
        return true;
    }

}