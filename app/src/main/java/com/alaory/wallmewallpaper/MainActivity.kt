package com.alaory.wallmewallpaper

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.alaory.wallmewallpaper.api.Reddit_Api_Contorller
import com.alaory.wallmewallpaper.api.wallhaven_api
import com.alaory.wallmewallpaper.databinding.ActivityMainBinding
import com.alaory.wallmewallpaper.postPage.Reddit_posts
import com.alaory.wallmewallpaper.postPage.favorite_list
import com.alaory.wallmewallpaper.postPage.wallhaven_posts
import com.alaory.wallmewallpaper.postPage.wallpaper_changer
import com.alaory.wallmewallpaper.settings.Reddit_settings
import com.alaory.wallmewallpaper.settings.wallhaven_settings
import com.alaory.wallmewallpaper.wallpaper.loadMedia
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okio.Path.Companion.toPath
import java.io.File

class MainActivity : AppCompatActivity(){

    private var binding: ActivityMainBinding? = null;
    //set menu controller
    var menucontroll : MenuChange? = null;

    //fragmenst

    //set settings
    var wallhaven_filter : wallhaven_settings? = null;
    var reddit_filter : Reddit_settings? = null;
    var settings : com.alaory.wallmewallpaper.settings.settings? = null;

    //set post pages
    var redditPosts  : Reddit_posts? = null;
    var wallhavenPosts : wallhaven_posts? = null;
    var favoriteList : favorite_list? = null;
    var wallpaperchanger : wallpaper_changer? = null


    //nav
    var filterbutton : FloatingActionButton ? = null;
    var navbox : ConstraintLayout ?  = null;
    //navigation buttons
    var reddit_floatingButton : FloatingActionButton? = null;
    var wallhaven_floatingButton : FloatingActionButton? = null;
    var favorite_floatingButton : FloatingActionButton? = null;
    var wallpaperChangerButton : ImageView? = null;


    var firstTimeOPen = true;

    enum class menu{
        reddit,
        wallhaven,
        favorite,
        wallpaperchanger,
        reddit_set,
        wallhaven_set,
        settings
    }

    companion object{
        var LastSetMenu : MainActivity.menu? = null;
    }

    override fun onResume() {
        super.onResume();
        BottonLoading.loctionbottom = 0;
        menucontroll?.PlayAnimation_forNav {
            it?.translationY(0f);
        }
        wallmewallpaper.HideSystemBar(window);

    }

    fun showstartdialog(){
        AlertDialog.Builder(this,R.style.Dialog_first)
            .setTitle("WAIT CAUTION")
            .setMessage("I am NOT in control nor affiliated of the app content you may see some disturbing,harmful,nsfw or sketchy content so please be careful.\n" +
                    "tip: you can long press a post to block it")
            .setNeutralButton("alright i'll be safe take care",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    firstTimeOPen = false;
                    getSharedPreferences("main", MODE_PRIVATE).edit().putBoolean("FirstTimeOpen",firstTimeOPen).apply();
                }
            })
            .create()
            .show()

    }
    fun enableBottomButtons(enable: Boolean){
        filterbutton!!.isEnabled = enable;
        reddit_floatingButton!!.isEnabled = enable;
        wallhaven_floatingButton!!.isEnabled = enable;
        favorite_floatingButton!!.isEnabled = enable;
    }


    fun hidenav(){
        enableBottomButtons(false)
        navbox!!.clearAnimation();
        navbox?.animate().apply {
            this!!.duration = 300;
            this!!.translationY(1000f);
            BottonLoading.loctionbottom = 1000;
            this.withEndAction {
                navbox!!.visibility = View.GONE;
            }
        }
    }

    fun shownav(){
        enableBottomButtons(true)
        navbox!!.clearAnimation();
        navbox?.animate().apply {
            this!!.duration = 300;
            this!!.translationY(0f);
            BottonLoading.loctionbottom = 0;
            this.withStartAction {
                navbox!!.visibility = View.VISIBLE;
            }
        }
    }

    interface MenuChange{
        fun ChangeTo(menuitem : menu,shownav: Boolean = true,changelastfragment: Boolean = false);
        fun Shownav(shownav: Boolean);
        fun hidenavbuttons(menuitem: menu,show: Boolean);
        fun PlayAnimation_forNav(playanimation : (animate : ViewPropertyAnimator?) -> Unit);
    }

    //init database
    val DataBase = database(this);
    val blockdatabase = database(this,database.ImageBlock_Table,"${database.ImageBlock_Table}.dp");



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        wallmewallpaper.HideSystemBar(window);

        this.supportActionBar!!.hide();

        DataBase.onCreate(DataBase.writableDatabase);
        blockdatabase.onCreate(blockdatabase.writableDatabase);

        DataBase.update_image_info_list_from_database();
        blockdatabase.update_image_info_list_from_database();

        if(Reddit_Api.redditcon == null)
            Reddit_Api.redditcon = Reddit_Api_Contorller();
        if(wallhaven_api.wallhavenApi == null)
            wallhaven_api.wallhavenApi = wallhaven_api();

        Reddit_posts.firsttime = true;
        //update settings
        Reddit_settings.loadprefs(this);
        wallhaven_settings.loadprefs(this);


        //set ui
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding!!.root);

        //check prefs
        firstTimeOPen = getSharedPreferences("main", MODE_PRIVATE).getBoolean("FirstTimeOpen",true);

        //update screen orientation data
        wallmewallpaper.checkorein();


        //set menu controller
        menucontroll = object : MenuChange{
            override fun ChangeTo(menuitem: menu, shownav: Boolean, changelastfragment: Boolean) {
                val FragmentControll = supportFragmentManager.beginTransaction();
                supportFragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
                when(menuitem){
                    menu.reddit ->{
                        redditPosts?.let { FragmentControll.replace(R.id.container, it) };
                    }
                    menu.favorite ->{
                        favoriteList?.let { FragmentControll.replace(R.id.container, it) };
                    }
                    menu.wallhaven ->{
                        wallhavenPosts?.let { FragmentControll.replace(R.id.container, it) };
                    }
                    menu.wallpaperchanger ->{
                        wallpaperchanger?.let { FragmentControll.replace(R.id.container, it) }
                    }
                    menu.reddit_set ->{
                        reddit_filter?.let { FragmentControll.replace(R.id.container, it) };
                    }
                    menu.settings ->{
                        settings?.let { FragmentControll.replace(R.id.container, it) };
                    }
                    menu.wallhaven_set ->{
                        wallhaven_filter?.let { FragmentControll.replace(R.id.container, it) };
                    }
                }

                FragmentControll.commitAllowingStateLoss();
                Shownav(shownav);
                LastSetMenu = menuitem;
                setfloatingIcon(LastSetMenu!!);
                setFABcolor(LastSetMenu!!);
            }

            override fun Shownav(shownav: Boolean) {
                if(shownav)
                    shownav();
                else
                    hidenav();
            }

            override fun hidenavbuttons(menuitem: menu,show : Boolean) {
                when(menuitem){
                    menu.wallhaven ->{
                        if(show)
                            wallhaven_floatingButton?.visibility = View.VISIBLE;
                        else
                            wallhaven_floatingButton?.visibility = View.GONE;
                    }
                    menu.reddit ->{
                        if(show)
                            reddit_floatingButton?.visibility = View.VISIBLE;
                        else
                            reddit_floatingButton?.visibility = View.GONE;
                    }
                    else ->{}
                }
            }

            override fun PlayAnimation_forNav(playanimation: (animate: ViewPropertyAnimator?) -> Unit) {
                navbox?.animate().apply {
                    playanimation(this);
                }

            }
        }



        //set  fragments
        redditPosts  = Reddit_posts(menucontroll);
        wallhavenPosts = wallhaven_posts(menucontroll);
        favoriteList = favorite_list(menucontroll);
        wallpaperchanger = wallpaper_changer();

        wallhaven_filter = wallhaven_settings(menucontroll);
        reddit_filter = Reddit_settings(menucontroll);
        settings = com.alaory.wallmewallpaper.settings.settings(menucontroll);



        //pull posts from apis
        Reddit_Api.redditcon?.Update_Api_key{
            redditPosts?.LoadMore();
            wallhavenPosts?.LoadMore();
        }//init wallhaven & reddit api to get the key and set data to array


        //set buttom navigtion
        filterbutton = findViewById<FloatingActionButton>(R.id.filterbutton);
        navbox = findViewById(R.id.navigation_constraint_box);
        reddit_floatingButton = findViewById(R.id.reddit_list_navigation_button);
        wallhaven_floatingButton = findViewById(R.id.wallhaven_list_navigation_button);
        favorite_floatingButton = findViewById(R.id.favorite_list_navigation_button);
        wallpaperChangerButton = findViewById(R.id.wallpaper_changer_list_navigation_button)


        if(firstTimeOPen){
            showstartdialog();
        }

        val settingsprefs = this.getSharedPreferences("settings", MODE_PRIVATE);

        //check if reddit source is enabled and should show
        if(!settingsprefs.getBoolean("reddit_source",true)){
            reddit_floatingButton!!.visibility = View.GONE;
            if(LastSetMenu == null)
                LastSetMenu = menu.wallhaven;
        }
        //check if wallhaven source is enabled and should show
        if(!settingsprefs.getBoolean("wallhaven_source",false)){
            wallhaven_floatingButton!!.visibility = View.GONE;
            if(LastSetMenu == null)
                LastSetMenu = menu.reddit;

            if(!settingsprefs.getBoolean("reddit_source",true))
                LastSetMenu = menu.favorite;
        }

        //set ui fragment
        if(LastSetMenu != null)
            menucontroll?.ChangeTo(LastSetMenu!!);
        else
            menucontroll?.ChangeTo(menu.reddit);






        //set button navitgtion actions
        reddit_floatingButton?.let {
            it.setOnClickListener {
                menucontroll?.ChangeTo(menu.reddit);
            }
        }
        wallhaven_floatingButton?.let {
            it.setOnClickListener {
                menucontroll?.ChangeTo(menu.wallhaven);
            }
        }
        favorite_floatingButton?.let {
            it.setOnClickListener {
                menucontroll?.ChangeTo(menu.favorite);
            }
        }
        wallpaperChangerButton?.let {
            it.setOnClickListener {
                menucontroll?.ChangeTo(menu.wallpaperchanger)
            }
        }


        //set floating button actions
        filterbutton?.setOnClickListener {
            when(LastSetMenu) {
                menu.reddit ->{
                    menucontroll?.ChangeTo(menu.reddit_set,false)
                }
                menu.wallhaven ->{
                    menucontroll?.ChangeTo(menu.wallhaven_set,false)
                }
                menu.favorite ->{
                    loadMedia(this);
                    //menucontroll?.ChangeTo(menu.settings,false)
                }
                menu.wallpaperchanger ->{
                    menucontroll?.ChangeTo(menu.settings,false)
                }
                else -> {}
            }
        }

    }


    fun setFABcolor(icon : menu){
        reddit_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
        wallhaven_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
        favorite_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
        wallpaperChangerButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
        favorite_floatingButton?.setImageResource(R.drawable.ic_favorite);
        when (icon){
            menu.reddit ->{
                reddit_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Selected,theme));
            }
            menu.wallhaven ->{
                wallhaven_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Selected,theme));
            }
            menu.favorite ->{
                favorite_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Selected,theme));
                favorite_floatingButton?.setImageResource(R.drawable.ic_heartfull);
            }
            menu.wallpaperchanger ->{
                wallpaperChangerButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Selected,theme));
            }
            else -> {}
        }
    }


    fun setfloatingIcon(id : menu){
        when(id){
            menu.reddit -> {
                filterbutton!!.setImageResource(R.drawable.filter_ic);
            }
            menu.wallhaven -> {
                filterbutton!!.setImageResource(R.drawable.filter_ic);
            }
            menu.favorite -> {
                filterbutton!!.setImageResource(R.drawable.add_ic);
            }
            menu.wallpaperchanger -> {
                filterbutton!!.setImageResource(R.drawable.ic_outline_settings_24);
            }
            else -> {}
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        Log.d("DestoryLog",this::class.java.simpleName);

        wallhaven_filter = null;
        reddit_filter = null;
        settings = null;
        redditPosts = null;
        wallhavenPosts  = null;
        favoriteList = null;


        Reddit_Api.redditcon?.reddit?.dispatcher?.cancelAll();
        Reddit_Api.redditcon?.reddit?.cache?.close();
        Reddit_Api.redditcon?.reddit_global_posts?.clear();
        Reddit_Api.redditcon?.Subreddits = emptyArray();
        Reddit_Api.redditcon = null;

        wallhaven_api.wallhavenApi?.wallhaven_homepage_posts?.clear();
        wallhaven_api.wallhavenApi?.wallhavenRequest?.dispatcher?.cancelAll();
        wallhaven_api.wallhavenApi?.wallhavenRequest?.cache?.close();
        wallhaven_api.wallhavenApi = null;


        database.imageblock_list = emptyArray();
        database.imageinfo_list = emptyArray();
        Runtime.getRuntime().exit(1);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 8777 && resultCode == RESULT_OK ){
            val wallpaperpath =data!!.data!!.toString();
            val conpath = Uri.parse(data!!.data!!.toString());
            val conres = this.contentResolver;
            Log.d("outInfo","reqcode : $requestCode  res: $resultCode  data: ${data!!.data!!.path} type: ${conres.getType(conpath)!!.split('/')[1]} ");
            val type = conres.getType(conpath)!!.split('/')[1];
            val wallpapername = conpath.lastPathSegment!!;
            val savePath = "${this.getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!.path.toPath()}/${wallpapername}.${type}";
            val sotrageinternal = File(savePath);
            sotrageinternal.writeBytes(conres.openInputStream(conpath)!!.readBytes());

            var Imageinfo = Image_Info(Uri.fromFile(sotrageinternal).toString(),Uri.fromFile(sotrageinternal).toString(),wallpapername,wallpapername,wallpapername,"",Image_Ratio(1,1),UrlType.Image);
            Log.d("outInfo","path ${Uri.fromFile(sotrageinternal).toString()}")
            var intent = Intent(this, Image_Activity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Image_Activity.MYDATA = Imageinfo;
            Image_Activity.THUMBNAIL = null;
            Image_Activity.save_local_external = false;
            Image_Activity.postmode = Image_Activity.mode.reddit;
            Image_Activity.loadedPreview = false;
            startActivity(intent);

        }
    }
}
