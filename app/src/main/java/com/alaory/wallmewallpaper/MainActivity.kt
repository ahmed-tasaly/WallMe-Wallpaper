package com.alaory.wallmewallpaper

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.alaory.wallmewallpaper.databinding.ActivityMainBinding
import com.alaory.wallmewallpaper.postPage.Reddit_posts
import com.alaory.wallmewallpaper.postPage.favorite_list
import com.alaory.wallmewallpaper.postPage.wallhaven_posts
import com.alaory.wallmewallpaper.settings.Reddit_settings
import com.alaory.wallmewallpaper.settings.wallhaven_settings
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(){

    private var binding: ActivityMainBinding? = null;

    //fragmenst
    var wallhaven_filter = wallhaven_settings();
    var reddit_filter = Reddit_settings();

    var settings = com.alaory.wallmewallpaper.settings.settings();


    var redditPosts  = Reddit_posts();
    var wallhavenPosts = wallhaven_posts();
    var favoriteList = favorite_list();




    var firstTimeOPen = true;

    enum class menu{
        reddit,
        wallhaven,
        favorite
    }

    companion object{
        //fragment check
        var fragmentcheck = true;

        var num_post_in_Column = 2;
        var last_orein = Configuration.ORIENTATION_PORTRAIT;
        var LastFragmentMode: Fragment?  = null;
        var mainactivity : MainActivity? = null;




        //nav
        var lastfloatingiconIcon : menu? = null;
        var filterbutton : FloatingActionButton ? = null;
        var navbox : ConstraintLayout ?  = null;
        //navigation buttons
        var reddit_floatingButton : FloatingActionButton? = null;
        var wallhaven_floatingButton : FloatingActionButton? = null;
        var favorite_floatingButton : FloatingActionButton? = null;


        fun checkorein(){
            when(Resources.getSystem().configuration.orientation){
                Configuration.ORIENTATION_PORTRAIT ->{
                    num_post_in_Column = 2;
                    last_orein = Configuration.ORIENTATION_PORTRAIT;
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    num_post_in_Column = 4;
                    last_orein = Configuration.ORIENTATION_LANDSCAPE;
                }
                Configuration.ORIENTATION_UNDEFINED -> {
                    num_post_in_Column = 2;
                    last_orein = Configuration.ORIENTATION_UNDEFINED;
                }
                else -> {}
            }
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

        fun change_fragment(fragment: Fragment,shownav : Boolean = false,changelastfragment : Boolean = false){

            if (changelastfragment)
                LastFragmentMode = fragment;

            val fragman = mainactivity?.supportFragmentManager?.beginTransaction();
            LastFragmentMode?.let {
                fragman?.remove(it)!!;
            }
            fragman?.replace(R.id.container,fragment);
            fragman?.commitAllowingStateLoss();



            if(shownav)
                shownav();
        }

        fun HideSystemBar(window: Window){
            window.decorView.apply {
             this.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ){
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }

        fun setImageView_asLoading(imageView: ImageView?){
            imageView!!.setImageResource(R.drawable.loading_anim);
            val animati : AnimatedVectorDrawable =  imageView.drawable as AnimatedVectorDrawable;
            animati.registerAnimationCallback(object  : Animatable2.AnimationCallback(){
                override fun onAnimationEnd(drawable: Drawable?) {
                    super.onAnimationEnd(drawable)
                    (drawable as AnimatedVectorDrawable).start();
                }
            })
            animati.start();
        }
    }


    override fun onResume() {
        super.onResume();
        BottonLoading.loctionbottom = 0;
        BottonLoading.updatebottom_navtigation(0);
        HideSystemBar(window);

    }

    fun showstartdialog(){
        AlertDialog.Builder(this,R.style.Dialog_first)
            .setTitle("WAIT CAUTION")
            .setMessage("I am NOT in control nor affiliated of the app content you may see some disturbing or harmful content so please be careful.")
            .setNeutralButton("alright i'll be safe take care",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    firstTimeOPen = false;
                    getSharedPreferences("main", MODE_PRIVATE).edit().putBoolean("firstTimeOPen",firstTimeOPen).apply();
                }
            })
            .create()
            .show()

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        HideSystemBar(window);

        this.supportActionBar!!.hide();


        //set global mainActivity
        mainactivity = this;


        //set ui
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding!!.root);

        //check prefs
        firstTimeOPen = getSharedPreferences("main", MODE_PRIVATE).getBoolean("firstTimeOPen",true);

        //set ui fragment
        if(mainactivity?.supportFragmentManager?.fragments!!.lastOrNull() != null)
            change_fragment(mainactivity?.supportFragmentManager?.fragments!!.lastOrNull()!!);
        else
            change_fragment(redditPosts);

        //update screen orientation data
        checkorein();


        //pull posts from apis
        Reddit_Api.Update_Api_key{
            redditPosts.LoadMore();
            wallhavenPosts.LoadMore();
        }//init wallhaven & reddit api to get the key and set data to array

        if(firstTimeOPen){
            showstartdialog();
        }


        //set buttom navigtion
        filterbutton = findViewById<FloatingActionButton>(R.id.filterbutton);
        navbox = findViewById(R.id.navigation_constraint_box);
        reddit_floatingButton = findViewById(R.id.reddit_list_navigation_button);
        wallhaven_floatingButton = findViewById(R.id.wallhaven_list_navigation_button);
        favorite_floatingButton = findViewById(R.id.favorite_list_navigation_button);


        val settingsprefs = this.getSharedPreferences("settings", MODE_PRIVATE);

        if(!settingsprefs.getBoolean("reddit_source",true)){
            reddit_floatingButton!!.visibility = View.GONE;
            lastfloatingiconIcon = menu.wallhaven;
        }
        if(!settingsprefs.getBoolean("wallhaven_source",false)){
            wallhaven_floatingButton!!.visibility = View.GONE;
            lastfloatingiconIcon = menu.reddit;
            if(!settingsprefs.getBoolean("reddit_source",true))
                lastfloatingiconIcon = menu.favorite;
        }

        if(lastfloatingiconIcon != null){
            setFragmentmenu(lastfloatingiconIcon!!);
        }
        else{
            setFragmentmenu(menu.reddit);
        }




        //set button navitgtion actions
        reddit_floatingButton?.let {
            it.setOnClickListener {
                setFragmentmenu(menu.reddit);
            }
        }
        wallhaven_floatingButton?.let {
            it.setOnClickListener {
                setFragmentmenu(menu.wallhaven);
            }
        }
        favorite_floatingButton?.let {
            it.setOnClickListener {
                setFragmentmenu(menu.favorite);
            }
        }


        //set floating button actions
        filterbutton?.setOnClickListener {
            when(lastfloatingiconIcon!!){
                menu.reddit -> {
                    change_fragment(reddit_filter);
                }
                menu.wallhaven -> {
                    change_fragment(wallhaven_filter);
                }
                menu.favorite -> {
                    change_fragment(settings);
                }
            }
        }

    }


    fun setFragmentmenu(menuButton : menu ){
        when(menuButton){
            menu.reddit -> {
                change_fragment(redditPosts,false,true);
            }
            menu.wallhaven -> {
                change_fragment(wallhavenPosts,false,true);
            }
            menu.favorite -> {
                change_fragment(favoriteList,false,true);
            }
        }
        lastfloatingiconIcon = menuButton;
        setfloatingIcon(lastfloatingiconIcon!!);
        setFABcolor(lastfloatingiconIcon!!);
    }


    fun setFABcolor(icon : menu){
        reddit_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
        wallhaven_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
        favorite_floatingButton?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.Buttons,theme));
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
                filterbutton!!.setImageResource(R.drawable.ic_outline_settings_24);
            }
        }
    }


}
