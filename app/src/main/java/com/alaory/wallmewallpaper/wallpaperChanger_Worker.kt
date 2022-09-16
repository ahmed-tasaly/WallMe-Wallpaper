package com.alaory.wallmewallpaper

import android.app.Notification
import android.app.WallpaperManager
import android.app.job.JobService
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.ImageRequest
import coil.target.Target
import kotlin.random.Random

class wallpaperChanger_Worker(val context: Context, param : WorkerParameters) : CoroutineWorker(context,param) {

    var Database = database(context);

    fun stringtonum(string: String): Long{
        var count: Long = 0;
        for(char in string)
            count += char.code;
        return count;
    }


    suspend fun change_Wallpaper(wallpaperFlag : Int): Boolean{
        Database.update_image_info_list_from_database();
        if(database.imageinfo_list.isEmpty()) {//if empty dont change
            return true;
        }


        var rannum = (0..database.imageinfo_list.lastIndex).random();
        var imageInfo = database.imageinfo_list[rannum];

        val lastname = context.getSharedPreferences("wallpaper_changer_service",
            JobService.MODE_PRIVATE
        ).getString("wallpapername","0");
        if(lastname == imageInfo.Image_name){
            rannum = (0..database.imageinfo_list.lastIndex).random(Random(stringtonum(lastname)));
            imageInfo = database.imageinfo_list[rannum];

            if(context.getSharedPreferences("wallpaper_changer_service", JobService.MODE_PRIVATE).getString("wallpapername","0") == imageInfo.Image_name){
                return true;
            }

        }




        context.getSharedPreferences("wallpaper_changer_service", JobService.MODE_PRIVATE).edit().putString("wallpapername",imageInfo.Image_name).apply();

        var requestState = true;
        val imagerequest = ImageRequest.Builder(context)
            .data(imageInfo.Image_url)
            .target(
                object : Target {
                    override fun onError(error: Drawable?) {
                        super.onError(error);
                        requestState = false;
                    }
                    override fun onSuccess(result: Drawable) {
                        super.onSuccess(result);
                        try{
                            val screenWidth = context.resources.displayMetrics.widthPixels;
                            val screenHeight = context.resources.displayMetrics.heightPixels;

                            val wallpapermanager = WallpaperManager.getInstance(context);
                            wallpapermanager.suggestDesiredDimensions(screenWidth,screenHeight);




                            val Image = result.toBitmap();


                            //get ration between image and screen
                            val scalescreen = screenWidth.toFloat()/Image.width.toFloat();
                            val scaleimage =  screenHeight.toFloat()/Image.height.toFloat();//FIND RATIO BETWEEN the two rectangles


                            //get image scale
                            val imagescreenWidth = Image.width*scaleimage;
                            val imagescreenHeight = Image.height*scaleimage;


                            val rectImage = Rect(
                                0,
                                0,
                                imagescreenWidth.toInt(),
                                imagescreenHeight.toInt()
                            );

                            wallpapermanager.setBitmap(Image,rectImage,true,wallpaperFlag);
                        }catch (e : Exception){
                            requestState = false;
                            Log.e(WallpaperManager::class.java.simpleName,e.toString());
                        }
                    }
                }
            )
            .build()

        val imageloader = ImageLoader.Builder(context)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("imagesaved"))
                    .build()
            }.build();

        imageloader.execute(imagerequest);
        return requestState;
    }

    override suspend fun doWork(): Result {
        Database.update_image_info_list_from_database();
        if(Resources.getSystem().configuration.orientation != Configuration.ORIENTATION_PORTRAIT || database.imageinfo_list.isEmpty()){
            return Result.success();
        }

        val screenSelection = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("screenSelection",0);
        var state = true;
        when (screenSelection){
            0 ->{
                state = change_Wallpaper(WallpaperManager.FLAG_SYSTEM);
            }
            1 ->{
                state = change_Wallpaper(WallpaperManager.FLAG_LOCK);
            }
            2 ->{
                change_Wallpaper(WallpaperManager.FLAG_SYSTEM);
                state = change_Wallpaper(WallpaperManager.FLAG_LOCK);
            }
        }


        Log.i("workermee","changing wallpaper");


        when(state){
            true ->{
                return Result.success();
            }
            false ->{
                return Result.retry();
            }
        }

    }
}