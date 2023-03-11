package com.alaory.wallmewallpaper.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.alaory.wallmewallpaper.Image_Activity
import com.alaory.wallmewallpaper.Image_Info
import com.alaory.wallmewallpaper.UrlType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread
import kotlin.math.absoluteValue

enum class setmode{
    HomeScreen,
    LockScreen,
    Both
}

fun setWallpaper(context: Context, wallBitmap: Bitmap, rectF: RectF, setScreen: setmode){
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

        thread {
            val WallPaperBitmap : Bitmap = Bitmap.createBitmap(
                wallBitmap,
                (wallBitmap.width * rectF.left).toInt(),
                (wallBitmap.height * rectF.top).toInt(),
                (wallBitmap.width * (rectF.right - rectF.left).absoluteValue).toInt(),
                (wallBitmap.height * (rectF.bottom - rectF.top).absoluteValue).toInt()
            );


            when(setScreen){
                setmode.HomeScreen -> {
                    wallpapermanager.setBitmap(WallPaperBitmap,null,true, WallpaperManager.FLAG_SYSTEM);
                }
                setmode.LockScreen -> {
                    wallpapermanager.setBitmap(WallPaperBitmap,null,true, WallpaperManager.FLAG_LOCK);
                }
                else -> {}
            }
            ContextCompat.getMainExecutor(context).execute {
                Toast.makeText(context,"Wallpaper has been set",Toast.LENGTH_LONG).show();
            }



        }.start()

    }catch (e:Exception){
        Log.e("Image_Activity",e.toString())
    }

}

fun saveMedia(context: Context, path : String, type: UrlType, Name : Image_Info){
    val resolver = context.contentResolver;

    when(type){
        UrlType.Image ->{
            //get image path
            val image = File(path);

            //set image info
            val ImageValues = ContentValues();
            ImageValues.put(MediaStore.Images.Media.DISPLAY_NAME,Name.Image_name);
            ImageValues.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
            ImageValues.put(MediaStore.Images.Media.TITLE,Name.Image_name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ImageValues.put(MediaStore.Images.Media.AUTHOR,Name.Image_auther);
                ImageValues.put(MediaStore.Images.Media.OWNER_PACKAGE_NAME,Name.Image_auther);
            };
            ImageValues.put(MediaStore.Images.Media.DATE_ADDED,System.currentTimeMillis()/1000);

            //create media for the image
            val OutFileDes = resolver.insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,ImageValues);

            //open media into a file
            val outfile = OutFileDes?.let { resolver.openFileDescriptor(it,"w") }
            //Use File to write to the media
            val writeout = FileOutputStream(outfile?.fileDescriptor);
            writeout.write(image.inputStream().readBytes());
            writeout.close();
            //repeat for all
            Toast.makeText(context,"Image has been saved to your Picture directory",Toast.LENGTH_LONG).show();
        }

        UrlType.Video ->{
            val video = File(path);

            val videoValues = ContentValues();
            videoValues.put(MediaStore.Video.Media.DISPLAY_NAME,Name.Image_name);
            videoValues.put(MediaStore.Video.Media.TITLE,Name.Image_name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                videoValues.put(MediaStore.Video.Media.AUTHOR,Name.Image_auther);
                videoValues.put(MediaStore.Video.Media.OWNER_PACKAGE_NAME,Name.Image_auther);
            };
            videoValues.put(MediaStore.Video.Media.DATE_ADDED,System.currentTimeMillis()/1000);
            videoValues.put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");


            val OutVideoDes = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,videoValues);
            val outVideo = OutVideoDes?.let { resolver.openFileDescriptor(it,"w")};
            val filevideo = FileOutputStream(outVideo?.fileDescriptor);

            filevideo.write(video.inputStream().readBytes());
            filevideo.close();
            Toast.makeText(context,"Video has been saved to your Movie directory",Toast.LENGTH_LONG).show();
        }

        UrlType.Gif ->{
            val gif = File(path);

            val gifValues = ContentValues();
            gifValues.put(MediaStore.Images.Media.DISPLAY_NAME,Name.Image_name);
            gifValues.put(MediaStore.Images.Media.TITLE,Name.Image_name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                gifValues.put(MediaStore.Images.Media.AUTHOR,Name.Image_auther);
                gifValues.put(MediaStore.Images.Media.OWNER_PACKAGE_NAME,Name.Image_auther);
            };
            gifValues.put(MediaStore.Images.Media.DATE_ADDED,System.currentTimeMillis()/1000);
            gifValues.put(MediaStore.Images.Media.MIME_TYPE,"image/gif");


            val OutgifDes = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,gifValues);
            val outgif = OutgifDes?.let { resolver.openFileDescriptor(it,"w")};
            val filegif = FileOutputStream(outgif?.fileDescriptor);

            filegif.write(gif.inputStream().readBytes());
            filegif.close();
            Toast.makeText(context,"Gif has been saved to your Picture directory",Toast.LENGTH_LONG).show();
        }
    }
}

fun loadMedia(context: Activity){
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT);
    val mime_type = arrayOf(
        "image/png",
        "image/jpg",
        "image/gif",
        "video/mp4",
        "video/mpeg",
        "image/jpeg",
        "video/webm"
    )
    intent.setType("*/*");

    intent.putExtra(Intent.EXTRA_MIME_TYPES,mime_type);
    val i = Intent.createChooser(intent,"Select a wallpaper");
    context.startActivityForResult(i,8777);
}
