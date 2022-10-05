package com.alaory.wallmewallpaper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.values
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.alaory.wallmewallpaper.api.wallhaven_api
import com.alaory.wallmewallpaper.interpreter.progressRespondBody
import com.alaory.wallmewallpaper.postPage.TagActivity
import com.alaory.wallmewallpaper.wallpaper.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ortiz.touchview.OnTouchImageViewListener
import com.ortiz.touchview.TouchImageView
import com.otaliastudios.zoom.ZoomSurfaceView
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.toHexString
import okio.Path.Companion.toPath


class Image_Activity(): AppCompatActivity(){

    //image info
    private var titlePost: TextView? = null;
    private var auther_post: TextView? = null;
    private var url_post: TextView? = null;
    private var counter_image: TextView? = null;
    private var mybitmap: Bitmap? = null;
    private var taggroup: ChipGroup? = null;
    private var bottomsheetarrow: ImageButton? =null;

    //image view
    var Full_image: TouchImageView? = null;
    var Full_video: ZoomSurfaceView? = null;


    //bottom sheet
    private var sheet_body : ConstraintLayout? = null;


    //buttons
    private var setWallPaperButton : FloatingActionButton? = null;
    private var saveWallpaperButton : FloatingActionButton? = null;
    private var setfavorite : FloatingActionButton? = null;
    private var blockimage : FloatingActionButton? = null;

    //bottom buttons
    private var setwallpaper_bottom_button: Button? = null;
    private var goback_bottom_button: FloatingActionButton? = null;
    private var container_bottom_button : ConstraintLayout? = null;


     //database
     val tempdatabase = database(this);
     val tempblockdatavase = database(this,database.ImageBlock_Table,"${database.ImageBlock_Table}.dp");
     var blocked = false;


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


    var MediaPath : String? = null;


    private  var cricle_prograssBar : ImageView? = null;
    //to check if the image is loaded
    private var loaded = false;


    //image loader
    var imageloader: ImageLoader? =null;

    companion object{
        val TAG = "Image_Activity";
        //the clicked data by the user
         var MYDATA : Image_Info? = null;
         var THUMBNAIL: Drawable? = null;
         var loadedPreview : Boolean = false;
        //save bitmap to file and load it as a uri



        //mode
        var postmode = mode.reddit;
        //wallhaven tags
        var TagNameList : Array<String> = emptyArray();


    }


    private fun isOnDatabase(): Boolean{
        var found = false;
        for(i in database.imageinfo_list){
            if(i.Image_name == myData!!.Image_name)
                found = true;
        }
        return found;
    }


    override fun onResume() {
        super.onResume()
        MainActivity.HideSystemBar(window);
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle);
        //activity system and app bar
        this.supportActionBar!!.hide();
        MainActivity.HideSystemBar(window);
        //update screen orein
        MainActivity.checkorein();

        myData = MYDATA
        thumbnail = THUMBNAIL


        //set the activit as a main screen
        setContentView(R.layout.show_image_fs);
        //update database


        tempdatabase.update_image_info_list_from_database();


        //-----------------------------------------------------------------



        //bottom buttons
        setwallpaper_bottom_button = findViewById(R.id.bottombutton_setwallpaper);
        goback_bottom_button = findViewById(R.id.bottombutton_goback);
        container_bottom_button = findViewById(R.id.bottombutton_container);
        container_bottom_button!!.animate().translationY(200f);//for animation
        //bottom sheet
        val bottomsheetfragment = findViewById<FrameLayout>(R.id.ImageInfo_BottomSheet);
        //set the ui elements
        Full_image = findViewById(R.id.full_image);
        Full_video = findViewById(R.id.full_video);



        counter_image = findViewById(R.id.counter_prograssBar_FullImage);
        cricle_prograssBar = findViewById(R.id.cricle_prograssBar_FullImage);
        MainActivity.setImageView_asLoading(cricle_prograssBar);



       BottomSheetBehavior.from(bottomsheetfragment).apply {



            this.state = BottomSheetBehavior.STATE_COLLAPSED;
            this.isHideable = false;

            val Views = LayoutInflater.from(this@Image_Activity).inflate(R.layout.bottom_sheet,null);
            bottomsheetfragment.addView(Views);

            //set bottom sheet view info
            taggroup = Views.findViewById(R.id.TagGroup);
            taggroup!!.isVisible = false;
            titlePost = Views.findViewById(R.id.title_post);
            auther_post = Views.findViewById(R.id.auther_post);
            url_post = Views.findViewById(R.id.url_post);
            sheet_body = Views.findViewById(R.id.bottom_sheet_body);

            //buttons
            setWallPaperButton = Views.findViewById(R.id.set_bottomsheet_floatingbutton);
            saveWallpaperButton = Views.findViewById(R.id.save_imageButton);
            setfavorite = Views.findViewById(R.id.favorite_bottomsheet_floatingbutton);
            blockimage = Views.findViewById(R.id.block_bottomsheet_floatingbutton);
            bottomsheetarrow = Views.findViewById<ImageButton>(R.id.pullbottom);

            if(isOnDatabase())//check if image is in favorite list database
                setfavorite!!.setImageResource(R.drawable.ic_heartfull);

            //pull buttonimage
            bottomsheetarrow!!.setOnClickListener {
                if(this.state == BottomSheetBehavior.STATE_EXPANDED)
                    this.state = BottomSheetBehavior.STATE_COLLAPSED;
                else
                    this.state = BottomSheetBehavior.STATE_EXPANDED;
            }

            url_post?.let{
                it.setOnClickListener {
                    val linkuri = Uri.parse("https://${MYDATA!!.post_url}");
                    this@Image_Activity.startActivity(Intent(Intent.ACTION_VIEW,linkuri));
                }
            }

            blockimage?.let {
                it.setOnClickListener {
                    it.animate().apply {
                        duration = 500
                    }
                    if(!blocked){
                        blocked = true;
                        tempblockdatavase.add_image_info_to_database(myData!!);
                        Toast.makeText(this@Image_Activity,"added to block list",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this@Image_Activity,"removed from the block list",Toast.LENGTH_SHORT).show();
                        tempblockdatavase.remove_image_info_from_database(myData!!);
                    }
                }
            }

            setfavorite?.setOnClickListener {
                val found = isOnDatabase();
                val button = it as FloatingActionButton;

                if(!found){
                    tempdatabase.add_image_info_to_database(myData!!);
                    button.setImageResource(R.drawable.ic_heartfull);
                    Log.d(Image_Activity::class.java.simpleName, myData!!.Image_name);
                }
                else{
                    tempdatabase.remove_image_info_from_database(myData!!);
                    button.setImageResource(R.drawable.ic_favorite);
                }

                for(i in database.imageinfo_list){
                    Log.i("database","name: ${i.Image_auther}")
                }
            }

            //hide bottmsheet and show setwallpaper button
            setWallPaperButton?.setOnClickListener {
                if(!loaded) {
                    Toast.makeText(this@Image_Activity,"Please wait for the image to load",Toast.LENGTH_LONG).show();
                    return@setOnClickListener;
                }


                //if video call video sevice
                if(MYDATA!!.type != UrlType.Image){
                    getSharedPreferences("LiveWallpaper",Context.MODE_PRIVATE).edit().putString("Video_Path",MediaPath).apply();

                    val wpm = WallpaperManager.getInstance(it.context);
                    val wpminfo = wpm.wallpaperInfo;
                    val videocomponent =ComponentName(applicationContext,livewallpaper::class.java);
                    val gifcomponent = ComponentName(applicationContext,gifwallpaper::class.java);

                    if(wpminfo !=null && (wpminfo.component == videocomponent  || wpminfo.component == gifcomponent))
                        wpm.clear();//if there is a live wallpaper clear it

                    try {
                        val liveintent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        if(MYDATA!!.type == UrlType.Video)
                            liveintent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, videocomponent);
                        else
                            liveintent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, gifcomponent);


                        startActivity(liveintent);
                    }catch (e :Exception){

                    }
                    return@setOnClickListener;
                }

                // normal wallpaper
                //hide bottom sheet and replace it with set wallpaper button
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
            Full_image!!.setOnTouchImageViewListener ( object : OnTouchImageViewListener{
                override fun onMove() {
                    bottomsheetfragment.isVisible = !Full_image!!.isZoomed;
                }
            });

           //set on double tap and hide bottom sheet on zoom
           var timesinclasttocuh : Long =  System.currentTimeMillis();
           Full_video?.let{
               it.setOnTouchListener { view, motionEvent ->
                   it.onTouchEvent(motionEvent);
                   //compare time to last tap to get a double table
                   if(motionEvent.action == 0){
                       val currentTime =  System.currentTimeMillis();
                       if(currentTime - timesinclasttocuh <= 300) {//check if its a double tap.. might need some adjustment
                           if (it.zoom >= 1.01){
                               it.zoomBy(-4f, true)
                           }else{
                               it.zoomBy(4f, true)
                           }
                       }

                       Log.d("Full_video","movtion ${currentTime - timesinclasttocuh} type of")
                       timesinclasttocuh = System.currentTimeMillis();
                   }

                   val matirx = it.engine.matrix.values();
                   Log.d("Full_video","engine matrix ${it.engine.matrix.toShortString()} ")
                   bottomsheetfragment.isVisible = it.zoom <= 1.01;

                   return@setOnTouchListener true;
               }
           }
        }.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
           override fun onStateChanged(bottomSheet: View, newState: Int) {
               if(MYDATA!!.type == UrlType.Video){
                   bottomSheet.isVisible = false;
                   bottomSheet.isVisible = true;
               }
           }
           override fun onSlide(bottomSheet: View, slideOffset: Float) {
               bottomsheetarrow!!.animate().apply {
                   duration = 0;
                   rotationX(180f*slideOffset);
               }
           }
       })




        //----------------------------------------------------


        //set text for image info
        if(myData?.Image_title!!.isNotEmpty())
            titlePost!!.setText(myData?.Image_title);
        else
            titlePost!!.visibility = View.GONE;

        auther_post!!.setText("posted by: ${myData?.Image_auther}");
        counter_image!!.isVisible = false;




        //-----------------------------------------------------------------

        //set wallhaven post info with tag functionality
        if(postmode == mode.wallhaven){
            wallhaven_api.wallhavenApi!!.imageInfo(myData!!) { statusCode ->
                if (statusCode == 200) {
                    runOnUiThread {
                        taggroup!!.isVisible = true;
                        auther_post!!.setText("posted by: ${myData?.Image_auther}");
                        for (i in 0 until TagNameList.size) {
                            if(i > 15)
                                break; //max number of tags on the bottom sheet
                            val TagChip = LayoutInflater.from(this)
                                .inflate(R.layout.tagchip, taggroup, false) as Chip;
                            TagChip.text = TagNameList[i];

                            TagChip.setOnClickListener {
                                try {
                                    val tempTag = wallhaven_api.Tag("&q=${TagNameList[i]}");
                                    val intent = Intent(this, TagActivity::class.java);
                                    intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    TagActivity.Tag_Assing = tempTag;
                                    startActivity(intent);
                                } catch (e: Exception) {
                                    Log.e(
                                        "Reddit_posts",
                                        "error while trying to set image activity"
                                    )
                                }
                            }
                            taggroup!!.addView(TagChip)
                        }
                    }
                }
                titlePost!!.isVisible = false;
            }
        }

        //------------------------------------------------------------------


        //set loading
        val loaderlistener = object : progressRespondBody.progressListener{
            override fun Update(byteread: Long, contentLength: Long, done: Boolean) {
                val byteread_decimal = String.format("%.2f",byteread.toFloat()/1000000);
                val bytesize_decimal = String.format("%.2f",contentLength.toFloat()/1000000);
                runOnUiThread {
                    counter_image!!.setText("$byteread_decimal/$bytesize_decimal Mbp");
                    counter_image!!.isVisible =
                        100*byteread/contentLength != (100).toLong();
                }

                Log.i("progressListener_image","done: ${100*byteread / contentLength}");
            }
        }


        //set bottom sheet colors
        val SetBottomSheetColorsLambda: (bitmap : Bitmap) -> Unit = {bitmap ->
            Palette.Builder(bitmap).generate{ palette ->
                palette?.let { pal ->
                    try{
                        Log.d("Pallate","lightvibrant ${pal.lightVibrantSwatch?.rgb?.toHexString()} " +
                                "vibrant ${pal.vibrantSwatch?.rgb?.toHexString()} " +
                                "darkVibrant ${pal.darkVibrantSwatch?.rgb?.toHexString()} " +
                                "lightMuted ${pal.lightMutedSwatch?.rgb?.toHexString()} " +
                                "muted ${pal.mutedSwatch?.rgb?.toHexString()} " +
                                "darkMuted ${pal.darkMutedSwatch?.rgb?.toHexString()} ")

                        var BottomSheetSwatch : Palette.Swatch? = null;

                        if(pal.mutedSwatch != null){
                            BottomSheetSwatch = pal.mutedSwatch;
                        }else if(pal.darkMutedSwatch != null){
                            BottomSheetSwatch = pal.darkMutedSwatch;
                        }else{
                            BottomSheetSwatch = pal.lightMutedSwatch;
                        }

                        val endBackground = ResourcesCompat.getDrawable(resources,R.drawable.bottomsheetshape,theme);
                        BottomSheetSwatch?.rgb?.let { endBackground!!.setTint(it) };
                        val startBackground = sheet_body!!.background;

                        val animationColor = arrayOf(startBackground,endBackground);
                        val transitionDrawable = TransitionDrawable(animationColor);
                        sheet_body!!.background =transitionDrawable;
                        transitionDrawable.startTransition(1000);

                        //set text color
                        BottomSheetSwatch?.bodyTextColor?.let { titlePost!!.setTextColor(it) };
                        BottomSheetSwatch?.titleTextColor?.let { auther_post!!.setTextColor(it) };
                        //BottomSheetSwatch?.population?.let { url_post!!.setTextColor(it) };

                        //bottons color
                        var buttoncolor = 0;
                        var buttonIconcolor = 0;

                        if(pal.vibrantSwatch != null){
                            buttoncolor = pal.vibrantSwatch!!.rgb;
                            buttonIconcolor = pal.vibrantSwatch!!.bodyTextColor;
                        }else if(pal.darkVibrantSwatch != null){
                            buttoncolor = pal.darkVibrantSwatch!!.rgb;
                            buttonIconcolor = pal.darkVibrantSwatch!!.bodyTextColor;
                        }else{
                            buttoncolor = pal.lightVibrantSwatch!!.rgb;
                            buttonIconcolor = pal.lightVibrantSwatch!!.bodyTextColor;
                        }



                        setWallPaperButton?.backgroundTintList = ColorStateList.valueOf(buttoncolor);
                        setWallPaperButton?.imageTintList = ColorStateList.valueOf(buttonIconcolor);

                        setfavorite?.backgroundTintList = ColorStateList.valueOf(buttoncolor);
                        setfavorite?.imageTintList = ColorStateList.valueOf(buttonIconcolor);

                        saveWallpaperButton?.backgroundTintList = ColorStateList.valueOf(buttoncolor);
                        saveWallpaperButton?.imageTintList = ColorStateList.valueOf(buttonIconcolor);

                        blockimage?.backgroundTintList = ColorStateList.valueOf(buttoncolor);
                        blockimage?.imageTintList = ColorStateList.valueOf(buttonIconcolor);


                    }catch (e: Exception){
                        Log.e(Image_Activity::class.java.simpleName,e.toString());
                    }

                }

            }
        }
            //set Image Loader
            imageloader = ImageLoader.Builder(this)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .networkCachePolicy(CachePolicy.READ_ONLY)
                .crossfade(true)
                .allowHardware(false)
                .components {
                    if(MYDATA!!.type == UrlType.Video){
                        add(VideoFrameDecoder.Factory())
                    }else{
                        if (android.os.Build.VERSION.SDK_INT >= 28) {
                            add(ImageDecoderDecoder.Factory())
                        } else {
                            add(GifDecoder.Factory())
                        }
                    }
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(this.cacheDir.resolve("imagesaved"))
                        //.directory(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path.toPath())
                        .build()
                }
                .okHttpClient {
                    OkHttpClient().newBuilder()
                        .addInterceptor(object : Interceptor{
                            override fun intercept(chain: Interceptor.Chain): Response {
                                val orginalres = chain.proceed(chain.request());
                                return orginalres.newBuilder()
                                    .body(progressRespondBody(orginalres.body!!,loaderlistener))
                                    .build()
                            }
                        })
                        .build()
                }
                .build()


            //load local bitmap and ui imageview data and do it in a callback
            imageloader?.let {
                it.enqueue(coil.request.ImageRequest.Builder(this)
                    .data(myData?.Image_url)
                    .placeholder(thumbnail)
                    .fallback(com.google.android.material.R.drawable.ic_mtrl_chip_close_circle)
                    .target(object : coil.target.Target {
                        override fun onError(error: Drawable?) {
                            super.onError(error)
                            mybitmap = error!!.toBitmap();
                            Full_image!!.setImageBitmap(mybitmap);
                        }

                        override fun onStart(placeholder: Drawable?) {
                            super.onStart(placeholder);
                            mybitmap = placeholder!!.toBitmap();
                            if (loadedPreview)
                                SetBottomSheetColorsLambda(mybitmap!!);
                            Full_image!!.setImageBitmap(mybitmap);

                        }

                        override fun onSuccess(result: Drawable) {
                            super.onSuccess(result);
                            MediaPath = imageloader!!.diskCache!![MemoryCache.Key(MYDATA!!.Image_url).key]!!.data.toString();
                            if(myData!!.type == UrlType.Image  || myData!!.type == UrlType.Gif) {
                                mybitmap = result.toBitmap();
                                Full_image!!.setImageDrawable(result);
                                (result as? Animatable)?.start();
                                SetBottomSheetColorsLambda(mybitmap!!);
                                myData!!.imageRatio =
                                    Image_Ratio(mybitmap!!.width, mybitmap!!.height);
                            }else{
                                val player = MediaPlayer();
                                Full_video!!.setContentSize(MYDATA!!.imageRatio.Width.toFloat(),MYDATA!!.imageRatio.Height.toFloat());
                                Full_video!!.addCallback(object  : ZoomSurfaceView.Callback{
                                    override fun onZoomSurfaceCreated(view: ZoomSurfaceView) {
                                        player.setSurface(Full_video!!.surface);
                                    }
                                    override fun onZoomSurfaceDestroyed(view: ZoomSurfaceView) {
                                        if(player.isPlaying) player!!.stop();
                                        player.release();
                                    }
                                });


                                player.apply {
                                    isLooping = true;
                                    setDataSource(MediaPath);
                                    setOnPreparedListener {
                                        Full_video!!.visibility = View.VISIBLE;
                                        Full_image!!.visibility = View.INVISIBLE;
                                    }
                                    setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                    prepare();
                                    start();
                                }
                            }
                            loaded = true;
                        }
                    })
                    .listener(
                        onSuccess = { _, _ ->
                            cricle_prograssBar?.visibility = View.GONE;
                        },
                        onCancel = {
                            cricle_prograssBar?.visibility = View.GONE;
                            Log.i("cricle_prograssBar", "cancled");
                        },
                        onError = { _, _ ->
                            cricle_prograssBar?.visibility = View.GONE;
                            Log.i("cricle_prograssBar", "error");
                        },
                        onStart = {
                            cricle_prograssBar?.visibility = View.VISIBLE;
                            Log.i("cricle_prograssBar", "starting");
                        }

                    )
                    .build()
                );
            }

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
                saveMedia(this, MediaPath!!, MYDATA!!.type, MYDATA!!.Image_name);
            }else{
                Toast.makeText(this,"Please Wait for the Wallpaper to load",Toast.LENGTH_LONG).show();
            }
        };

        Log.d("Image_Activity","Info url ${myData?.Image_url} name ${myData?.Image_name} thumbnail ${myData?.Image_thumbnail}");
        //------------------------------------------------------------------------------

    }

    override fun onDestroy(){
        super.onDestroy()
        Log.d("DestoryLog","Image Acvtivity");
        Full_image = null;
        Full_video = null;

        mybitmap?.recycle();
        TagNameList = emptyArray();
        imageloader?.let {
            it.memoryCache?.clear();
            it.shutdown();
        }
    }
}