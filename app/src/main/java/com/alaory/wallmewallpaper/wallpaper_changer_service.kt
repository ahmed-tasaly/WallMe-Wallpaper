package com.alaory.wallmewallpaper


import android.app.NotificationManager
import android.app.WallpaperManager
import android.app.job.JobParameters
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
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.ImageRequest
import coil.target.Target
import kotlin.random.Random

class wallpaper_changer_service : JobService() {

    companion object{
        var resused = 1;
    }
    var Database = database(this);

    fun stringtonum(string: String): Long{
        var count: Long = 0;
        for(char in string)
            count += char.code;
        return count;
    }


    fun change_Wallpaper(wallpaperFlag : Int): Boolean{
        Database.update_image_info_list_from_database();
        if(database.imageinfo_list.isEmpty()) {//if empty dont change
            return false;
        }


        var rannum = (0..database.imageinfo_list.lastIndex).random();
        var imageInfo = database.imageinfo_list[rannum];

        val lastname = this.getSharedPreferences("wallpaper_changer_service", MODE_PRIVATE).getString("wallpapername","0");
        if(lastname == imageInfo.Image_name){
            rannum = (0..database.imageinfo_list.lastIndex).random(Random(stringtonum(lastname)));
            imageInfo = database.imageinfo_list[rannum];

            if(this.getSharedPreferences("wallpaper_changer_service", MODE_PRIVATE).getString("wallpapername","0") == imageInfo.Image_name)
                return true;
        }




        this.getSharedPreferences("wallpaper_changer_service", MODE_PRIVATE).edit().putString("wallpapername",imageInfo.Image_name).apply();

        val imagerequest = ImageRequest.Builder(this)
            .data(imageInfo.Image_url)
            .target(
                object : Target{
                    override fun onSuccess(result: Drawable) {
                        super.onSuccess(result);
                        try{
                            val screenWidth = resources.displayMetrics.widthPixels;
                            val screenHeight = resources.displayMetrics.heightPixels;

                            val wallpapermanager = WallpaperManager.getInstance(this@wallpaper_changer_service);
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
        return true;
    }



    override fun onStartJob(param: JobParameters?): Boolean {
        if(Resources.getSystem().configuration.orientation != Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this,"wallpaper background failed",Toast.LENGTH_SHORT).show();
            return true;
        }



        Toast.makeText(this,"wallpaper background service $resused",Toast.LENGTH_SHORT).show();
        //Log.i("Jobmeout","called $resused");


        var state = true;
        val screenSelection = this.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("screenSelection",0);
        when (screenSelection){
            0 ->{
                state = change_Wallpaper(WallpaperManager.FLAG_SYSTEM);
            }
            1 ->{
                state =  change_Wallpaper(WallpaperManager.FLAG_LOCK);
            }
            2 ->{
                change_Wallpaper(WallpaperManager.FLAG_SYSTEM);
                state =  change_Wallpaper(WallpaperManager.FLAG_LOCK);
            }
        }




        return true;
    }

    override fun onStopJob(param: JobParameters?): Boolean {
        jobFinished(param,true);
        return true;
    }

}