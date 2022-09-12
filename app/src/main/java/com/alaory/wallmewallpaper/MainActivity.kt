package com.alaory.wallmewallpaper

import android.content.DialogInterface
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.alaory.wallmewallpaper.databinding.ActivityMainBinding
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

    //init database
    val DataBase = database(this);
    var firstTimeOPen = true;


    companion object{
        //fragment check
        var fragmentcheck = true;

        var num_post_in_Column = 2;
        var last_orein = Configuration.ORIENTATION_PORTRAIT;
        var LastFragmentMode: Fragment?  = null;
        var mainactivity : MainActivity? = null;




        //nav
        var lastfloatingiconIcon : Int = 0;
        var bottomnav : BottomNavigationView ? = null;
        var filterbutton : FloatingActionButton ? = null;
        var navbox : ConstraintLayout ?  = null;



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
            for(i in 0 until bottomnav!!.menu.size())
                bottomnav!!.menu.get(i).isEnabled = enable;
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

        fun change_fragment(fragment: Fragment,shownav : Boolean = false){

            LastFragmentMode = mainactivity?.supportFragmentManager?.fragments!!.lastOrNull();

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
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
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


        //update settings
        Reddit_settings.loadprefs(this);
        wallhaven_settings.loadprefs(this);
        //update database
        DataBase.update_image_info_list_from_database();


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
        bottomnav = findViewById<BottomNavigationView>(R.id.bottom_navigation);
        bottomnav?.selectedItemId = R.id.Reddit_posts_List;

        if(lastfloatingiconIcon != 0)
            setfloatingIcon(lastfloatingiconIcon);

        //set button navitgtion actions
        bottomnav?.setOnItemSelectedListener {
           when (it.itemId){
               R.id.Reddit_posts_List -> {
                   change_fragment(redditPosts);
               }
               R.id.wallhaven_posts_list -> {
                   change_fragment(wallhavenPosts);
               }
               R.id.Favorite_posts_list -> {
                   change_fragment(favoriteList);
               }
           }
            lastfloatingiconIcon = it.itemId;
            setfloatingIcon(it.itemId);
            return@setOnItemSelectedListener true;
        }


        //set floating button actions
        filterbutton?.setOnClickListener {
            val button = it as FloatingActionButton;
            when(bottomnav?.selectedItemId){
                R.id.Reddit_posts_List -> {
                    hidenav();
                    change_fragment(reddit_filter);
                }
                R.id.wallhaven_posts_list -> {
                    hidenav();
                    change_fragment(wallhaven_filter);
                }
                R.id.Favorite_posts_list -> {
                    hidenav()
                    change_fragment(settings);
                }
                else -> {}
            }
        }

    }

    fun setfloatingIcon(id : Int){
        when(id){
            R.id.Reddit_posts_List -> {
                filterbutton!!.setImageResource(R.drawable.filter_ic);
            }
            R.id.wallhaven_posts_list -> {
                filterbutton!!.setImageResource(R.drawable.filter_ic);
            }
            R.id.Favorite_posts_list -> {
                filterbutton!!.setImageResource(R.drawable.ic_outline_settings_24);
            }
        }
    }


}
