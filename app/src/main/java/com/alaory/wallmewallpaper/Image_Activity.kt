package com.alaory.wallmewallpaper

import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import coil.ImageLoader
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ortiz.touchview.TouchImageView
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.absoluteValue


class Image_Activity(): AppCompatActivity(){
    //image info
    private var titlePost: TextView? = null;
    private var auther_post: TextView? = null;
    private var url_post: TextView? = null;
    private var Full_image: TouchImageView? = null;
    private var mybitmap: Bitmap? = null;
    private var taggroup: ChipGroup? = null;


    //buttons
    private var setWallPaperButton : FloatingActionButton? = null;
    private var saveWallpaperButton : FloatingActionButton? = null;
    private var setfavorite : FloatingActionButton? = null;

    //bottom buttons
    private var setwallpaper_bottom_button: Button? = null;
    private var goback_bottom_button: FloatingActionButton? = null;
    private var container_bottom_button : ConstraintLayout? = null;


     var myData : Image_Info? = null;
     var thumbnail: Drawable? = null;

    enum class mode{
        wallhaven,
        reddit
    }
    enum class setmode{
        HomeScreen,
        LockScreen,
        Both
    }




    private  var cricle_prograssBar : ProgressBar? = null;
    //to check if the image is loaded
    private var loaded = false;

    companion object{
        //the clicked data by the user
         var MYDATA : Image_Info? = null;
         var THUMBNAIL: Drawable? = null;
        //save bitmap to file and load it as a uri
        //mode
        var postmode = mode.reddit;
        //wallhaven tags
        var TagNameList : Array<String> = emptyArray();






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



        fun setWallpaper(context: Context,wallBitmap: Bitmap ,rectF: RectF,setLockScreen: setmode){
            //set the wallpaper
                try{
                    val screenWidth = context.resources.displayMetrics.widthPixels;
                    val screenHeight = context.resources.displayMetrics.heightPixels;

                    val wallpapermanager = WallpaperManager.getInstance(context);
                    wallpapermanager.suggestDesiredDimensions(screenWidth,screenHeight);



                    val WallPaperBitmap : Bitmap = Bitmap.createBitmap(
                        wallBitmap,
                        (wallBitmap.width * rectF.left).toInt(),
                        (wallBitmap.height * rectF.top).toInt(),
                        (wallBitmap.width * (rectF.right - rectF.left).absoluteValue).toInt(),
                        (wallBitmap.height * (rectF.bottom - rectF.top).absoluteValue).toInt()
                    );


                    when(setLockScreen){
                        setmode.HomeScreen -> {
                            wallpapermanager.setBitmap(WallPaperBitmap);
                        }
                        setmode.LockScreen -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpapermanager.setBitmap(WallPaperBitmap,null,true,WallpaperManager.FLAG_LOCK)
                            }else{
                                Toast.makeText(context,"Your device version of android doesn't support lock screen wallpaper",Toast.LENGTH_LONG).show();
                            }
                        }
                        else -> {}
                    }



                }catch (e:Exception){
                    Log.e("Image_Activity",e.toString())
                }

        }

    }












    private fun HideSystemBar(){
        if(Configuration.ORIENTATION_LANDSCAPE == Resources.getSystem().configuration.orientation){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ){
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }
    }





    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle);
        //activity system and app bar
        this.supportActionBar!!.hide();
        HideSystemBar();
        //update screen orein
        MainActivity.checkorein();

        myData = MYDATA!!
        thumbnail = THUMBNAIL!!
        //set the activit as a main screen
        setContentView(R.layout.show_image_fs);

        //-----------------------------------------------------------------
        //bottom buttons
        setwallpaper_bottom_button = findViewById(R.id.bottombutton_setwallpaper);
        goback_bottom_button = findViewById(R.id.bottombutton_goback);
        container_bottom_button = findViewById(R.id.bottombutton_container);
        container_bottom_button!!.animate().translationY(200f);//for animation
        //bottom sheet
        val bottomsheetfragment = findViewById<FrameLayout>(R.id.ImageInfo_BottomSheet);

        BottomSheetBehavior.from(bottomsheetfragment).apply {
            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_UNDEFINED)
                peekHeight = ((resources.displayMetrics.heightPixels / resources.displayMetrics.density)/2.5).toInt();
            else
                peekHeight = ((resources.displayMetrics.widthPixels / resources.displayMetrics.density)/2.5).toInt();


            this.state = BottomSheetBehavior.STATE_COLLAPSED;

            val Views = LayoutInflater.from(this@Image_Activity).inflate(R.layout.bottom_sheet,null);
            bottomsheetfragment.addView(Views);

            //set bottom sheet view info
            taggroup = Views.findViewById(R.id.TagGroup);
            taggroup!!.isVisible = false;
            titlePost = Views.findViewById(R.id.title_post);
            auther_post = Views.findViewById(R.id.auther_post);
            url_post = Views.findViewById(R.id.url_post);
            setWallPaperButton = Views.findViewById(R.id.set_bottomsheet_floatingbutton);
            saveWallpaperButton = Views.findViewById(R.id.save_imageButton);
            setfavorite = Views.findViewById(R.id.favorite_bottomsheet_floatingbutton);


            //pull buttonimage
            Views.findViewById<ImageButton>(R.id.pullbottom).setOnClickListener {
                if(this.state == BottomSheetBehavior.STATE_EXPANDED)
                    this.state = BottomSheetBehavior.STATE_COLLAPSED;
                else
                    this.state = BottomSheetBehavior.STATE_EXPANDED;

                it.animate().apply {
                    duration = 300;

                    rotationXBy(180f);
                }
            }

            setfavorite?.setOnClickListener {
                val tempdatabase = database(this@Image_Activity);
                tempdatabase.add_image_info_to_database(myData!!);
                val tempdatabase_parsed = tempdatabase.read_image_info_list_from_database();
                for(i in tempdatabase_parsed)
                    Log.i("tempdatabase_parsed","info: ${i.Image_name}  ${i.Image_url}  ${i.Image_title}  ${i.Image_thumbnail}");
            }

            //hide bottmsheet and show setwallpaper button
            setWallPaperButton?.setOnClickListener {

                this@apply.state = BottomSheetBehavior.STATE_COLLAPSED;
                bottomsheetfragment.animate().apply {
                    this?.duration = 100;
                    this?.translationY(200f);
                }
                container_bottom_button?.animate().apply {
                    this?.withStartAction {
                        container_bottom_button!!.visibility = View.VISIBLE;
                    }
                    this?.duration = 200;
                    this?.translationY(0f);
                }
            }
            //hide setwallpaper button and show bottomsheet
            goback_bottom_button?.setOnClickListener {
                bottomsheetfragment.animate().apply {
                    this?.duration = 200;
                    this?.translationY(0f);
                }
                container_bottom_button?.animate().apply {
                    this?.duration = 100;
                    this?.translationY(200f);
                    this?.withEndAction {
                        container_bottom_button?.visibility = View.INVISIBLE;
                    }
                }

            }

        }

        //----------------------------------------------------


        //set the ui elements
        Full_image = findViewById(R.id.full_image);
        cricle_prograssBar = findViewById(R.id.cricle_prograssBar_FullImage);

        //set text for image info
        titlePost!!.setText(myData?.Image_title);
        auther_post!!.setText("posted by: ${myData?.Image_auther}");
        url_post!!.setText(myData?.post_url)




        //-----------------------------------------------------------------

        //set wallhaven post info with tag functionality
        if(postmode == mode.wallhaven){
            wallhaven_api.imageInfo(myData!!) {
                runOnUiThread {
                    taggroup!!.isVisible = true;
                    auther_post!!.setText("posted by: ${myData?.Image_auther}");
                    for (i in TagNameList){
                        val TagChip = LayoutInflater.from(this).inflate(R.layout.tagchip,taggroup,false) as Chip;
                        TagChip.text = i;

                        TagChip.setOnClickListener {

                            try {
                                val tempTag = wallhaven_api.Tag("&q=+$i");
                                val intent = Intent(this, TagActivity::class.java);
                                intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                TagActivity.Tag_Assing = tempTag;
                                startActivity(intent);
                            }catch (e: Exception){
                                Log.e("Reddit_posts","error while trying to set image activity")
                            }
                        }
                        taggroup!!.addView(TagChip)
                    }
                }
            }
            titlePost!!.isVisible = false;
        }

        //------------------------------------------------------------------

        //load local bitmap and ui imageview data and do it in a callback
        ImageLoader(applicationContext).enqueue(coil.request.ImageRequest.Builder(this)
            .data(myData?.Image_url)
            .placeholder(thumbnail)
            .fallback(com.google.android.material.R.drawable.ic_mtrl_chip_close_circle)
            .target(object : coil.target.Target{
                override fun onError(error: Drawable?) {
                    super.onError(error)
                    mybitmap = error!!.toBitmap();
                    Full_image!!.setImageBitmap(mybitmap);
                }

                override fun onStart(placeholder: Drawable?) {
                    super.onStart(placeholder)
                    mybitmap = placeholder!!.toBitmap();
                    Full_image!!.setImageBitmap(mybitmap);
                }

                override fun onSuccess(result: Drawable) {
                    super.onSuccess(result)
                    loaded = true;
                    mybitmap = result.toBitmap();
                    Full_image!!.setImageBitmap(mybitmap);
                }
            })
            .listener(
                onSuccess = {_,_ ->
                    cricle_prograssBar?.visibility = View.INVISIBLE;
                },
                onCancel = {
                    cricle_prograssBar?.visibility = View.INVISIBLE;
                },
                onError = {_,_ ->
                    cricle_prograssBar?.visibility = View.INVISIBLE;
                },
                onStart = {
                    cricle_prograssBar?.visibility = View.VISIBLE;
                }

            )
            .build()
        );

        //--------------------------------------------------------------------------
        //set the wallpaper set button
        setwallpaper_bottom_button?.setOnClickListener {
            if (!loaded){
                Toast.makeText(this,"Please wait for the image to load",Toast.LENGTH_LONG).show();
                return@setOnClickListener;
            }

            val alert_setwallpaper = AlertDialog.Builder(this,R.style.TransparentDialog)

            val buttonlist = LayoutInflater.from(this).inflate(R.layout.setwallpaperalert,null) as ConstraintLayout;
            alert_setwallpaper.setView(buttonlist);

            val tempDialog = alert_setwallpaper.show();

            buttonlist.findViewById<Button>(R.id.SetHomeScreen).setOnClickListener {
                setWallpaper(
                    this,
                    mybitmap!!,
                    Full_image!!.zoomedRect,
                    setmode.HomeScreen
                );
                Toast.makeText(this,"Wallpaper set to Homescreen",Toast.LENGTH_LONG).show();
                tempDialog.dismiss();
            }

            buttonlist.findViewById<Button>(R.id.SetLockScreen).setOnClickListener {
                setWallpaper(
                    this,
                    mybitmap!!,
                    Full_image!!.zoomedRect,
                    setmode.LockScreen
                );
                Toast.makeText(this,"Wallpaper set to lockscreen",Toast.LENGTH_LONG).show();
                tempDialog.dismiss();
            }

            buttonlist.findViewById<Button>(R.id.SetBothScreen).setOnClickListener {
                val temprect = Full_image!!.zoomedRect;
                setWallpaper(
                    this,
                    mybitmap!!,
                    temprect,
                    setmode.LockScreen
                );
                setWallpaper(
                    this,
                    mybitmap!!,
                    temprect,
                    setmode.HomeScreen
                );

                Toast.makeText(this,"Wallpaper set to Both",Toast.LENGTH_LONG).show();
                tempDialog.dismiss();
            }




        };


        saveWallpaperButton?.setOnClickListener {
            if(loaded) {
                saveImage(applicationContext,mybitmap!!, myData!!.Image_name);
                Toast.makeText(this,"Image has been saved to your photos directory",Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(this,"Please wait for the image to load",Toast.LENGTH_LONG).show();
            }
        };

        Log.i("Image_Activity","Info url ${myData?.Image_url} name ${myData?.Image_name} thumbnail ${myData?.Image_thumbnail}");
        //------------------------------------------------------------------------------

    }


}