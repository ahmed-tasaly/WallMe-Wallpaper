package com.alaory.wallmewallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.alaory.wallmewallpaper.Image_Activity
import com.alaory.wallmewallpaper.UrlType
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.absoluteValue

fun setWallpaper(context: Context, wallBitmap: Bitmap, rectF: RectF, setLockScreen: Image_Activity.setmode){
    //set the wallpaper

    try{
        val screenWidth = context.resources.displayMetrics.widthPixels;
        val screenHeight = context.resources.displayMetrics.heightPixels;

        val wallpapermanager = WallpaperManager.getInstance(context);
        wallpapermanager.suggestDesiredDimensions(screenWidth,screenHeight);

        if(!wallpapermanager.isWallpaperSupported){
            Toast.makeText(context,"wallpaper is not supported. idk how", Toast.LENGTH_LONG).show();
            return;
        }


        val WallPaperBitmap : Bitmap = Bitmap.createBitmap(
            wallBitmap,
            (wallBitmap.width * rectF.left).toInt(),
            (wallBitmap.height * rectF.top).toInt(),
            (wallBitmap.width * (rectF.right - rectF.left).absoluteValue).toInt(),
            (wallBitmap.height * (rectF.bottom - rectF.top).absoluteValue).toInt()
        );


        when(setLockScreen){
            Image_Activity.setmode.HomeScreen -> {
                wallpapermanager.setBitmap(WallPaperBitmap,null,true, WallpaperManager.FLAG_SYSTEM);
            }
            Image_Activity.setmode.LockScreen -> {
                wallpapermanager.setBitmap(WallPaperBitmap,null,true, WallpaperManager.FLAG_LOCK);
            }
            else -> {}
        }



    }catch (e:Exception){
        Log.e("Image_Activity",e.toString())
    }

}



fun Bitmap_toUri(context: Context, image: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.PNG,100,bytes);
    val imagepath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"tempImage")
    if (!imagepath.mkdirs()) {
        Log.e("Image_Activity", "Directory not created")
    }
    val imagesaved = File(imagepath.absolutePath + "/temp.png");
    imagesaved.writeBytes(bytes.toByteArray());


    return Uri.fromFile(imagepath);
}


fun saveImage(context: Context, path : String, type: UrlType, Name : String){

    when(type){
        UrlType.Image ->{
            val orginalFile = File(path);
            orginalFile.inputStream();
        }
        UrlType.Video ->{

        }
        UrlType.Gif ->{

        }
    }

    //val imageByteStream = ByteArrayOutputStream();
    //image.compress(Bitmap.CompressFormat.PNG,100,imageByteStream);
    //val path = MediaStore.Images.Media.insertImage(context.contentResolver,image,Name,null);
}
