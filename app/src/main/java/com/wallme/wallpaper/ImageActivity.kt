package com.wallme.wallpaper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class Image_Activity(): AppCompatActivity(){
    //ui elements
    private lateinit var titlePost: TextView;
    private lateinit var auther_post: TextView;
    private lateinit var url_post: TextView;
    private lateinit var Full_image: ImageView;
    private lateinit var mybitmap: Bitmap;

    private lateinit var cricle_prograssBar : ProgressBar;
    //to check if the image is loaded
    private var loaded = false;

    companion object{
        //the clicked data by the user
        lateinit var myData : List_image;
        lateinit var thumbnail: Drawable;
        //save bitmap to file and load it as a uri


        fun saveImage(context: Context, image: Bitmap,Name : String): String {
            val imageByteStream = ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG,100,imageByteStream);

            val path = MediaStore.Images.Media.insertImage(context.contentResolver,image,Name,null);
            return path;
        }

        fun Bitmap_toUri(context: Context, image: Bitmap): Uri? {
            var bytes = ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG,100,bytes);
            var imagepath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"tempImage")
            if (!imagepath.mkdirs()) {
                Log.e("Image_Activity", "Directory not created")
            }
            var imagesaved = File(imagepath.absolutePath + "/temp.png");
            imagesaved.writeBytes(bytes.toByteArray());

            return Uri.parse(imagesaved.path);
        }

    }






    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle);
        this.supportActionBar!!.hide();
        //set the activit as a main screen
        setContentView(R.layout.show_image_fs);
        //set the ui elements
        titlePost = findViewById<TextView>(R.id.title_post);
        auther_post = findViewById<TextView>(R.id.auther_post);
        url_post = findViewById<TextView>(R.id.url_post);
        Full_image = findViewById<ImageView>(R.id.full_image);
        cricle_prograssBar = findViewById<ProgressBar>(R.id.cricle_prograssBar_FullImage);


        titlePost.setText(myData.Image_title);
        auther_post.setText("posted by: ${myData.Image_auther}");
        url_post.setText(myData.post_url)


        //load local bitmap and ui imageview data and do it in a callback
        ImageLoader(applicationContext).enqueue(coil.request.ImageRequest.Builder(this)
            .data(myData.Image_url)
            .placeholder(thumbnail)
            .target(object : coil.target.Target{
                override fun onError(error: Drawable?) {
                    super.onError(error)
                    mybitmap = error!!.toBitmap();
                    Full_image.setImageBitmap(mybitmap);
                }

                override fun onStart(placeholder: Drawable?) {
                    super.onStart(placeholder)
                    mybitmap = placeholder!!.toBitmap();
                    Full_image.setImageBitmap(mybitmap);
                }

                override fun onSuccess(result: Drawable) {
                    super.onSuccess(result)
                    loaded = true;
                    mybitmap = result.toBitmap();
                    Full_image.setImageBitmap(mybitmap);
                }
            })
            .listener(
                onSuccess = {_,_ ->
                    cricle_prograssBar.visibility = View.INVISIBLE;
                },
                onCancel = {
                    cricle_prograssBar.visibility = View.INVISIBLE;
                },
                onError = {_,_ ->
                    cricle_prograssBar.visibility = View.INVISIBLE;
                },
                onStart = {
                    cricle_prograssBar.visibility = View.VISIBLE;
                }

            )
            .build()
        );


        //set the wallpaper set button
        findViewById<ImageButton>(R.id.set_imageButton).setOnClickListener { setWallpaper(); };
        findViewById<ImageButton>(R.id.save_imageButton).setOnClickListener { if(loaded) saveImage(applicationContext,mybitmap, myData.Image_name); };
        Log.i("Image_Activity","Info url ${myData.Image_url} name ${myData.Image_name} thumbnail ${myData.Image_thumbnail}");


    }


    private fun setWallpaper(){
        //set the wallpaper
        if(loaded){
            try {
                var uri_image = Bitmap_toUri(applicationContext,mybitmap);
                val intent = Intent(Intent.ACTION_ATTACH_DATA);
                intent.addCategory((Intent.CATEGORY_DEFAULT));
                intent.setDataAndType(uri_image,"image/*");
                intent.putExtra("mimeType","image/*")
                this.startActivity(Intent.createChooser(intent,"Set as:"));
            }catch (e:Exception){
                Log.e("Image_Activity",e.toString())
            }


        }
    }

}