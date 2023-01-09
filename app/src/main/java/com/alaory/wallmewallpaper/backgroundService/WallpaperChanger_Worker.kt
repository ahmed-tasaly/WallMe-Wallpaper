package com.alaory.wallmewallpaper.backgroundService


import android.app.WallpaperManager
import android.app.job.JobService
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log

import androidx.core.graphics.drawable.toBitmap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.ImageRequest
import coil.target.Target
import com.alaory.wallmewallpaper.UrlType
import com.alaory.wallmewallpaper.database
import kotlin.random.Random

class wallpaperChanger_Worker(val context: Context, param : WorkerParameters) : CoroutineWorker(context,param) {

    var Database = database(context);

    fun stringtonum(string: String): Long{
        var count: Long = 0;
        for(char in string)
            count += char.code;
        return count;
    }
    //set wallpaper with full screen
    fun setWallpaper(result: Bitmap,wallpaperFlag: Int) : Boolean{
        try {
            val screenWidth = context.resources.displayMetrics.widthPixels;
            val screenHeight = context.resources.displayMetrics.heightPixels;

            val wallpapermanager = WallpaperManager.getInstance(context);
            wallpapermanager.suggestDesiredDimensions(
                screenWidth,
                screenHeight
            );


            val Image = result


            //get ration between image and screen
            val scalescreen = screenWidth.toFloat() / Image.width.toFloat();
            val scaleimage =
                screenHeight.toFloat() / Image.height.toFloat();//FIND RATIO BETWEEN the two rectangles


            //get image scale
            val imagescreenWidth = Image.width * scaleimage;
            val imagescreenHeight = Image.height * scaleimage;


            val rectImage = Rect(
                0,
                0,
                imagescreenWidth.toInt(),
                imagescreenHeight.toInt()
            );

            wallpapermanager.setBitmap(Image, rectImage, true, wallpaperFlag);
            return true
        } catch (e: Exception) {
            return false
        }
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
        if(lastname == imageInfo.Image_name || imageInfo.type != UrlType.Image){
            rannum = (0..database.imageinfo_list.lastIndex).random(Random(stringtonum(lastname!!)));
            imageInfo = database.imageinfo_list[rannum];

            if(context.getSharedPreferences("wallpaper_changer_service", JobService.MODE_PRIVATE).getString("wallpapername","0") == imageInfo.Image_name){
                return true;
            }

        }




        context.getSharedPreferences("wallpaper_changer_service", JobService.MODE_PRIVATE).edit().putString("wallpapername",imageInfo.Image_name).apply();

        val uriWallpaper = Uri.parse(imageInfo.Image_url);

        var requestState = true;

        if(uriWallpaper.scheme == "content"){
            when(imageInfo.type){//local wallpaper
                UrlType.Video -> {
                    val mpegplayer = MediaMetadataRetriever()
                    mpegplayer.setDataSource(context.contentResolver.openFileDescriptor(uriWallpaper,"r")!!.fileDescriptor);
                    setWallpaper(mpegplayer.frameAtTime!!,wallpaperFlag);
                }
                else -> {
                    val fs = context.contentResolver.openInputStream(uriWallpaper);
                    val bitmap = BitmapFactory.decodeStream(fs);
                    setWallpaper(bitmap,wallpaperFlag);
                }
            }

        }else {//online wallpaper
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
                            requestState = setWallpaper(result.toBitmap(),wallpaperFlag);
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
        }
        return requestState;
    }

    override suspend fun doWork(): Result {
        Database.update_image_info_list_from_database();
        if(Resources.getSystem().configuration.orientation != Configuration.ORIENTATION_PORTRAIT || database.imageinfo_list.isEmpty()){
            return Result.success();
        }

        val screenSelection = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("screenSelection",0);
        var state = true;
        //wallpaper screen
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